package edu.market.notification.application.exception;

/**
 * Excepción base para todas las excepciones de la capa de aplicación.
 * Siguiendo los principios de arquitectura hexagonal, cada capa debe tener
 * sus propias excepciones para mantener una clara separación de responsabilidades.
 */
public class ApplicationException extends RuntimeException {
    
    public ApplicationException(String message) {
        super(message);
    }
    
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
