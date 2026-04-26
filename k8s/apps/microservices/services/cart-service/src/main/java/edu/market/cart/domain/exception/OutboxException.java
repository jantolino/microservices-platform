package edu.market.cart.domain.exception;

/**
 * Excepción específica para errores relacionados con el patrón Transactional Outbox.
 * Se lanza cuando hay problemas con la validación o manipulación de eventos en el outbox.
 */
public class OutboxException extends DomainException {
    
    public OutboxException(String message) {
        super(message);
    }
    
    public OutboxException(String message, Throwable cause) {
        super(message, cause);
    }
}
