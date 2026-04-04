package edu.market.notification.infrastructure.adapter.output.persistence;

import edu.market.notification.domain.model.NotificationAudit;
import edu.market.notification.domain.port.output.persistence.NotificationAuditRepositoryPort;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationAuditEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.mapper.NotificationAuditOutputMapper;
import edu.market.notification.infrastructure.adapter.output.persistence.repository.JpaNotificationAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para auditoría de notificaciones que implementa el puerto de repositorio del dominio.
 * Siguiendo el patrón de Ports and Adapters (Arquitectura Hexagonal).
 */
@Component
@RequiredArgsConstructor
public class NotificationAuditRepositoryAdapter implements NotificationAuditRepositoryPort {

    private final JpaNotificationAuditRepository notificationAuditRepository;
    private final NotificationAuditOutputMapper notificationAuditOutputMapper;

    @Override
    @Transactional
    public NotificationAudit save(NotificationAudit audit) {
        NotificationAuditEntity entity = notificationAuditOutputMapper.toEntity(audit);
        NotificationAuditEntity savedEntity = notificationAuditRepository.save(entity);
        return notificationAuditOutputMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationAudit> findById(UUID id) {
        return notificationAuditRepository.findByUuid(id)
                .map(notificationAuditOutputMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationAudit> findByNotificationId(UUID notificationId) {
        return notificationAuditRepository.findByNotificationId(notificationId).stream()
                .map(notificationAuditOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationAudit> findByUserId(UUID userId) {
        return notificationAuditRepository.findByUserId(userId).stream()
                .map(notificationAuditOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationAudit> findByAction(String action) {
        return notificationAuditRepository.findByAction(action).stream()
                .map(notificationAuditOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationAudit> findByTimestampBetween(LocalDateTime from, LocalDateTime to) {
        return notificationAuditRepository.findByTimestampBetween(from, to).stream()
                .map(notificationAuditOutputMapper::toDomain)
                .collect(Collectors.toList());
    }
}
