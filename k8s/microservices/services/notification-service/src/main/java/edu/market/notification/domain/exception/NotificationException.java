package edu.market.notification.domain.exception;

/**
 * Excepción específica para problemas relacionados con notificaciones.
 * Se utiliza cuando ocurren errores en la creación, procesamiento o gestión de notificaciones.
 * 
 * Patrón de diseño: Exception Hierarchy - Implementa una jerarquía de excepciones específicas del dominio
 * extendiendo de DomainException para proporcionar manejo de errores especializado.
 */
public class NotificationException extends DomainException {
    
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
