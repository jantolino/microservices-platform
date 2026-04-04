package edu.market.notification.domain.exception;

/**
 * Excepción para problemas relacionados con la entrega de notificaciones.
 * Se utiliza cuando hay errores en el proceso de envío o entrega de notificaciones.
 */
public class DeliveryException extends DomainException {
    
    public DeliveryException(String message) {
        super(message);
    }
    
    public DeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
