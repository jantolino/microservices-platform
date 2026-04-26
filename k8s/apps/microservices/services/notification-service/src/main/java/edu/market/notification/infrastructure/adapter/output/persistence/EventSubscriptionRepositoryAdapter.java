package edu.market.notification.infrastructure.adapter.output.persistence;

import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.model.EventSubscription;
import edu.market.notification.domain.port.output.persistence.EventSubscriptionRepositoryPort;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.EventSubscriptionEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.mapper.EventSubscriptionOutputMapper;
import edu.market.notification.infrastructure.adapter.output.persistence.repository.JpaEventSubscriptionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para suscripciones a eventos que implementa el puerto de repositorio del dominio.
 * Siguiendo el patrón de Ports and Adapters (Arquitectura Hexagonal).
 */
@Component
@RequiredArgsConstructor
public class EventSubscriptionRepositoryAdapter implements EventSubscriptionRepositoryPort {

    private final JpaEventSubscriptionRepository eventSubscriptionRepository;
    private final EventSubscriptionOutputMapper eventSubscriptionOutputMapper;

    @Override
    @Transactional
    public EventSubscription save(EventSubscription eventSubscription) {
        EventSubscriptionEntity entity = eventSubscriptionOutputMapper.toEntity(eventSubscription);
        EventSubscriptionEntity savedEntity = eventSubscriptionRepository.save(entity);
        return eventSubscriptionOutputMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventSubscription> findById(UUID id) {
        return eventSubscriptionRepository.findByUuid(id)
                .map(eventSubscriptionOutputMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventSubscription> findByEventTypeAndSourceService(String eventType, SourceServiceType sourceService) {
        return eventSubscriptionRepository.findByEventTypeAndSourceService(eventType, sourceService)
                .map(eventSubscriptionOutputMapper::toDomain);
    }   

    @Override
    @Transactional(readOnly = true)
    public List<EventSubscription> findByEventType(String eventType) {
        return eventSubscriptionRepository.findByEventType(eventType).stream()
                .map(eventSubscriptionOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSubscription> findAllActive() {
        return eventSubscriptionRepository.findByActiveTrue().stream()
                .map(eventSubscriptionOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        eventSubscriptionRepository.deleteByUuid(id);
    }
}
