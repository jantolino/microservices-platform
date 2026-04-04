package edu.market.notification.application.port.input.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationHistoryQuery;
import edu.market.notification.application.dto.response.NotificationHistoryResult;

/**
 * Puerto de caso de uso para consultar el historial de notificaciones.
 * 
 * Este caso de uso permite recuperar el historial de notificaciones enviadas a los usuarios,
 * con soporte para filtrado por varios criterios.
 * 
 * Siguiendo los principios de arquitectura hexagonal y CQRS, este puerto utiliza Value Objects
 * específicos para consultas (Query) y resultados (Result), evitando exponer entidades de dominio
 * fuera de la capa de aplicación.
 */
public interface GetNotificationHistoryUseCasePort {
    
    /**
     * Recupera el historial de notificaciones según los criterios especificados.
     * 
     * @param query Value Object con los criterios de búsqueda
     * @return Un VO con la lista de notificaciones y metadatos de paginación
     */
    NotificationHistoryResult getHistory(NotificationHistoryQuery query, ClientContextCommand clientContext);
}
