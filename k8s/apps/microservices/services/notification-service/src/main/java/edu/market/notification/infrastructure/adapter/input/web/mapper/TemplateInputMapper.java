package edu.market.notification.infrastructure.adapter.input.web.mapper;

import edu.market.notification.application.dto.request.CreateTemplateCommand;
import edu.market.notification.application.dto.request.GetTemplatesQuery;
import edu.market.notification.application.dto.request.UpdateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.CreateTemplateRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.GetTemplatesRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UpdateTemplateRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.TemplateResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir DTOs de plantillas de la capa web a comandos/consultas de la capa de aplicación
 * y viceversa. Este mapper se encarga de la conversión entre objetos de la API REST y objetos de la capa 
 * de aplicación para las operaciones relacionadas con plantillas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateInputMapper {
    
    /**
     * Convierte un DTO de solicitud de creación de plantilla a un comando de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public CreateTemplateCommand toCreateTemplateCommand(CreateTemplateRequestDTO requestDTO) {
        
        log.info("toCreateTemplateCommand - init");
        
        CreateTemplateCommand command = new CreateTemplateCommand(
            requestDTO.getUserUuid(),
            requestDTO.getTemplateType(),
            requestDTO.getName(),
            requestDTO.getSubject(),
            requestDTO.getContent(),
            requestDTO.getContentType(),
            requestDTO.getMetadata()
        );
        
        log.info("toCreateTemplateCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de actualización de plantilla a un comando de la capa de aplicación.
     *
     * @param templateId ID de la plantilla a actualizar
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public UpdateTemplateCommand toUpdateTemplateCommand(String templateId, UpdateTemplateRequestDTO requestDTO) {
        
        log.info("toUpdateTemplateCommand - init");
        
        UpdateTemplateCommand command = new UpdateTemplateCommand(
            requestDTO.getUserUuid(),
            templateId,
            requestDTO.getTemplateType(),
            requestDTO.getName(),
            requestDTO.getSubject(),
            requestDTO.getContent(),
            requestDTO.getContentType(),
            requestDTO.getMetadata()
        );
        
        log.info("toUpdateTemplateCommand - end");
        return command;
    }
    
    /**
     * Convierte un DTO de solicitud de consulta de plantillas a una consulta de la capa de aplicación.
     *
     * @param requestDTO DTO de solicitud de la API
     * @return Consulta para la capa de aplicación
     */
    public GetTemplatesQuery toGetTemplatesQuery(GetTemplatesRequestDTO requestDTO) {
        
        log.info("toGetTemplatesQuery - init");
        
        GetTemplatesQuery query = new GetTemplatesQuery(
            requestDTO.getTemplateId(),
            requestDTO.getTemplateType()
        );
        
        log.info("toGetTemplatesQuery - end");
        return query;
    }

    /**
     * Convierte un objeto TemplateResponse de la capa de aplicación a un DTO de respuesta.
     *
     * @param response Objeto de respuesta de la capa de aplicación
     * @return DTO de respuesta para la API
     */
    public TemplateResponseDTO toTemplateResponseDTO(TemplateResponse response) {
        
        log.info("toTemplateResponseDTO - init");
        
        if (response == null) {
            return null;
        }

        TemplateResponseDTO dto = new TemplateResponseDTO(
                response.templateId(),
                response.templateType(),
                response.name(),
                response.subject(),
                response.content(),
                response.contentType(),
                response.metadata(),
                response.createdAt(),
                response.updatedAt()
        );
        
        log.info("toTemplateResponseDTO - end");
        return dto;
    }

    /**
     * Convierte una lista de objetos TemplateResponse de la capa de aplicación a una lista de DTOs de respuesta.
     *
     * @param responses Lista de objetos de respuesta de la capa de aplicación
     * @return Lista de DTOs de respuesta para la API
     */
    public List<TemplateResponseDTO> toTemplateResponseDTOList(List<TemplateResponse> responses) {
        
        log.info("toTemplateResponseDTOList - init");
        
        if (responses == null) {
            return null;
        }

        List<TemplateResponseDTO> dtoList = responses.stream()
                .map(this::toTemplateResponseDTO)
                .collect(Collectors.toList());
        
        log.info("toTemplateResponseDTOList - end");
        return dtoList;
    }
}
