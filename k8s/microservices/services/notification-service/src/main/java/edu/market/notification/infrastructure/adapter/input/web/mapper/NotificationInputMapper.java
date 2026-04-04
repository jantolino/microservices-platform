package edu.market.notification.infrastructure.adapter.input.web.mapper;

import edu.market.notification.application.dto.request.BatchNotificationCommand;
import edu.market.notification.application.dto.request.GenerateReportCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.request.NotificationHistoryQuery;
import edu.market.notification.application.dto.request.RetryNotificationCommand;
import edu.market.notification.application.dto.response.NotificationHistoryResult;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.dto.response.NotificationStatusResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.BatchNotificationRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.GenerateReportRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.NotificationHistoryRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.NotificationRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.RetryNotificationRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationHistoryResultDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationStatusResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir DTOs de notificaciones de la capa web a comandos/consultas de la capa de aplicación
 * y viceversa. Este mapper se encarga de la conversión entre objetos de la API REST y objetos de la capa 
 * de aplicación para las operaciones relacionadas con notificaciones.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationInputMapper {
    
    /**
     * Convierte un DTO de solicitud de notificación a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public NotificationCommand toNotificationCommand(NotificationRequestDTO requestDTO) {
        
        log.info("toNotificationCommand - init");
        
        NotificationCommand command = new NotificationCommand(
            requestDTO.getSubject(),
            requestDTO.getContent(),
            requestDTO.getRecipientId(),
            requestDTO.getRecipientEmail(),
            requestDTO.getChannels(),
            requestDTO.getPriority(),
            requestDTO.getScheduledFor(),
            requestDTO.getSourceService()
        );
        
        log.info("toNotificationCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de notificaciones por lotes a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public BatchNotificationCommand toBatchNotificationCommand(BatchNotificationRequestDTO requestDTO) {
        
        log.info("toBatchNotificationCommand - init");
        
        List<NotificationCommand> notificationCommands = requestDTO.getNotifications().stream()
            .map(this::toNotificationCommand)
            .collect(Collectors.toList());
            
        BatchNotificationCommand command = new BatchNotificationCommand(
            requestDTO.getBatchId(),
            notificationCommands,
            requestDTO.getSource(),
            requestDTO.getPriority(),
            requestDTO.isFailFast()
        );
        
        log.info("toBatchNotificationCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de reintento de notificación a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public RetryNotificationCommand toRetryNotificationCommand(RetryNotificationRequestDTO requestDTO) {
        
        log.info("toRetryNotificationCommand - init");
        
        RetryNotificationCommand command = new RetryNotificationCommand(
            requestDTO.getNotificationId()
        );
        
        log.info("toRetryNotificationCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de historial de notificaciones a una consulta de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Consulta para la capa de aplicación
     */
    public NotificationHistoryQuery toNotificationHistoryQuery(NotificationHistoryRequestDTO requestDTO) {
        
        log.info("toNotificationHistoryQuery - init");
        
        NotificationHistoryQuery query = new NotificationHistoryQuery(
            requestDTO.getUserId(),
            requestDTO.getNotificationType(),
            requestDTO.getStatus(),
            requestDTO.getStartDate(),
            requestDTO.getEndDate(),
            requestDTO.getLimit(),
            requestDTO.getOffset()
        );
        
        log.info("toNotificationHistoryQuery - end");
        return query;
    }
    
    /**
     * Convierte un DTO de solicitud de generación de reporte a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public GenerateReportCommand toGenerateReportCommand(GenerateReportRequestDTO requestDTO) {
        
        log.info("toGenerateReportCommand - init");
        
        GenerateReportCommand command = new GenerateReportCommand(
            requestDTO.getUserId(),
            requestDTO.getStartDate(),
            requestDTO.getEndDate(),
            requestDTO.getReportFormat()
        );
        
        log.info("toGenerateReportCommand - end");
        return command;
    }

    /**
     * Convierte un objeto NotificationResponse de la capa de aplicación a un DTO de respuesta.
     *
     * @param response Objeto de respuesta de la capa de aplicación
     * @return DTO de respuesta para la API
     */
    public NotificationResponseDTO toNotificationResponseDTO(NotificationResponse response) {
        
        log.info("toNotificationResponseDTO - init");
        
        if (response == null) {
            return null;
        }

        NotificationResponseDTO dto = new NotificationResponseDTO(
                response.id(),
                response.subject(),
                response.content(),
                response.recipientId(),
                response.recipientEmail(),
                response.channels(),
                response.priority(),
                response.status(),
                response.createdAt(),
                response.scheduledFor(),
                response.sentAt(),
                response.retryCount(),
                response.errorMessage(),
                response.sourceService()
        );
        
        log.info("toNotificationResponseDTO - end");
        return dto;
    }

    /**
     * Convierte un objeto NotificationStatusResponse de la capa de aplicación a un DTO de respuesta.
     *
     * @param response Objeto de respuesta de la capa de aplicación
     * @return DTO de respuesta para la API
     */
    public NotificationStatusResponseDTO toNotificationStatusResponseDTO(NotificationStatusResponse response) {
        
        log.info("toNotificationStatusResponseDTO - init");
        
        if (response == null) {
            return null;
        }

        NotificationStatusResponseDTO dto = new NotificationStatusResponseDTO(
                response.notificationId(),
                response.status(),
                response.lastUpdated(),
                response.errorMessage(),
                response.retryCount(),
                response.canRetry()
        );
        
        log.info("toNotificationStatusResponseDTO - end");
        return dto;
    }

    /**
     * Convierte un objeto NotificationHistoryResult de la capa de aplicación a un DTO de respuesta.
     *
     * @param result Objeto de resultado de la capa de aplicación
     * @return DTO de respuesta para la API
     */
    public NotificationHistoryResultDTO toNotificationHistoryResultDTO(NotificationHistoryResult result) {
        
        log.info("toNotificationHistoryResultDTO - init");
        
        if (result == null) {
            return null;
        }

        List<NotificationResponseDTO> notificationDTOs = result.notifications().stream()
                .map(this::toNotificationResponseDTO)
                .collect(Collectors.toList());

        NotificationHistoryResultDTO dto = new NotificationHistoryResultDTO(
                notificationDTOs,
                result.totalCount(),
                result.limit(),
                result.offset(),
                result.hasMore()
        );
        
        log.info("toNotificationHistoryResultDTO - end");
        return dto;
    }
}
