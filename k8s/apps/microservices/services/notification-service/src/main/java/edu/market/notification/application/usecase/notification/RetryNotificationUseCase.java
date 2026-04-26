package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.notification.RetryNotificationUseCasePort;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.domain.port.output.service.NotificationChannelServicePort;
import edu.market.notification.domain.port.output.service.SerializationServicePort;
import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.event.DomainEventFactory;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

/**
 * Implementación del caso de uso para reintentar manualmente notificaciones fallidas.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 * 
 * Este caso de uso complementa el proceso automático de reintentos del patrón Transactional Outbox,
 * proporcionando una forma explícita para que:
 * - Los administradores puedan reintentar notificaciones específicas que fallaron
 * - Se pueda ofrecer una interfaz de usuario donde los usuarios puedan reintentar notificaciones importantes
 * - Sirva como mecanismo de recuperación para casos excepcionales que requieren intervención manual
 * 
 * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
 * entre la base de datos y el sistema de mensajería.
 */
public class RetryNotificationUseCase implements RetryNotificationUseCasePort {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationChannelServicePort notificationChannel;
    private final TransactionalOutboxRepositoryPort outboxRepository;
    private final LoggingPort log;  
    private final SerializationServicePort serialization;  
    private final int maxRetries;

    public RetryNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            NotificationChannelServicePort notificationChannel,
            TransactionalOutboxRepositoryPort outboxRepository,
            LoggingPort log,
            SerializationServicePort serialization,
            int maxRetries) {
        this.notificationRepository = notificationRepository;
        this.notificationChannel = notificationChannel;
        this.outboxRepository = outboxRepository;
        this.log = log;
        this.serialization = serialization;
        this.maxRetries = maxRetries;
    }

    /**
     * Reintenta manualmente el envío de una notificación que ha fallado previamente.
     * Este método está diseñado para ser invocado explícitamente a través de una API o interfaz de usuario,
     * permitiendo a los administradores o usuarios finales reintentar notificaciones específicas sin esperar
     * al proceso automático de reintentos.
     * 
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería, creando un nuevo evento en el outbox
     * con estado PENDING para que sea procesado por el scheduler.
     * 
     * @param notificationId El identificador de la notificación a reintentar manualmente
     * @return Value Object con la información de la notificación reintentada
     */
    @Override
    public NotificationResponse retry(UUID notificationId, ClientContextCommand clientContext) {
        
        log.info("retry - Init for notification ID: {}", notificationId);
        
        try {
            // Buscar la notificación en el repositorio
            Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
            
            if (notificationOpt.isEmpty()) {
                log.warn("retry - Notification not found with ID: {}", notificationId);
                throw new ApplicationException("Notification not found with ID: " + notificationId);
            }
            
            Notification notification = notificationOpt.get();
            
            // Verificar si la notificación puede ser reintentada
            if (!notification.canRetry(maxRetries)) {
                log.warn("retry - Notification has reached maximum retry attempts: {}", notification.getRetryCount());
                throw new ApplicationException("Notification has reached maximum retry attempts: " + notification.getRetryCount());
            }
            
            // Verificar que la notificación esté en estado fallido
            if (notification.getStatus() != EventStatusType.FAILED) {
                log.warn("retry - Cannot retry notification that is not in FAILED state. Current status: {}", notification.getStatus());
                throw new ApplicationException("Cannot retry notification that is not in FAILED state. Current status: " + notification.getStatus());
            }
            
            // Incrementar el contador de reintentos
            notification.incrementRetryCount();
            log.debug("retry - Incremented retry count to: {}", notification.getRetryCount());
            
            // Actualizar el estado de la notificación a PENDING
            notification.markAsPending();
            log.debug("retry - Notification marked as PENDING");
            
            // Guardar la notificación actualizada
            log.debug("retry - Call notificationRepository.save");
            Notification savedNotification = notificationRepository.save(notification);            
            
            try {
                // Enviar la notificación
                log.debug("retry - Call notificationChannel.send");
                notificationChannel.send(savedNotification);
                
                // Actualizar el estado de la notificación a SENT
                log.debug("retry - Mark notification as sent");
                savedNotification.markAsSent();
                
                // Guardar la notificación actualizada
                Notification updatedNotification = notificationRepository.save(savedNotification);
                log.debug("retry - Notification saved with SENT status");
                
                // Guardar el evento en la tabla outbox
                log.debug("retry - Call saveRetriedEventToOutbox");
                saveRetriedEventToOutbox(updatedNotification, true);
                
                log.info("retry - End (notification sent successfully after retry)");
                return NotificationMapper.toResponse(updatedNotification);
            } catch (Exception e) {
                // En caso de excepción, marcar la notificación como fallida nuevamente
                String errorMessage = "Error retrying notification: " + e.getMessage();
                log.error("retry - Exception occurred during retry", e);
                savedNotification.markAsFailed(errorMessage);
                
                // Guardar evento de fallo en la tabla outbox
                log.debug("retry - Call saveRetriedEventToOutbox with failure");
                saveRetriedEventToOutbox(savedNotification, false);
                
                // Guardar la notificación con el estado de error
                Notification updatedNotification = notificationRepository.save(savedNotification);
                
                log.info("retry - End (retry failed)");
                return NotificationMapper.toResponse(updatedNotification);
            }        
        } catch (Exception e) {
            String errorMessage = "Error retrying notification: " + e.getMessage();
            log.error("retry - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
    
    /**
     * Guarda un evento de notificación reintentada en la tabla outbox.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * El evento será posteriormente procesado por un job dedicado que lo publicará en RabbitMQ.
     * 
     * @param notification La notificación reintentada
     * @param success Indica si el reintento fue exitoso o no
     */
    private void saveRetriedEventToOutbox(Notification notification, boolean success) {
        log.debug("saveRetriedEventToOutbox - Saving retried event to outbox for notification: {}", notification.getId());
        try {
            // Usar eventos existentes según el resultado del reintento
            Object event;
            
            if (success) {
                event = DomainEventFactory.createNotificationSentEvent(notification);
            } else {
                event = DomainEventFactory.createNotificationFailedEvent(notification, notification.getErrorMessage());
            }
            
            String payload = serialization.serialize(event);
            
            TransactionalOutbox outboxEvent = TransactionalOutbox.builder()
                .withId(UUID.randomUUID())
                .withStatusType(EventStatusType.PENDING)
                .withAggregateId(notification.getId().toString())
                .withPayload(payload)
                .withCreatedAt(LocalDateTime.now())
                .withProcessed(false)
                .withProcessedAt(null)
                .withRetryCount(0)
                .withMessage("")
                .withLastRetryAt(null)
                .withEventType(success ? EventType.NOTIFICATION_SENT : EventType.NOTIFICATION_FAILED)
                .build();
            
            log.debug("saveRetriedEventToOutbox - Call outboxRepository.save");
            outboxRepository.save(outboxEvent);
            log.debug("saveRetriedEventToOutbox - Event saved successfully");
        } catch (Exception e) {
            // Registrar el error pero no lo propaga para no afectar el flujo principal
            log.error("saveRetriedEventToOutbox - Error saving notification retried event to outbox", e);
        }
    }
}
