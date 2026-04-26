package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.notification.CancelNotificationUseCasePort;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.domain.port.output.service.SerializationServicePort;
import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.domain.event.DomainEventFactory;
import edu.market.notification.domain.event.NotificationCanceledEvent;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.EventType;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

/**
 * Implementación del caso de uso para cancelar notificaciones programadas.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 * 
 * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
 * entre la base de datos y el sistema de mensajería.
 */
public class CancelNotificationUseCase implements CancelNotificationUseCasePort {

    private final NotificationRepositoryPort notificationRepository;
    private final TransactionalOutboxRepositoryPort outboxRepository;
    private final LoggingPort log;
    private final SerializationServicePort serialization;

    public CancelNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            TransactionalOutboxRepositoryPort outboxRepository,
            LoggingPort log,
            SerializationServicePort serialization) {
        this.notificationRepository = notificationRepository;
        this.outboxRepository = outboxRepository;
        this.log = log;
        this.serialization = serialization;
    }

    /**
     * Cancela una notificación programada.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * 
     * @param notificationId El identificador de la notificación a cancelar
     * @return Respuesta con la notificación actualizada con su estado final
     */
    @Override
    public NotificationResponse cancel(UUID notificationId, ClientContextCommand clientContext) {
        
        log.info("cancel - Init for notification ID: {}", notificationId);
        
        // Buscar la notificación por ID
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (notificationOpt.isEmpty()) {
            log.warn("cancel - Notification not found with ID: {}", notificationId);
            throw new ApplicationException("Notification not found with ID: " + notificationId);
        }
        
        Notification notification = notificationOpt.get();
        log.debug("cancel - Notification found with status: {}", notification.getStatus());        
        
        try {
            // Marcar la notificación como cancelada
            notification.cancel();
            log.debug("cancel - Notification marked as cancelled");
            
            // Guardar la notificación actualizada
            Notification savedNotification = notificationRepository.save(notification);
            log.debug("cancel - Notification saved with updated status: {}", savedNotification.getStatus());
            
            // Guardar evento de cancelación en la tabla outbox (patrón Transactional Outbox)
            log.debug("cancel - Call saveCancelledEventToOutbox");
            saveCancelledEventToOutbox(savedNotification);
            
            // Convertir la entidad de dominio a objeto de respuesta usando el mapper centralizado
            log.debug("cancel - Call NotificationInputMapper.toResponse");
            NotificationResponse response = NotificationMapper.toResponse(savedNotification);
            
            log.info("cancel - End (notification cancelled successfully)");
            return response;        
        } catch (Exception e) {
            String errorMessage = "Error cancelling notification: " + e.getMessage();
            log.error("cancel - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
    
    /**
     * Guarda un evento de notificación cancelada en la tabla outbox.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * El evento será posteriormente procesado por un job dedicado que lo publicará en RabbitMQ.
     * 
     * @param notification La notificación cancelada
     */
    private void saveCancelledEventToOutbox(Notification notification) {
        log.debug("saveCancelledEventToOutbox - Saving cancelled event to outbox for notification: {}", notification.getId());
        try {
            NotificationCanceledEvent event = DomainEventFactory.createNotificationCanceledEvent(notification, "");
            String payload = serialization.serialize(event);
            
            TransactionalOutbox outboxEvent = TransactionalOutbox.builder()
                .withId(UUID.randomUUID())
                .withStatusType(EventStatusType.CANCELLED)
                .withAggregateId(notification.getId().toString())
                .withPayload(payload)
                .withCreatedAt(LocalDateTime.now())
                .withProcessed(false)
                .withProcessedAt(null)
                .withRetryCount(0)
                .withMessage("")
                .withLastRetryAt(null)
                .withEventType(EventType.NOTIFICATION_CANCELED)
                .build();
            
            log.debug("saveCancelledEventToOutbox - Call outboxRepository.save");
            outboxRepository.save(outboxEvent);
            // Las métricas técnicas (latencia, throughput) son manejadas automáticamente por el aspecto MetricsCollectionAspect
            log.debug("saveCancelledEventToOutbox - Event saved successfully");
        } catch (Exception e) {
            // Registrar el error pero no lo propaga para no afectar el flujo principal
            log.error("saveCancelledEventToOutbox - Error processing notification cancelled event", e);
        }
    }
    
}
