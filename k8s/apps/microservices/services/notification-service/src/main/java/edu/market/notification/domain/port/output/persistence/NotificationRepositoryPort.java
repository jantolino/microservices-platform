package edu.market.notification.domain.port.output.persistence;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.vo.NotificationFilterCriteriaVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de notificaciones.
 * Siguiendo el patrón de Ports and Adapters (Arquitectura Hexagonal).
 */
public interface NotificationRepositoryPort {
    
    /**
     * Guarda una notificación
     * @param notification Notificación a guardar
     * @return Notificación guardada
     */
    Notification save(Notification notification);
    
    /**
     * Busca una notificación por su ID
     * @param id ID de la notificación
     * @return Notificación encontrada o vacío
     */
    Optional<Notification> findById(UUID id);
    
    /**
     * Busca notificaciones por estado
     * @param status Estado de las notificaciones
     * @return Lista de notificaciones
     */
    List<Notification> findByStatus(EventStatusType status);
    
    /**
     * Busca notificaciones programadas que deben enviarse
     * @param now Fecha actual
     * @return Lista de notificaciones programadas listas para enviar
     */
    List<Notification> findScheduledNotificationsReadyToSend(LocalDateTime now);
    
    /**
     * Busca notificaciones fallidas para reintentar
     * @param maxRetries Número máximo de reintentos
     * @return Lista de notificaciones fallidas que pueden reintentarse
     */
    List<Notification> findFailedNotificationsForRetry(int maxRetries);
    
    /**
     * Busca notificaciones por usuario
     * @param userId ID del usuario
     * @return Lista de notificaciones del usuario
     */
    List<Notification> findByUserId(UUID userId);
    
    /**
     * Busca notificaciones por usuario y estado
     * @param userId ID del usuario
     * @param status Estado de las notificaciones
     * @return Lista de notificaciones del usuario con el estado especificado
     */
    List<Notification> findByUserIdAndStatus(UUID userId, EventStatusType status);
    
    /**
     * Elimina una notificación
     * @param id ID de la notificación
     */
    void deleteById(UUID id);
    
    /**
     * Busca notificaciones según criterios de filtrado
     * @param criteria Criterios de filtrado encapsulados en un VO
     * @return Lista de notificaciones que cumplen los criterios
     */
    List<Notification> findByFilters(NotificationFilterCriteriaVO criteria);
    
    /**
     * Cuenta el número total de notificaciones que cumplen con los criterios de filtrado
     * @param criteria Criterios de filtrado encapsulados en un VO
     * @return Número total de notificaciones
     */
    int countByFilters(NotificationFilterCriteriaVO criteria);
}
