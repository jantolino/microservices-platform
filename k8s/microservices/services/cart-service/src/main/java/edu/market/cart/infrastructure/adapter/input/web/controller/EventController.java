package edu.market.notification.infrastructure.adapter.input.web.controller;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.ProcessDomainEventCommand;
import edu.market.notification.application.dto.request.SubscribeToEventsCommand;
import edu.market.notification.application.dto.request.UnsubscribeFromEventsCommand;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.port.input.event.ProcessDomainEventUseCasePort;
import edu.market.notification.application.port.input.event.SubscribeToEventsUseCasePort;
import edu.market.notification.application.port.input.event.UnsubscribeFromEventsUseCasePort;
import edu.market.notification.infrastructure.adapter.input.web.api.EventApi;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.ProcessEventRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.SubscribeToEventsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UnsubscribeFromEventsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.mapper.EventInputMapper;
import edu.market.notification.infrastructure.adapter.input.web.mapper.NotificationInputMapper;
import edu.market.notification.infrastructure.adapter.input.web.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que implementa la API de procesamiento de eventos de dominio.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController implements EventApi {

    private final ProcessDomainEventUseCasePort processDomainEventUseCase;
    private final SubscribeToEventsUseCasePort subscribeToEventsUseCase;
    private final UnsubscribeFromEventsUseCasePort unsubscribeFromEventsUseCase;
    private final EventInputMapper eventInputMapper;
    private final NotificationInputMapper notificationMapper;
    private final RequestContextUtil requestContextUtil;

    @Override
    public ResponseEntity<NotificationResponseDTO> processEvent(ProcessEventRequestDTO requestDTO) {
        
        log.info("processEvent - init");
        
        log.debug("processEvent - Call eventInputMapper.toProcessDomainEventCommand");
        ProcessDomainEventCommand command = eventInputMapper.toProcessDomainEventCommand(requestDTO);
        
        log.debug("processEvent - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(requestDTO.getSource());
                
        log.debug("processEvent - Call processDomainEventUseCase.process");
        NotificationResponse response = processDomainEventUseCase.process(command, clientContext);
        
        log.debug("processEvent - Convert response to DTO");
        NotificationResponseDTO responseDTO = notificationMapper.toNotificationResponseDTO(response);
        
        log.debug("processEvent - Create response entity");
        ResponseEntity<NotificationResponseDTO> result = ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(responseDTO);
        
        log.info("processEvent - end");
        return result;
    }

    @Override
    public ResponseEntity<Boolean> subscribeToEvents(SubscribeToEventsRequestDTO requestDTO) {
        
        log.info("subscribeToEvents - init");
        
        log.debug("subscribeToEvents - Call eventInputMapper.toSubscribeToEventsCommand");
        SubscribeToEventsCommand command = eventInputMapper.toSubscribeToEventsCommand(requestDTO);
        
        log.debug("subscribeToEvents - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext("system");
                
        log.debug("subscribeToEvents - Call subscribeToEventsUseCase.subscribe");
        boolean subscribed = subscribeToEventsUseCase.subscribe(command, clientContext);
        
        log.debug("subscribeToEvents - Create response entity");
        ResponseEntity<Boolean> result = ResponseEntity.ok(subscribed);
        
        log.info("subscribeToEvents - end");
        return result;
    }

    @Override
    public ResponseEntity<Boolean> unsubscribeFromEvents(UnsubscribeFromEventsRequestDTO requestDTO) {
        
        log.info("unsubscribeFromEvents - init");
        
        log.debug("unsubscribeFromEvents - Call eventInputMapper.toUnsubscribeFromEventsCommand");
        UnsubscribeFromEventsCommand command = eventInputMapper.toUnsubscribeFromEventsCommand(requestDTO);
        
        log.debug("unsubscribeFromEvents - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext("system");
        
        log.debug("unsubscribeFromEvents - Call unsubscribeFromEventsUseCase.unsubscribe");
        boolean unsubscribed = unsubscribeFromEventsUseCase.unsubscribe(command, clientContext);
        
        log.debug("unsubscribeFromEvents - Create response entity");
        ResponseEntity<Boolean> result = ResponseEntity.ok(unsubscribed);
        
        log.info("unsubscribeFromEvents - end");
        return result;
    }
}
