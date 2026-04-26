package edu.market.notification.domain.exception;

/**
 * Excepción para problemas relacionados con las preferencias de usuario.
 * Se utiliza cuando hay errores en la gestión de preferencias de notificación.
 * 
 * Patrones de diseño implementados:
 * - Exception Hierarchy: Forma parte de una jerarquía de excepciones de dominio, extendiendo DomainException
 * - Domain-Specific Exception: Encapsula errores específicos del contexto de preferencias de usuario
 * - Informational Exception: Proporciona información detallada sobre el error ocurrido
 * - Chain of Responsibility: Permite propagar excepciones a través de las capas
 */
public class UserPreferenceException extends DomainException {
    
    public UserPreferenceException(String message) {
        super(message);
    }
    
    public UserPreferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
