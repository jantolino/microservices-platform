package edu.market.notification.domain.port.output.persistence;

import edu.market.notification.domain.model.NotificationAudit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de auditoría de notificaciones.
 * Implementa el patrón de Audit Logging para observabilidad.
 */
public interface NotificationAuditRepositoryPort {
    
    /**
     * Guarda un registro de auditoría
     * @param audit Registro de auditoría a guardar
     * @return Registro guardado
     */
    NotificationAudit save(NotificationAudit audit);
    
    /**
     * Busca un registro de auditoría por su ID
     * @param id ID del registro
     * @return Registro encontrado o vacío
     */
    Optional<NotificationAudit> findById(UUID id);
    
    /**
     * Busca registros de auditoría por ID de notificación
     * @param notificationId ID de la notificación
     * @return Lista de registros de auditoría
     */
    List<NotificationAudit> findByNotificationId(UUID notificationId);
    
    /**
     * Busca registros de auditoría por ID de usuario
     * @param userId ID del usuario
     * @return Lista de registros de auditoría
     */
    List<NotificationAudit> findByUserId(UUID userId);
    
    /**
     * Busca registros de auditoría por acción
     * @param action Acción realizada
     * @return Lista de registros de auditoría
     */
    List<NotificationAudit> findByAction(String action);
    
    /**
     * Busca registros de auditoría por rango de fechas
     * @param from Fecha de inicio
     * @param to Fecha de fin
     * @return Lista de registros de auditoría
     */
    List<NotificationAudit> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
