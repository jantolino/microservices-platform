package edu.market.notification.infrastructure.adapter.output.persistence.repository;

import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.EventSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad EventSubscriptionEntity.
 */
@Repository
public interface JpaEventSubscriptionRepository extends JpaRepository<EventSubscriptionEntity, UUID> {
    
    /**
     * Busca una suscripción a evento por su UUID
     * @param uuid UUID de la suscripción
     * @return Suscripción encontrada o vacío
     */
    Optional<EventSubscriptionEntity> findByUuid(UUID uuid);
    
    /**
     * Busca suscripciones a eventos por tipo de evento
     * @param eventType Tipo de evento
     * @return Lista de suscripciones
     */
    List<EventSubscriptionEntity> findByEventType(String eventType);

    /**
     * Busca una suscripción a evento por tipo de evento y servicio fuente
     * @param eventType Tipo de evento
     * @param sourceService Servicio fuente
     * @return Suscripción encontrada o vacío
     */
    Optional<EventSubscriptionEntity> findByEventTypeAndSourceService(String eventType, SourceServiceType sourceService);
    
    /**
     * Busca suscripciones a eventos activas
     * @return Lista de suscripciones activas
     */
    List<EventSubscriptionEntity> findByActiveTrue();

    /**
     * Elimina una suscripción a evento por su UUID
     * @param uuid UUID de la suscripción
     */
    void deleteByUuid(UUID uuid);
}
