package edu.market.notification.application.usecase.event;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.SubscribeToEventsCommand;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.port.input.event.SubscribeToEventsUseCasePort;
import edu.market.notification.domain.model.EventSubscription;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.EventSubscriptionRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para suscribirse a eventos de otros microservicios.
 */
public class SubscribeToEventsUseCase implements SubscribeToEventsUseCasePort {

    private final EventSubscriptionRepositoryPort eventSubscriptionRepository;     ;
    private final LoggingPort log;

    public SubscribeToEventsUseCase(EventSubscriptionRepositoryPort eventSubscriptionRepository, LoggingPort log) {
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.log = log;
    }

    @Override
    public boolean subscribe(SubscribeToEventsCommand command, ClientContextCommand clientContext) {
        
        log.info("subscribe - Init (eventType={}, sourceService={})", command.eventType(), command.sourceService());
        
        try {
            // Verificar si ya existe una suscripción para este evento y servicio
            log.debug("subscribe - Call eventSubscriptionRepository.findByEventTypeAndSourceService");
            Optional<EventSubscription> existingSubscription = eventSubscriptionRepository
                .findByEventTypeAndSourceService(command.eventType(), command.sourceService());
            
            if (existingSubscription.isPresent()) {
                log.info("subscribe - Subscription already exists for eventType={} and sourceService={}", 
                    command.eventType(), command.sourceService());
                return true; // Ya existe, consideramos que la operación fue exitosa
            }
            
            // Crear nueva suscripción
            log.debug("subscribe - Creating new event subscription");
            EventSubscription subscription = EventSubscription.builder()
                .withId(UUID.randomUUID())
                .withEventType(command.eventType())
                .withSourceService(command.sourceService())
                .withActive(true)
                .withCreatedAt(LocalDateTime.now())
                .withUpdatedAt(LocalDateTime.now())
                .build();
            
            // Guardar la suscripción
            log.debug("subscribe - Call eventSubscriptionRepository.save");
            eventSubscriptionRepository.save(subscription);
            
            log.info("subscribe - End");
            return true;
        } catch (Exception e) {
            log.error("subscribe - Error subscribing to event: %s: ", e, e.getMessage());
            throw new ApplicationException("Error subscribing to event: " + e.getMessage(), e);
        }
    }
}
