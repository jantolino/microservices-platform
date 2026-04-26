package edu.market.notification.infrastructure.adapter.output.persistence.repository;

import edu.market.notification.infrastructure.adapter.output.persistence.entity.TransactionalOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad TransactionalOutboxEntity.
 */
@Repository
public interface JpaTransactionalOutboxRepository extends JpaRepository<TransactionalOutboxEntity, UUID> {
    
    /**
     * Busca eventos no procesados
     * @return Lista de eventos no procesados
     */
    List<TransactionalOutboxEntity> findByProcessedFalse();
    
    /**
     * Busca eventos no procesados por tipo de evento
     * @param eventType Tipo de evento
     * @return Lista de eventos no procesados
     */
    List<TransactionalOutboxEntity> findByProcessedFalseAndEventType(String eventType);
    
    /**
     * Busca eventos no procesados con un número de reintentos menor que el máximo especificado
     * @param maxRetries Número máximo de reintentos
     * @return Lista de eventos no procesados con reintentos por debajo del límite
     */
    List<TransactionalOutboxEntity> findByProcessedFalseAndRetryCountLessThan(int maxRetries);
    
    /**
     * Busca eventos no procesados con un número de reintentos mayor o igual que el máximo especificado
     * @param maxRetries Número máximo de reintentos
     * @return Lista de eventos que han excedido el límite de reintentos
     */
    List<TransactionalOutboxEntity> findByProcessedFalseAndRetryCountGreaterThanEqual(int maxRetries);
        
    /**
     * Elimina eventos procesados antiguos
     * @param cutoffDate Fecha límite para considerar un evento como antiguo
     * @return Número de filas eliminadas
     */
    @Modifying
    @Query("DELETE FROM TransactionalOutboxEntity e WHERE e.processed = true AND e.processedAt < :cutoffDate")
    int deleteProcessedEventsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /*
     * Busca un evento por su ID.
     * 
     * @param uuid ID del evento a buscar
     * @return Optional con el evento si existe, vacío si no
     */
    Optional<TransactionalOutboxEntity> findByUuid(UUID uuid);
}
