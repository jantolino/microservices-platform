package edu.market.notification.application.usecase.event;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.UnsubscribeFromEventsCommand;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.port.input.event.UnsubscribeFromEventsUseCasePort;
import edu.market.notification.domain.model.EventSubscription;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.EventSubscriptionRepositoryPort;

import java.util.Optional;

/**
 * Implementación del caso de uso para desuscribirse de eventos de otros microservicios.
 */
public class UnsubscribeFromEventsUseCase implements UnsubscribeFromEventsUseCasePort {

    private final EventSubscriptionRepositoryPort eventSubscriptionRepository;
    private final LoggingPort log;

    public UnsubscribeFromEventsUseCase(EventSubscriptionRepositoryPort eventSubscriptionRepository, LoggingPort log) {
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.log = log;
    }

    @Override
    public boolean unsubscribe(UnsubscribeFromEventsCommand command, ClientContextCommand clientContext) {
        
        log.info("unsubscribe - Init (eventType={}, sourceService={})", command.eventType(), command.sourceService());
        
        try {
            // Verificar si existe la suscripción
            log.debug("unsubscribe - Checking if subscription exists");
            Optional<EventSubscription> subscriptionOpt = eventSubscriptionRepository
                .findByEventTypeAndSourceService(command.eventType(), command.sourceService());
            
            if (subscriptionOpt.isEmpty()) {
                log.info("unsubscribe - No subscription found for eventType={} and sourceService={}", 
                    command.eventType(), command.sourceService());
                return false; // No existe la suscripción, no se puede desuscribir
            }
            
            EventSubscription subscription = subscriptionOpt.get();
            
            // Si la suscripción está activa, desactivarla
            if (subscription.isActive()) {
                log.debug("unsubscribe - Deactivating subscription");
                subscription.setActive(false);
                eventSubscriptionRepository.save(subscription);
                log.info("unsubscribe - Subscription deactivated successfully");
            } else {
                log.info("unsubscribe - Subscription was already inactive");
            }
            
            log.info("unsubscribe - End");
            return true;
        } catch (Exception e) {
            log.error("unsubscribe - Error unsubscribing from event: {}", e.getMessage(), e);
            throw new ApplicationException("Error unsubscribing from event: " + e.getMessage(), e);
        }
    }
}
