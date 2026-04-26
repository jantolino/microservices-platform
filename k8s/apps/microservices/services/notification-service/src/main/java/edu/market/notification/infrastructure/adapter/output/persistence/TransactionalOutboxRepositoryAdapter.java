package edu.market.notification.infrastructure.adapter.output.persistence;

import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.TransactionalOutboxEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.mapper.TransactionalOutboxOutputMapper;
import edu.market.notification.infrastructure.adapter.output.persistence.repository.JpaTransactionalOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador para el puerto de repositorio de TransactionalOutbox.
 * Implementa las operaciones del patrón Outbox para garantizar la consistencia de eventos.
 */
@Component
@RequiredArgsConstructor
public class TransactionalOutboxRepositoryAdapter implements TransactionalOutboxRepositoryPort {

    private final JpaTransactionalOutboxRepository outboxRepository;
    private final TransactionalOutboxOutputMapper outboxMapper;

    @Override
    @Transactional
    public TransactionalOutbox save(TransactionalOutbox event) {
        TransactionalOutboxEntity entity = outboxMapper.toEntity(event);
        TransactionalOutboxEntity savedEntity = outboxRepository.save(entity);
        return outboxMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionalOutbox> findByProcessedFalse() {
        return outboxRepository.findByProcessedFalse().stream()
                .map(outboxMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionalOutbox> findByProcessedFalseAndEventType(String eventType) {
        return outboxRepository.findByProcessedFalseAndEventType(eventType).stream()
                .map(outboxMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransactionalOutbox> findByProcessedFalseAndRetryCountLessThan(int maxRetries) {
        return outboxRepository.findByProcessedFalseAndRetryCountLessThan(maxRetries).stream()
                .map(outboxMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransactionalOutbox> findByProcessedFalseAndRetryCountGreaterThanEqual(int maxRetries) {
        return outboxRepository.findByProcessedFalseAndRetryCountGreaterThanEqual(maxRetries).stream()
                .map(outboxMapper::toDomain)
                .collect(Collectors.toList());
    }    
    
    @Override
    @Transactional
    public int deleteProcessedEventsBefore(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(daysOld, ChronoUnit.DAYS);
        return outboxRepository.deleteProcessedEventsBefore(cutoffDate);
    }

    @Override
    public Optional<TransactionalOutbox> findByUuid(UUID uuid) {
        Optional<TransactionalOutboxEntity> entity = outboxRepository.findByUuid(uuid);
        return entity.map(outboxMapper::toDomain);
    }   
}
