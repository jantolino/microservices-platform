package edu.market.cart.domain.port.output.persistence;

import edu.market.cart.domain.enums.SourceServiceType;
import edu.market.cart.domain.model.EventSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para operaciones con suscripciones a eventos.
 */
public interface EventSubscriptionRepositoryPort {
    
    /**
     * Guarda una suscripción a evento.
     *
     * @param eventSubscription La suscripción a guardar
     * @return La suscripción guardada
     */
    EventSubscription save(EventSubscription eventSubscription);
    
    /**
     * Busca una suscripción por su ID.
     *
     * @param id El ID de la suscripción
     * @return La suscripción si existe, o un Optional vacío
     */
    Optional<EventSubscription> findById(UUID id);
    
    /**
     * Busca una suscripción por tipo de evento y servicio fuente.
     *
     * @param eventType El tipo de evento
     * @param sourceService El servicio fuente
     * @return La suscripción si existe, o un Optional vacío
     */
    Optional<EventSubscription> findByEventTypeAndSourceService(String eventType, SourceServiceType sourceService);
    
    /**
     * Obtiene todas las suscripciones activas.
     *
     * @return Lista de suscripciones activas
     */
    List<EventSubscription> findAllActive();

    List<EventSubscription> findByEventType(String eventType);
    
    /**
     * Elimina una suscripción.
     *
     * @param id El ID de la suscripción a eliminar
     */
    void delete(UUID id);
}
