package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationHistoryQuery;
import edu.market.notification.application.dto.response.NotificationHistoryResult;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.notification.GetNotificationHistoryUseCasePort;
import edu.market.notification.domain.vo.NotificationFilterCriteriaVO;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;

import java.util.List;

/**
 * Implementación del caso de uso para obtener el historial de notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 * 
 * Esta implementación utiliza Value Objects específicos para consultas (Query) y resultados (Result),
 * siguiendo el patrón CQRS y evitando exponer entidades de dominio fuera de la capa de aplicación.
 */
public class GetNotificationHistoryUseCase implements GetNotificationHistoryUseCasePort {

    private final NotificationRepositoryPort notificationRepository;
    private final LoggingPort log;

    public GetNotificationHistoryUseCase(
            NotificationRepositoryPort notificationRepository,
            LoggingPort log) {
        this.notificationRepository = notificationRepository;
        this.log = log;
    }

    /**
     * Recupera el historial de notificaciones según los criterios especificados.
     * 
     * @param query Value Object con los criterios de búsqueda
     * @return Un VO con la lista de notificaciones y metadatos de paginación
     */
    @Override
    public NotificationHistoryResult getHistory(NotificationHistoryQuery query, ClientContextCommand clientContext) {
        
        log.info("getHistory - Init");
        log.debug("getHistory - Query: {}", query.toString());
        
        try {
            // Usar el mapper para convertir el query VO a un filtro de criterios
            log.debug("getHistory - Call NotificationInputMapper.toFilterCriteria");
            NotificationFilterCriteriaVO filterCriteria = NotificationMapper.toFilterCriteria(query);
            
            // Delegar la búsqueda al repositorio
            log.debug("getHistory - Call notificationRepository.findByFilters");
            List<Notification> notifications = notificationRepository.findByFilters(filterCriteria);
            
            // Obtener el conteo total para la paginación
            log.debug("getHistory - Call notificationRepository.countByFilters");
            int totalCount = notificationRepository.countByFilters(filterCriteria);
            
            // Convertir las entidades de dominio a VOs de respuesta
            log.debug("getHistory - Call NotificationInputMapper.toHistoryResult");
            NotificationHistoryResult result = NotificationMapper.toHistoryResult(
                    notifications, totalCount, query.limit(), query.offset());
            
            log.info("getHistory - End. Found {} notifications of {} total", notifications.size(), totalCount);
            
            return result;
        } catch (Exception e) {
            String errorMessage = "Error retrieving notification history: " + e.getMessage();
            log.error("getHistory - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
