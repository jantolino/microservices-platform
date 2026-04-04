package edu.market.cart.domain.exception;

/**
 * Excepción específica para errores relacionados con las suscripciones a eventos.
 * Se lanza cuando hay problemas con la validación o manipulación de suscripciones a eventos.
 */
public class EventSubscriptionException extends DomainException {
    
    public EventSubscriptionException(String message) {
        super(message);
    }
    
    public EventSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
