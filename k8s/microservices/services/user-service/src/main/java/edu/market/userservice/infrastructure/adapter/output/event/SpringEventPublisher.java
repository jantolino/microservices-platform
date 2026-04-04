package edu.market.userservice.infrastructure.adapter.output.event;

import edu.market.userservice.domain.event.DomainEvent;
import edu.market.userservice.domain.port.output.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementación del puerto de salida para publicar eventos de dominio
 * utilizando el mecanismo de eventos de Spring
 */
@Component
public class SpringEventPublisher implements EventPublisherPort {
    
    private static final Logger log = LoggerFactory.getLogger(SpringEventPublisher.class);
    private final ApplicationEventPublisher eventPublisher;
    
    public SpringEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void publish(DomainEvent event) {
        log.info("Publicando evento de dominio: {} [{}]", 
                event.getClass().getSimpleName(), 
                event.getEventId());
        
        eventPublisher.publishEvent(event);
    }
}
