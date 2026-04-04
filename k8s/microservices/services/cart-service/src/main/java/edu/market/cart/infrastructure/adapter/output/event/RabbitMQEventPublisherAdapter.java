package edu.market.notification.infrastructure.adapter.output.event;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.domain.port.output.event.EventPublisherPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.domain.port.output.service.EventManagementServicePort;
import edu.market.notification.infrastructure.config.properties.TransactionalOutboxProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador para la publicación de eventos de dominio usando RabbitMQ.
 * Implementa el patrón Transactional Outbox para garantizar la consistencia
 * entre la transacción de dominio y la publicación de eventos.
 *
 * Patrones de diseño implementados:
 * - Adapter: Implementa el puerto de salida EventPublisherPort
 * - Transactional Outbox: Actualiza el estado de los eventos en una tabla transaccional outbox
 * - Batch Processing: Procesa y envía eventos en lotes para mayor eficiencia
 * - Dependency Injection: Utiliza inyección de dependencias para obtener los servicios necesarios
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQEventPublisherAdapter implements EventPublisherPort {

    private final TransactionalOutboxRepositoryPort transactionalOutboxRepository;    
    private final NotificationRepositoryPort notificationRepository;
    private final EventManagementServicePort eventManagementService;
    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final TransactionalOutboxProperties properties;    

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${notification-service.app.config.infrastructure.transactional-outbox.processing-interval}")
    public void publish() {
        
        log.info("publish - Init");
        
        log.debug("publish - Call isAvailable - Checking if RabbitMQ is available");
        if (!isAvailable()) {            
            return;
        }
        
        // Configuración de máximo de reintentos (desde properties)
        final int maxRetries = properties.getMaxRetries();
        
        log.debug("publish - Call findByProcessedFalseAndRetryCountLessThan with maxRetries={}", maxRetries);
        List<TransactionalOutbox> pendingEvents = transactionalOutboxRepository.findByProcessedFalseAndRetryCountLessThan(maxRetries);
        
        if (pendingEvents.isEmpty()) {
            log.info("publish - No pending outbox events found within retry limit");
            return;
        }
        
        // Filtrar eventos programados que están listos para procesar usando el servicio de dominio
        log.debug("publish - Filtering scheduled events ready to process");
        List<TransactionalOutbox> readyEvents = eventManagementService.filterEventsReadyToProcess(pendingEvents);
        
        if (readyEvents.isEmpty()) {
            log.info("publish - No events ready to process at this time");
            return;
        }
        
        // Ordenar eventos por prioridad usando el servicio de dominio
        log.debug("publish - Sorting events by priority");
        List<TransactionalOutbox> prioritizedEvents = eventManagementService.sortEventsByPriority(readyEvents);
        
        // Agrupar eventos por agregado para procesamiento en lote
        log.debug("publish - Grouping events by aggregate ID");
        // Utilizamos el agrupamiento para posible procesamiento por lotes de agregados relacionados
        Map<String, List<TransactionalOutbox>> eventsByAggregate = eventManagementService.groupRelatedEvents(prioritizedEvents);
        
        // Registrar información de agrupamiento para auditoría
        if (log.isDebugEnabled()) {
            eventsByAggregate.forEach((aggregateId, events) -> {
                log.debug("publish - Aggregate {} has {} related events", aggregateId, events.size());
            });
        }
        
        // Procesar cada grupo de eventos en lote por tipo de estado
        log.debug("publish - Processing events by type");
        Map<EventStatusType, List<TransactionalOutbox>> eventsByType = prioritizedEvents.stream()
            .collect(Collectors.groupingBy(TransactionalOutbox::getStatusType));
        
        // Procesar cada grupo de eventos en lote
        log.debug("publish - Call processEventBatch");
        eventsByType.forEach(this::processEventBatch);
        
        // Verificar si hay eventos que han excedido el límite de reintentos
        log.debug("publish - Call findByProcessedFalseAndRetryCountGreaterThanEqual with maxRetries={}", maxRetries);
        List<TransactionalOutbox> failedEvents = transactionalOutboxRepository.findByProcessedFalseAndRetryCountGreaterThanEqual(maxRetries);
        
        if (!failedEvents.isEmpty()) {
            log.warn("publish - Found {} events that exceeded retry limit", failedEvents.size());
            log.debug("publish - Call handleMaxRetriesExceeded");
            handleMaxRetriesExceeded(failedEvents);
        }
        
        log.info("publish - End");
    }

    /**
     * Verifica si el publicador de eventos está disponible comprobando
     * la conexión con RabbitMQ.
     *
     * @return true si RabbitMQ está disponible
     */
    @Override
    public boolean isAvailable() {
        
        log.info("isAvailable - Init");

        log.info("isAvailable - Checking connection");
        try {
            log.info("isAvailable - End: Connection is open");
            return connectionFactory.createConnection().isOpen();
        } catch (Exception e) {
            log.warn("isAvailable - Error: RabbitMQ connection is not available: {}", e.getMessage());
            return false;
        }
    }    

    @PostConstruct
    @Override
    public void publishCallback() {
        // Configurar callback de confirmación
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            
            if (correlationData != null && correlationData.getId() != null) {
                
                try {
                    UUID eventId = UUID.fromString(correlationData.getId());
                    Optional<TransactionalOutbox> event = transactionalOutboxRepository.findByUuid(eventId);
                    
                    if (ack) {                        
                        
                        if (event.isPresent()) {
                            log.debug("publishCallback - Event marked as processed: {}", eventId);
                            event.get().markAsProcessed();
                            transactionalOutboxRepository.save(event.get());
                            
                            // Actualizar la notificación asociada                            
                            UUID notificationId = eventManagementService.extractEventFromPayload(
                                event.get().getPayload(), event.get().getEventType());
                                
                            if (notificationId != null) {
                                notificationRepository.findById(notificationId).ifPresent(notification -> {
                                    // Actualizar el estado de la notificación a enviado
                                    notification.markAsSent();
                                    notificationRepository.save(notification);
                                    log.debug("publishCallback - Notification {} marked as sent", notificationId);
                                });                                
                            }
                            
                        } else {
                            log.warn("publishCallback - Failed to mark event as processed: {} (event not found)", eventId);
                        }
                        
                    } else {
                        
                        if (event.isPresent()) {
                            
                            log.debug("publishCallback - Event marked as failed: {}", eventId);
                            event.get().markAsFailed(cause);
                            transactionalOutboxRepository.save(event.get());
                            
                            // Actualizar la notificación asociada                            
                            UUID notificationId = eventManagementService.extractEventFromPayload(
                                event.get().getPayload(), event.get().getEventType());
                                
                            if (notificationId != null) {
                                notificationRepository.findById(notificationId).ifPresent(notification -> {
                                    // Actualizar el estado de la notificación a enviado
                                    notification.markAsFailed(cause);
                                    notificationRepository.save(notification);
                                    log.debug("publishCallback - Notification {} marked as failed", notificationId);
                                });
                            }
                            
                        } else {
                            log.warn("publishCallback - Failed to mark event as failed: {} (event not found)", eventId);
                        }
                    }
                } catch (Exception e) {
                    log.error("publishCallback - Error: {}", e.getMessage(), e);
                }
            }
        });
        
        // Configurar callback de retorno para mensajes no enrutados
        this.rabbitTemplate.setReturnsCallback(returned -> {
            String correlationId = returned.getMessage().getMessageProperties().getCorrelationId();
            Optional<TransactionalOutbox> event = transactionalOutboxRepository.findByUuid(UUID.fromString(correlationId));
            
            try {
                if (event.isPresent()) {
                
                    String errorMessage = String.format("Message routing failed: exchange=%s, routingKey=%s, replyCode=%d, replyText=%s", 
                            returned.getExchange(), returned.getRoutingKey(), 
                            returned.getReplyCode(), returned.getReplyText());
                    
                    // Actualizar el mensaje de error en el outbox
                    event.get().markAsFailed(errorMessage);
                    transactionalOutboxRepository.save(event.get());
                    
                    log.error(errorMessage);                   
                    
                
                } else {
                    log.error("publishCallback - Returned message without correlation ID: exchange={}, routingKey={}", 
                            returned.getExchange(), returned.getRoutingKey());
                }
            } catch (Exception e) {
                log.error("publishCallback - Error: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Maneja eventos que han excedido el número máximo de reintentos.
     * Actualiza el mensaje de error y registra una advertencia para cada evento.
     * Marca los eventos como cancelados para evitar más reintentos.
     * 
     * @param failedEvents Lista de eventos que han excedido el límite de reintentos
     */
    private void handleMaxRetriesExceeded(List<TransactionalOutbox> failedEvents) {
        log.warn("handleMaxRetriesExceeded - Processing {} events that exceeded retry limit", failedEvents.size());
        
        // Procesar cada evento que ha excedido el límite de reintentos
        for (TransactionalOutbox event : failedEvents) {
            
            // Verificar la política de reintentos usando el servicio de dominio
            boolean shouldRetry = eventManagementService.shouldRetryEvent(event, properties.getMaxRetries());

            if (!shouldRetry) {                
            
                try {
                    // Crear mensaje de error para indicar que se excedió el límite de reintentos
                    String errorMessage = String.format("Event processing failed after %d attempts", event.getRetryCount());
                    
                    // Usar el método de dominio para marcar como cancelado
                    event.markAsCancelled(errorMessage);
                    
                    // Guardar el evento actualizado
                    transactionalOutboxRepository.save(event);
                    
                    // Extraer ID de notificación del payload usando el servicio de dominio
                    UUID notificationId = eventManagementService.extractEventFromPayload(event.getPayload(), event.getEventType());
                    
                    // Actualizar la notificación asociada si existe                    
                    if (notificationId != null) {
                        // Buscar la notificación asociada y actualizar su estado
                        notificationRepository.findById(notificationId).ifPresent(notification -> {                            
                            notification.markAsFailed(errorMessage);                            
                            notificationRepository.save(notification);
                        });
                    }
                    
                    log.warn("Event {} exceeded retry limit after {} attempts. Last error: {}", 
                            event.getId(), event.getRetryCount(), event.getMessage());
                    
                } catch (Exception e) {
                    log.error("Error handling failed event {}: {}", event.getId(), e.getMessage(), e);
                }
            }

        }
    }

    /**
     * Procesa un lote de eventos del mismo tipo usando RabbitTemplate.
     * 
     * @param statusType Tipo de estado de notificación (usado como routing key)
     * @param events Lista de eventos a procesar en lote
     */
    private void processEventBatch(EventStatusType statusType, List<TransactionalOutbox> events) {
        try {
            String routingKey = statusType.name().toLowerCase();
            log.debug("processEventBatch - Processing batch of {} events with routing key: {}", events.size(), routingKey);
            
            // Enviar eventos en lote usando transacciones para garantizar consistencia
            for (TransactionalOutbox event : events) {                
                // Crear correlación con ID del evento
                CorrelationData correlationData = new CorrelationData(event.getId().toString());
                event.incrementRetryCount();
                
                transactionalOutboxRepository.save(event);
                
                // Enviar mensaje con correlación
                rabbitTemplate.convertAndSend(
                    properties.getExchange(), 
                    routingKey, 
                    event.getPayload(),
                    correlationData
                );
            }
            
            log.debug("processEventBatch - Successfully sent batch of {} events with routing key: {}", events.size(), routingKey);
        } catch (Exception e) {
            log.error("processEventBatch - Error processing batch of events with routing key {}: {}", 
                    statusType.name().toLowerCase(), e.getMessage(), e);            
        }
    }
    
}
