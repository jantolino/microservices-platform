package edu.market.notification.domain.enums;

/**
 * Representa los diferentes canales por los que se puede enviar una notificación.
 * Parte del patrón Bulkhead para aislar los diferentes canales de notificación.
 */
public enum NotificationChannelType {
    EMAIL,
    SMS,
    PUSH,
    IN_APP,
    WEBHOOK
}
