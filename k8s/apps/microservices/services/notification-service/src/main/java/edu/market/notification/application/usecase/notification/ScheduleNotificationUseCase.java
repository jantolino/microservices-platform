package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.notification.ScheduleNotificationUseCasePort;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.domain.port.output.service.SerializationServicePort;
import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.domain.event.DomainEventFactory;
import edu.market.notification.domain.event.NotificationScheduledEvent;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del caso de uso para programar notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 * 
 * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
 * entre la base de datos y el sistema de mensajería.
 */
public class ScheduleNotificationUseCase implements ScheduleNotificationUseCasePort {

    private final NotificationRepositoryPort notificationRepository;
    private final TransactionalOutboxRepositoryPort outboxRepository;
    private final LoggingPort log;
    private final SerializationServicePort serialization;

    public ScheduleNotificationUseCase(
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
     * Programa una notificación para ser enviada en una fecha y hora específica.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * 
     * @param command Value Object con los datos de la notificación a programar
     * @return Value Object con la información de la notificación programada
     */
    @Override
    public NotificationResponse schedule(NotificationCommand command, ClientContextCommand clientContext) {
        
        log.info("schedule - Init for notification command");
        
        LocalDateTime scheduledTime = command.scheduledFor();        
        
        try {
                       
            // Convertir el comando a entidad de dominio
            log.debug("schedule - Call NotificationInputMapper.toEntity");
            Notification notification = NotificationMapper.toEntity(command);            
            
            // Establecer la hora programada en la notificación
            log.debug("schedule - Notification scheduled for: {}", scheduledTime);
            notification.schedule(scheduledTime);
            
            // Guardar la notificación en el repositorio
            log.debug("schedule - Call notificationRepository.save");
            Notification savedNotification = notificationRepository.save(notification);            
            
            // Guardar evento de programación en la tabla outbox (patrón Transactional Outbox)
            log.debug("schedule - Call saveScheduledEventToOutbox");
            saveScheduledEventToOutbox(savedNotification);
            
            log.info("schedule - End (notification scheduled successfully)");
            return NotificationMapper.toResponse(savedNotification);
        } catch (Exception e) {
            String errorMessage = "Error scheduling notification: " + e.getMessage();
            log.error("schedule - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
    
    /**
     * Guarda un evento de notificación programada en la tabla outbox.
     * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
     * entre la base de datos y el sistema de mensajería.
     * El evento será posteriormente procesado por un job dedicado que lo publicará en RabbitMQ.
     * 
     * @param notification La notificación programada
     */
    private void saveScheduledEventToOutbox(Notification notification) {
        log.debug("saveScheduledEventToOutbox - Saving scheduled event to outbox for notification: {}", notification.getId());
        try {
            NotificationScheduledEvent event = DomainEventFactory.createNotificationScheduledEvent(notification, notification.getScheduledFor());
            String payload = serialization.serialize(event);
            
            TransactionalOutbox outboxEvent = TransactionalOutbox.builder()
                .withId(UUID.randomUUID())
                .withStatusType(EventStatusType.SCHEDULED)
                .withAggregateId(notification.getId().toString())
                .withPayload(payload)
                .withCreatedAt(LocalDateTime.now())
                .withProcessed(false)
                .withProcessedAt(null)
                .withRetryCount(0)
                .withMessage("")
                .withLastRetryAt(null)
                .withEventType(EventType.NOTIFICATION_SCHEDULED)
                .build();
            
            log.debug("saveScheduledEventToOutbox - Call outboxRepository.save");
            outboxRepository.save(outboxEvent);
            // Las métricas técnicas (latencia, throughput) son manejadas automáticamente por el aspecto MetricsCollectionAspect
            log.debug("saveScheduledEventToOutbox - Event saved successfully");
        } catch (Exception e) {
            // Registrar el error pero no lo propaga para no afectar el flujo principal
            log.error("saveScheduledEventToOutbox - Error saving notification scheduled event to outbox", e);
        }
    }
}
