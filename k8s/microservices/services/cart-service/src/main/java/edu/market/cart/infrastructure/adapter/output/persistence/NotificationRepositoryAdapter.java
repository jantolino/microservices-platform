package edu.market.notification.infrastructure.adapter.output.persistence;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.vo.NotificationFilterCriteriaVO;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.mapper.NotificationOutputMapper;
import edu.market.notification.infrastructure.adapter.output.persistence.repository.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador para el repositorio de notificaciones
 */
@Component
@RequiredArgsConstructor
@Transactional
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {    

    private final JpaNotificationRepository notificationRepository;
    private final NotificationOutputMapper notificationOutputMapper;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        NotificationEntity entity = notificationOutputMapper.toEntity(notification);
        NotificationEntity savedEntity = notificationRepository.save(entity);
        return notificationOutputMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findByUuid(id)
                .map(notificationOutputMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByStatus(EventStatusType status) {
        return notificationRepository.findByStatus(status).stream()
                .map(notificationOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUserId(UUID userId) {
        return notificationRepository.findByRecipientUserId(userId).stream()
                .map(notificationOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUserIdAndStatus(UUID userId, EventStatusType status) {
        return notificationRepository.findByRecipientUserIdAndStatus(userId, status).stream()
                .map(notificationOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findScheduledNotificationsReadyToSend(LocalDateTime now) {
        return notificationRepository.findScheduledNotificationsReadyToSend(now).stream()
                .map(notificationOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findFailedNotificationsForRetry(int maxRetries) {
        return notificationRepository.findFailedNotificationsForRetry(maxRetries).stream()
                .map(notificationOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        notificationRepository.deleteByUuid(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByFilters(NotificationFilterCriteriaVO criteria) {
        // Configurar paginación
        PageRequest pageable = criteria.limit() > 0 ?
            PageRequest.of(criteria.offset() / criteria.limit(), criteria.limit()) :
            PageRequest.of(0, Integer.MAX_VALUE);
        
        // Ejecutar consulta con parámetros directamente del record
        List<NotificationEntity> entities = notificationRepository.findByFilters(
            criteria.userId(),
            criteria.notificationType(),
            criteria.status(),
            criteria.startDate(),
            criteria.endDate(),
            pageable
        );
        
        // Convertir entidades a objetos de dominio
        return entities.stream()
                .map(notificationOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int countByFilters(NotificationFilterCriteriaVO criteria) {
        // Ejecutar consulta de conteo con parámetros directamente del record
        return notificationRepository.countByFilters(
            criteria.userId(),
            criteria.notificationType(),
            criteria.startDate(),
            criteria.endDate()
        );
    }
}
