package edu.market.notification.domain.exception;

/**
 * Excepción lanzada cuando una entidad o value object no cumple con las reglas de validación.
 * Se utiliza para validaciones generales de entidades y objetos de valor.
 */
public class InvalidEntityException extends DomainException {
    
    public InvalidEntityException(String message) {
        super(message);
    }
    
    public InvalidEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
