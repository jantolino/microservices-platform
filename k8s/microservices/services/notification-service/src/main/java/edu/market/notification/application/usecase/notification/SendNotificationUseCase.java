package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.notification.SendNotificationUseCasePort;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.event.NotificationFailedEvent;
import edu.market.notification.domain.event.NotificationSentEvent;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.domain.port.output.service.NotificationChannelServicePort;
import edu.market.notification.domain.port.output.service.SerializationServicePort;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Implementación del caso de uso para enviar notificaciones.
 * 
 * Este caso de uso es responsable de coordinar el proceso de envío de notificaciones,
 * incluyendo el registro en el repositorio, el envío a través del canal apropiado,
 * el guardado de eventos en la tabla outbox y el registro de métricas.
 * 
 * Patrones de diseño implementados:
 * - Arquitectura Hexagonal: Implementa un puerto de entrada y utiliza puertos de salida
 * - Inyección de Dependencias: Recibe todas las dependencias por constructor 
 * - Transactional Outbox: Guarda eventos en la tabla outbox dentro de la transacción principal
 * - Domain Events: Utiliza eventos de dominio para representar cambios de estado
 * - Inmutabilidad: Utiliza objetos inmutables para los eventos y modelos de dominio
 * - Instrumentation: Registra métricas y logs para monitoreo y observabilidad
 */
public class SendNotificationUseCase implements SendNotificationUseCasePort {

    private final NotificationChannelServicePort notificationChannel;
    private final NotificationRepositoryPort notificationRepository;
    private final TransactionalOutboxRepositoryPort outboxRepository;
    private final LoggingPort log;
    private final SerializationServicePort serialization;

    public SendNotificationUseCase(
            NotificationChannelServicePort notificationChannel,
            NotificationRepositoryPort notificationRepository,
            TransactionalOutboxRepositoryPort outboxRepository,            
            LoggingPort log,
            SerializationServicePort serialization) {
        this.notificationChannel = notificationChannel;
        this.notificationRepository = notificationRepository;
        this.outboxRepository = outboxRepository;
        this.log = log;
        this.serialization = serialization;
    }
    
    /**
     * Envía una notificación a través del canal especificado.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * 
     * @param command Value Object con los datos de la notificación a enviar
     * @return Value Object con la información de la notificación enviada
     */
    @Override
    public NotificationResponse send(NotificationCommand command, ClientContextCommand clientContext) {
        // Convertir el comando a entidad de dominio
        log.info("send - Init");
        Notification notification = NotificationMapper.toEntity(command);
        
        // Guardar la notificación antes de enviarla (parte del dominio principal)
        Notification savedNotification = notificationRepository.save(notification);
        Set<NotificationChannelType> channels = savedNotification.getChannels();
        log.debug("send - Notification saved with ID: %s, channels: %s", savedNotification.getId(), channels);
        
        try {
            // Verificar si el canal está disponible
            if (!notificationChannel.isAvailable()) {
                log.debug("send - Notification channel not available");
                String errorMessage = "Notification channel not available";
                
                savedNotification.markAsFailed(errorMessage);                
                
                // Guardar evento de fallo en la tabla outbox (patrón Transactional Outbox)
                log.debug("send - Call saveFailedEventToOutbox");
                saveFailedEventToOutbox(savedNotification, errorMessage);
                
                log.info("send - End");
                return NotificationMapper.toResponse(savedNotification);
            }
            
            // Enviar la notificación
            log.debug("send - Call notificationChannel.send");
            notificationChannel.send(savedNotification);
            
            // Actualizar el estado de la notificación
            log.debug("send - Mark notification as sent");
            savedNotification.markAsSent();
            
            // Guardar la notificación actualizada
            log.debug("send - Call notificationRepository.save for updated notification");
            Notification updatedNotification = notificationRepository.save(savedNotification);
            
            // Guardar el evento en la tabla outbox
            log.debug("send - Call saveSentEventToOutbox");
            saveSentEventToOutbox(updatedNotification);
            
            log.info("send - End");
            return NotificationMapper.toResponse(updatedNotification);
        } catch (Exception e) {
            // En caso de excepción, marcar la notificación como fallida
            String errorMessage = "Error sending notification: " + e.getMessage();
            log.error("send - Exception occurred", e);
            savedNotification.markAsFailed(errorMessage);
            
            // Guardar evento de fallo en la tabla outbox (patrón Transactional Outbox)
            log.debug("send - Call saveFailedEventToOutbox");
            saveFailedEventToOutbox(savedNotification, errorMessage);
            
            // Guardar la notificación con el estado de error
            Notification updatedNotification = notificationRepository.save(savedNotification);
            
            log.info("send - End (exception)");
            return NotificationMapper.toResponse(updatedNotification);
        } 
    }
    
    /**
     * Guarda un evento de notificación enviada en la tabla outbox.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * El evento será posteriormente procesado por un job dedicado que lo publicará en RabbitMQ.
     * 
     * @param notification La notificación enviada
     */
    private void saveSentEventToOutbox(Notification notification) {
        log.debug("saveSentEventToOutbox - Saving sent event to outbox for notification: %s", notification.getId());
        try {
            NotificationSentEvent event = NotificationSentEvent.fromNotification(notification);
            String payload = serialization.serialize(event);
            
            TransactionalOutbox outboxEvent = TransactionalOutbox.builder()
                .withId(UUID.randomUUID())
                .withStatusType(EventStatusType.SENT)
                .withAggregateId(notification.getId().toString())
                .withPayload(payload)
                .withCreatedAt(LocalDateTime.now())
                .withProcessed(false)
                .withProcessedAt(null)
                .withRetryCount(0)
                .withMessage("")
                .withLastRetryAt(null)
                .withEventType(EventType.NOTIFICATION_SENT)
                .build();
            
            log.debug("saveSentEventToOutbox - Call outboxRepository.save");
            outboxRepository.save(outboxEvent);
            // Las métricas técnicas (latencia, throughput) son manejadas automáticamente por el aspecto MetricsCollectionAspect
            log.debug("saveSentEventToOutbox - Event saved successfully");

        } catch (Exception e) {
            // Registrar el error pero no lo propaga para no afectar el flujo principal
            log.error("saveSentEventToOutbox - Error saving notification sent event to outbox", e);
        }
    }
    
    /**
     * Guarda un evento de notificación fallida en la tabla outbox.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * El evento será posteriormente procesado por un job dedicado que lo publicará en RabbitMQ.
     * 
     * @param notification La notificación fallida
     * @param errorMessage El mensaje detallado del error ocurrido
     */
    private void saveFailedEventToOutbox(Notification notification, String errorMessage) {
        log.debug("saveFailedEventToOutbox - Saving failed event to outbox for notification: %s, error: %s", notification.getId(), errorMessage);
        try {
            NotificationFailedEvent event = NotificationFailedEvent.fromNotification(notification, errorMessage);
            String payload = serialization.serialize(event);
            
            TransactionalOutbox outboxEvent = TransactionalOutbox.builder()
                .withId(UUID.randomUUID())
                .withStatusType(EventStatusType.FAILED)
                .withAggregateId(notification.getId().toString())
                .withPayload(payload)
                .withCreatedAt(LocalDateTime.now())
                .withProcessed(false)
                .withProcessedAt(null)
                .withRetryCount(0)
                .withMessage("")
                .withLastRetryAt(null)
                .withEventType(EventType.NOTIFICATION_FAILED)
                .build();
            
            log.debug("saveFailedEventToOutbox - Call outboxRepository.save");
            outboxRepository.save(outboxEvent);
            // Las métricas técnicas (latencia, throughput) son manejadas automáticamente por el aspecto MetricsCollectionAspect
            log.debug("saveFailedEventToOutbox - Event saved successfully");

        } catch (Exception e) {
            // Registrar el error pero no lo propaga para no afectar el flujo principal
            log.error("saveFailedEventToOutbox - Error saving notification failed event to outbox", e);
        }
    }    
}
