package edu.market.notification.domain.enums;

/**
 * Representa los diferentes estados en los que puede estar una notificación.
 * Utilizado para el seguimiento del ciclo de vida de la notificación.
 */
public enum EventStatusType {
    PENDING,      // Notificación creada pero aún no procesada
    SCHEDULED,    // Notificación programada para envío futuro
    PROCESSING,   // En proceso de envío
    SENT,         // Enviada exitosamente
    FAILED,       // Falló el envío
    CANCELLED     // Cancelada antes de enviar
}
