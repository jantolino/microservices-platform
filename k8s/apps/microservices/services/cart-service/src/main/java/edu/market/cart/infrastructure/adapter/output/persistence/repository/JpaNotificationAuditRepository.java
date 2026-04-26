package edu.market.notification.infrastructure.adapter.output.persistence.repository;

import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad NotificationAuditEntity.
 */
@Repository
public interface JpaNotificationAuditRepository extends JpaRepository<NotificationAuditEntity, UUID> {
    
    /**
     * Busca un registro de auditoría por su UUID
     * @param uuid UUID del registro de auditoría
     * @return Registro de auditoría encontrado o vacío
     */
    Optional<NotificationAuditEntity> findByUuid(UUID uuid);
    
    /**
     * Busca registros de auditoría por ID de notificación
     * @param notificationId ID de la notificación
     * @return Lista de registros de auditoría
     */
    List<NotificationAuditEntity> findByNotificationId(UUID notificationId);
    
    /**
     * Busca registros de auditoría por ID de usuario
     * @param userId ID del usuario
     * @return Lista de registros de auditoría
     */
    List<NotificationAuditEntity> findByUserId(UUID userId);
    
    /**
     * Busca registros de auditoría por acción
     * @param action Acción realizada
     * @return Lista de registros de auditoría
     */
    List<NotificationAuditEntity> findByAction(String action);
    
    /**
     * Busca registros de auditoría por rango de fechas
     * @param from Fecha inicial
     * @param to Fecha final
     * @return Lista de registros de auditoría
     */
    List<NotificationAuditEntity> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
