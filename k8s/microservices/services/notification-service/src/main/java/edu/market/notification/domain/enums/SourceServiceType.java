package edu.market.notification.domain.enums;

/**
 * Enumera los diferentes servicios que pueden solicitar notificaciones.
 * Utilizado para la trazabilidad y auditoría del origen de las solicitudes.
 */
public enum SourceServiceType {
    USER_SERVICE,          // Servicio de gestión de usuarios
    ORDER_SERVICE,         // Servicio de pedidos
    PAYMENT_SERVICE,       // Servicio de pagos
    INVENTORY_SERVICE,     // Servicio de inventario
    SHIPPING_SERVICE,      // Servicio de envíos
    MARKETING_SERVICE,     // Servicio de marketing
    PRODUCT_SERVICE,       // Servicio de productos
    REVIEW_SERVICE,        // Servicio de reseñas
    CUSTOMER_SERVICE,      // Servicio de atención al cliente
    SYSTEM,                // Notificaciones generadas automáticamente por el sistema
    EXTERNAL_API,          // Integraciones con APIs externas
    SCHEDULER,             // Servicio de programación de tareas
    ADMIN_PORTAL,          // Portal de administración
    UNKNOWN;               // Origen desconocido o no especificado
    
    /**
     * Obtiene el tipo de servicio a partir de su nombre
     * @param name Nombre del servicio
     * @return SourceServiceType correspondiente o UNKNOWN si no existe
     */
    public static SourceServiceType fromString(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
