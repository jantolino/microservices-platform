package edu.market.notification.infrastructure.adapter.output.persistence.repository;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad NotificationEntity.
 */
@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    
    /**
     * Busca una notificación por su UUID
     * @param uuid UUID de la notificación
     * @return Notificación encontrada o vacío
     */
    Optional<NotificationEntity> findByUuid(UUID uuid);
    
    /**
     * Busca notificaciones por estado
     * @param status Estado de la notificación
     * @return Lista de notificaciones
     */
    List<NotificationEntity> findByStatus(EventStatusType status);
    
    /**
     * Busca notificaciones por destinatario
     * @param userId ID del usuario destinatario
     * @return Lista de notificaciones
     */
    List<NotificationEntity> findByRecipientUserId(UUID userId);
    
    /**
     * Busca notificaciones por destinatario y estado
     * @param userId ID del usuario destinatario
     * @param status Estado de la notificación
     * @return Lista de notificaciones
     */
    List<NotificationEntity> findByRecipientUserIdAndStatus(UUID userId, EventStatusType status);
    
    /**
     * Elimina una notificación por su UUID
     * @param uuid UUID de la notificación
     */
    void deleteByUuid(UUID uuid);
    
    /**
     * Busca notificaciones programadas listas para enviar
     * @param now Fecha y hora actual
     * @return Lista de notificaciones programadas listas para enviar
     */
    //@Query("SELECT n FROM NotificationEntity n WHERE n.status = 'SCHEDULED' AND n.scheduledFor <= :now")
    @Query("SELECT n FROM NotificationEntity n")
    //List<NotificationEntity> findScheduledNotificationsReadyToSend(@Param("now") LocalDateTime now);
    List<NotificationEntity> findScheduledNotificationsReadyToSend(LocalDateTime now);
    
    /**
     * Busca notificaciones fallidas para reintentar
     * @param maxRetries Número máximo de reintentos
     * @return Lista de notificaciones fallidas que pueden reintentarse
     */
    //@Query("SELECT n FROM NotificationEntity n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries")
    @Query("SELECT n FROM NotificationEntity n")
    //List<NotificationEntity> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);
    List<NotificationEntity> findFailedNotificationsForRetry(int maxRetries);
    
    /**
     * Busca notificaciones aplicando filtros dinámicos
     * @param userId ID del usuario (opcional)
     * @param notificationType Tipo de notificación (opcional)
     * @param status Estado de la notificación (opcional)
     * @param startDate Fecha de inicio (opcional)
     * @param endDate Fecha de fin (opcional)
     * @param pageable Paginación
     * @return Lista de notificaciones filtradas
     */
    /*@Query("SELECT n FROM NotificationEntity n "
           + "WHERE (:userId is null OR n.recipientUserId = :userId) "
           + "AND (:notificationType is null OR n.type = :notificationType) "
           + "AND (:status is null OR n.status = :status) "
           + "AND (:startDate is null OR n.createdAt >= :startDate) "
           + "AND (:endDate is null OR n.createdAt <= :endDate)")*/
    @Query("SELECT n FROM NotificationEntity n")
    List<NotificationEntity> findByFilters(
        @Param("userId") UUID userId,
        @Param("notificationType") String notificationType,
        @Param("status") String status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Cuenta notificaciones aplicando filtros dinámicos
     * @param userId ID del usuario (opcional)
     * @param notificationType Tipo de notificación (opcional)
     * @param status Estado de la notificación (opcional)
     * @param startDate Fecha de inicio (opcional)
     * @param endDate Fecha de fin (opcional)
     * @return Número de notificaciones que cumplen los filtros
     */
   /* @Query("SELECT COUNT(n) FROM NotificationEntity n "
           + "WHERE (:userId is null OR n.recipientUserId = :userId) "
           + "AND (:status is null OR n.status = :status) "
           + "AND (:startDate is null OR n.createdAt >= :startDate) "
           + "AND (:endDate is null OR n.createdAt <= :endDate)")*/
    @Query("SELECT n FROM NotificationEntity n")
    int countByFilters(
        @Param("userId") UUID userId,
        @Param("notificationType") String notificationType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}
