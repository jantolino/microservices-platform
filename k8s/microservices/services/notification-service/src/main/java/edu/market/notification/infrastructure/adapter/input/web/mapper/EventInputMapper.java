package edu.market.notification.infrastructure.adapter.input.web.mapper;

import edu.market.notification.application.dto.request.ProcessDomainEventCommand;
import edu.market.notification.application.dto.request.SubscribeToEventsCommand;
import edu.market.notification.application.dto.request.UnsubscribeFromEventsCommand;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.ProcessEventRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.SubscribeToEventsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UnsubscribeFromEventsRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Mapper para convertir DTOs de eventos de la capa web a comandos de la capa de aplicación.
 * Este mapper se encarga de la conversión entre objetos de la API REST y objetos de la capa de aplicación
 * para las operaciones relacionadas con eventos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventInputMapper {

    /**
     * Convierte un DTO de solicitud de procesamiento de evento a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public ProcessDomainEventCommand toProcessDomainEventCommand(ProcessEventRequestDTO requestDTO) {
        
        log.info("toProcessDomainEventCommand - init");
        
        if (requestDTO == null) {
            return null;
        }
        
        ProcessDomainEventCommand command = new ProcessDomainEventCommand(
            requestDTO.getEventId(),
            requestDTO.getEventType(),
            requestDTO.getSource(),
            requestDTO.getPayload(),
            requestDTO.getCorrelationId()
        );
        
        log.info("toProcessDomainEventCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de suscripción a eventos a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public SubscribeToEventsCommand toSubscribeToEventsCommand(SubscribeToEventsRequestDTO requestDTO) {
        
        log.info("toSubscribeToEventsCommand - init");
        
        if (requestDTO == null) {
            return null;
        }
        
        SubscribeToEventsCommand command = new SubscribeToEventsCommand(
            requestDTO.getEventType(),
            requestDTO.getSourceService()
        );
        
        log.info("toSubscribeToEventsCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de cancelación de suscripción a eventos a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public UnsubscribeFromEventsCommand toUnsubscribeFromEventsCommand(UnsubscribeFromEventsRequestDTO requestDTO) {
        
        log.info("toUnsubscribeFromEventsCommand - init");
        
        if (requestDTO == null) {
            return null;
        }
        
        UnsubscribeFromEventsCommand command = new UnsubscribeFromEventsCommand(
            requestDTO.getEventType(),
            requestDTO.getSourceService()
        );
        
        log.info("toUnsubscribeFromEventsCommand - end");
        return command;
    }
}
