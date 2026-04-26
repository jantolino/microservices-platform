package edu.market.notification.infrastructure.adapter.input.web.controller;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.CreateTemplateCommand;
import edu.market.notification.application.dto.request.GetTemplatesQuery;
import edu.market.notification.application.dto.request.UpdateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;
import edu.market.notification.application.port.input.template.CreateTemplateUseCasePort;
import edu.market.notification.application.port.input.template.DeleteTemplateUseCasePort;
import edu.market.notification.application.port.input.template.GetTemplateUseCasePort;
import edu.market.notification.application.port.input.template.UpdateTemplateUseCasePort;
import edu.market.notification.infrastructure.adapter.input.web.api.TemplateApi;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.CreateTemplateRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.GetTemplatesRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UpdateTemplateRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.TemplateResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.mapper.TemplateInputMapper;
import edu.market.notification.infrastructure.adapter.input.web.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que implementa la API de gestión de plantillas de notificación.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TemplateController implements TemplateApi {

    private final CreateTemplateUseCasePort createTemplateUseCase;
    private final UpdateTemplateUseCasePort updateTemplateUseCase;
    private final DeleteTemplateUseCasePort deleteTemplateUseCase;
    private final GetTemplateUseCasePort getTemplateUseCase;    
    private final TemplateInputMapper templateInputMapper;
    private final RequestContextUtil requestContextUtil;

    @Override
    public ResponseEntity<TemplateResponseDTO> createTemplate(CreateTemplateRequestDTO requestDTO) {
        log.info("createTemplate - init");
        
        log.debug("createTemplate - Call templateInputMapper.toCreateTemplateCommand");
        CreateTemplateCommand command = templateInputMapper.toCreateTemplateCommand(requestDTO);
     
        log.debug("createTemplate - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(requestDTO.getUserUuid());

        log.debug("createTemplate - Call createTemplateUseCase.create");
        TemplateResponse response = createTemplateUseCase.create(command, clientContextCommand);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        ResponseEntity<TemplateResponseDTO> result = ResponseEntity.status(HttpStatus.CREATED)
                .body(templateInputMapper.toTemplateResponseDTO(response));
        
        log.info("createTemplate - end");
        return result;
    }

    @Override
    public ResponseEntity<List<TemplateResponseDTO>> getTemplates(GetTemplatesRequestDTO requestDTO) {
        log.info("getTemplates - init");
        
        log.debug("getTemplates - Call templateInputMapper.toGetTemplatesQuery");
        GetTemplatesQuery query = templateInputMapper.toGetTemplatesQuery(requestDTO);
        
        log.debug("getTemplates - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(requestDTO.getUserUuid());
        
        log.debug("getTemplates - Call getTemplateUseCase.getTemplates");
        List<TemplateResponse> response = getTemplateUseCase.getTemplates(query, clientContextCommand);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        ResponseEntity<List<TemplateResponseDTO>> result = ResponseEntity.ok(templateInputMapper.toTemplateResponseDTOList(response));
        
        log.info("getTemplates - end");
        return result;
    }

    @Override
    public ResponseEntity<TemplateResponseDTO> updateTemplate(String templateId, UpdateTemplateRequestDTO requestDTO) {
        log.info("updateTemplate - init");
        
        log.debug("updateTemplate - Call templateInputMapper.toUpdateTemplateCommand");
        UpdateTemplateCommand command = templateInputMapper.toUpdateTemplateCommand(templateId, requestDTO);

        log.debug("updateTemplate - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(requestDTO.getUserUuid());
        
        log.debug("updateTemplate - Call updateTemplateUseCase.update");
        TemplateResponse response = updateTemplateUseCase.update(command, clientContextCommand);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        ResponseEntity<TemplateResponseDTO> result = ResponseEntity.ok(templateInputMapper.toTemplateResponseDTO(response));
        
        log.info("updateTemplate - end");
        return result;
    }

    @Override
    public ResponseEntity<Void> deleteTemplate(String templateId) {
        log.info("deleteTemplate - init");
        
        log.debug("deleteTemplate - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(templateId);        
        
        log.debug("deleteTemplate - Call deleteTemplateUseCase.delete");
        deleteTemplateUseCase.delete(templateId, clientContextCommand);
        
        ResponseEntity<Void> result = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        
        log.info("deleteTemplate - end");
        return result;
    }
}
