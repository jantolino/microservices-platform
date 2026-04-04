package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.event.DomainEvent;

/**
 * Puerto de salida para publicar eventos de dominio
 */
public interface EventPublisherPort {
    
    /**
     * Publica un evento de dominio
     * 
     * @param event Evento a publicar
     */
    void publish(DomainEvent event);
}
