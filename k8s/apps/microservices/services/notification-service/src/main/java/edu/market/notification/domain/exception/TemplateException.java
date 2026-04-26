package edu.market.notification.domain.exception;

/**
 * Excepción para problemas relacionados con plantillas de notificación.
 * Se utiliza cuando hay errores en la creación, actualización o procesamiento de plantillas.
 * 
 * Patrones de diseño implementados:
 * - Exception Hierarchy: Forma parte de una jerarquía de excepciones de dominio, extendiendo DomainException
 * - Domain-Specific Exception: Encapsula errores específicos del contexto de plantillas
 * - Informational Exception: Proporciona información detallada sobre el error ocurrido
 * - Chain of Responsibility: Permite propagar excepciones a través de las capas de la aplicación
 */
public class TemplateException extends DomainException {
    
    public TemplateException(String message) {
        super(message);
    }
    
    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
