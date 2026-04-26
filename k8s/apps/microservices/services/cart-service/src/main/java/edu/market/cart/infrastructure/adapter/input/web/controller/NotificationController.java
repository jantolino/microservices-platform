package edu.market.notification.infrastructure.adapter.input.web.controller;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.GenerateReportCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.request.NotificationHistoryQuery;
import edu.market.notification.application.dto.response.NotificationHistoryResult;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.dto.response.NotificationStatusResponse;
import edu.market.notification.application.port.input.notification.CancelNotificationUseCasePort;
import edu.market.notification.application.port.input.notification.CheckNotificationStatusUseCasePort;
import edu.market.notification.application.port.input.notification.GenerateNotificationReportUseCasePort;
import edu.market.notification.application.port.input.notification.GetNotificationHistoryUseCasePort;
import edu.market.notification.application.port.input.notification.RetryNotificationUseCasePort;
import edu.market.notification.application.port.input.notification.ScheduleNotificationUseCasePort;
import edu.market.notification.application.port.input.notification.SendNotificationUseCasePort;
import edu.market.notification.infrastructure.adapter.input.web.api.NotificationApi;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.GenerateReportRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.NotificationHistoryRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.NotificationRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationHistoryResultDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationStatusResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.mapper.NotificationInputMapper;
import edu.market.notification.infrastructure.adapter.input.web.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que implementa la API de gestión de notificaciones.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final ScheduleNotificationUseCasePort scheduleNotificationUseCase;
    private final CancelNotificationUseCasePort cancelNotificationUseCase;
    private final CheckNotificationStatusUseCasePort checkNotificationStatusUseCase;
    private final RetryNotificationUseCasePort retryNotificationUseCase;
    private final SendNotificationUseCasePort sendNotificationUseCase;
    private final GenerateNotificationReportUseCasePort generateReportUseCase;    
    private final GetNotificationHistoryUseCasePort getNotificationHistoryUseCase;
    private final NotificationInputMapper notificationMapper;
    private final RequestContextUtil requestContextUtil;

    @Override
    public ResponseEntity<NotificationResponseDTO> scheduleNotification(NotificationRequestDTO requestDTO) {
        
        log.info("scheduleNotification - init");
        
        log.debug("scheduleNotification - Call notificationMapper.toNotificationCommand");
        NotificationCommand command = notificationMapper.toNotificationCommand(requestDTO);
        
        log.debug("scheduleNotification - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(requestDTO.getRecipientId());
        
        log.debug("scheduleNotification - Call scheduleNotificationUseCase.schedule");
        NotificationResponse response = scheduleNotificationUseCase.schedule(command, clientContext);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        log.debug("scheduleNotification - Call notificationMapper.toNotificationResponseDTO");
        ResponseEntity<NotificationResponseDTO> result = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationMapper.toNotificationResponseDTO(response));
        
        log.info("scheduleNotification - end");
        return result;
    }

    @Override
    public ResponseEntity<NotificationResponseDTO> cancelNotification(String id) {
        
        log.info("cancelNotification - init");
        
        log.debug("cancelNotification - Parse id to UUID");
        UUID notificationId = UUID.fromString(id);
        
        log.debug("cancelNotification - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(id);
        
        log.debug("cancelNotification - Call cancelNotificationUseCase.cancel");
        NotificationResponse response = cancelNotificationUseCase.cancel(notificationId, clientContext);
        
        // Devolver el resultado
        ResponseEntity<NotificationResponseDTO> result = ResponseEntity.ok(notificationMapper.toNotificationResponseDTO(response));
        
        log.info("cancelNotification - end");
        return result;
    }

    @Override
    public ResponseEntity<NotificationStatusResponseDTO> checkNotificationStatus(String id) {
        
        log.info("checkNotificationStatus - init");
        
        log.debug("checkNotificationStatus - Parse id to UUID");
        UUID notificationId = UUID.fromString(id);
        
        log.debug("checkNotificationStatus - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(id);
        
        log.debug("checkNotificationStatus - Call checkNotificationStatusUseCase.check");
        NotificationStatusResponse response = checkNotificationStatusUseCase.check(notificationId, clientContext);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        log.debug("checkNotificationStatus - Call notificationMapper.toNotificationStatusResponseDTO");
        ResponseEntity<NotificationStatusResponseDTO> result = ResponseEntity.ok(
                notificationMapper.toNotificationStatusResponseDTO(response));
        
        log.info("checkNotificationStatus - end");
        return result;
    }

    @Override
    public ResponseEntity<NotificationResponseDTO> retryNotification(String id) {
        
        log.info("retryNotification - init");
        
        log.debug("retryNotification - Parse id to UUID");
        UUID notificationId = UUID.fromString(id);
        
        log.debug("retryNotification - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(id);
        
        log.debug("retryNotification - Call retryNotificationUseCase.retry");
        NotificationResponse response = retryNotificationUseCase.retry(notificationId, clientContext);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        log.debug("retryNotification - Call notificationMapper.toNotificationResponseDTO");
        ResponseEntity<NotificationResponseDTO> result = ResponseEntity.ok(
                notificationMapper.toNotificationResponseDTO(response));
        
        log.info("retryNotification - end");
        return result;
    }

    @Override
    public ResponseEntity<NotificationResponseDTO> sendNotification(NotificationRequestDTO requestDTO) {
        
        log.info("sendNotification - init");
        
        log.debug("sendNotification - Call notificationMapper.toNotificationCommand");
        NotificationCommand command = notificationMapper.toNotificationCommand(requestDTO);
        
        log.debug("sendNotification - Create ClientContextCommand");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(requestDTO.getRecipientId());
        
        log.debug("sendNotification - Call sendNotificationUseCase.send");
        NotificationResponse response = sendNotificationUseCase.send(command, clientContext);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        log.debug("sendNotification - Call notificationMapper.toNotificationResponseDTO");
        ResponseEntity<NotificationResponseDTO> result = ResponseEntity.ok(
                notificationMapper.toNotificationResponseDTO(response));
        
        log.info("sendNotification - end");
        return result;
    }

    @Override
    public ResponseEntity<byte[]> generateNotificationReport(GenerateReportRequestDTO requestDTO) {
        
        log.info("generateNotificationReport - init");
        
        log.debug("generateNotificationReport - Converting DTO to command via mapper");
        GenerateReportCommand command = notificationMapper.toGenerateReportCommand(requestDTO);
        
        log.debug("generateNotificationReport - Creating client context");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(requestDTO.getUserId().toString());
        
        log.debug("generateNotificationReport - Calling generateReport use case");
        byte[] reportBytes = generateReportUseCase.generateReport(command, clientContext);        
        
        ResponseEntity<byte[]> result = ResponseEntity.ok(reportBytes);
        
        log.info("generateNotificationReport - end");
        return result;
    }

    @Override
    public ResponseEntity<NotificationHistoryResultDTO> getNotificationHistory(NotificationHistoryRequestDTO requestDTO) {
        
        log.info("getNotificationHistory - init");
        
        log.debug("getNotificationHistory - Converting DTO to query via mapper");
        NotificationHistoryQuery query = notificationMapper.toNotificationHistoryQuery(requestDTO);
        
        log.debug("getNotificationHistory - Creating client context");
        ClientContextCommand clientContext = requestContextUtil.getClientContext(requestDTO.getUserId().toString());
        
        log.debug("getNotificationHistory - Calling getHistory use case");
        NotificationHistoryResult historyResult = getNotificationHistoryUseCase.getHistory(query, clientContext);
        
        log.debug("getNotificationHistory - Converting result to DTO via mapper");
        NotificationHistoryResultDTO resultDTO = notificationMapper.toNotificationHistoryResultDTO(historyResult);
        
        log.debug("getNotificationHistory - Creating response entity");
        ResponseEntity<NotificationHistoryResultDTO> result = ResponseEntity.ok(resultDTO);
        
        log.info("getNotificationHistory - end");
        return result;
    }
}
