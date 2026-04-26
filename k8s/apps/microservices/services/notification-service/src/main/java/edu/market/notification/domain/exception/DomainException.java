package edu.market.notification.domain.exception;

/**
 * Excepción base para todas las excepciones del dominio de notificaciones.
 * Proporciona una capa de abstracción para las excepciones específicas del dominio.
 */
public abstract class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
