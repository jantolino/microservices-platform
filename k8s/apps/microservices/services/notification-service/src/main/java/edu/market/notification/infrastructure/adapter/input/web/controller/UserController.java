package edu.market.notification.infrastructure.adapter.input.web.controller;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.DisableNotificationsCommand;
import edu.market.notification.application.dto.request.UpdateUserPreferencesCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;
import edu.market.notification.application.port.input.user.DisableNotificationsUseCasePort;
import edu.market.notification.application.port.input.user.GetUserPreferencesUseCasePort;
import edu.market.notification.application.port.input.user.UpdateUserPreferencesUseCasePort;
import edu.market.notification.infrastructure.adapter.input.web.api.UserApi;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.DisableNotificationsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UpdateUserPreferencesRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.UserPreferenceResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.mapper.UserInputMapper;
import edu.market.notification.infrastructure.adapter.input.web.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que implementa la API de gestión de preferencias de usuario.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UpdateUserPreferencesUseCasePort updateUserPreferencesUseCase;    
    private final GetUserPreferencesUseCasePort getUserPreferencesUseCase;
    private final DisableNotificationsUseCasePort disableNotificationsUseCase;
    private final UserInputMapper userInputMapper;
    private final RequestContextUtil requestContextUtil;

    @Override
    public ResponseEntity<UserPreferenceResponseDTO> getUserPreferences(String userId) {
        log.info("getUserPreferences - init");
        
        log.debug("getUserPreferences - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(userId);
        
        log.debug("getUserPreferences - Call getUserPreferencesUseCase.get");
        Optional<UserPreferenceResponse> response = 
            getUserPreferencesUseCase.get(UUID.fromString(userId), clientContextCommand);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        ResponseEntity<UserPreferenceResponseDTO> result = ResponseEntity.ok(userInputMapper.toUserPreferenceResponseDTO(response.get()));
        
        log.info("getUserPreferences - end");
        return result;
    }

    @Override
    public ResponseEntity<UserPreferenceResponseDTO> updateUserPreferences(String userId, UpdateUserPreferencesRequestDTO requestDTO) {
        log.info("updateUserPreferences - init");
        
        log.debug("updateUserPreferences - Call userInputMapper.toUpdateUserPreferencesCommand");
        UpdateUserPreferencesCommand command = userInputMapper.toUpdateUserPreferencesCommand(
                userId, 
                requestDTO
        );

        log.debug("updateUserPreferences - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(userId);
        
        log.debug("updateUserPreferences - Call updateUserPreferencesUseCase.update");
        UserPreferenceResponse response = updateUserPreferencesUseCase.update(command, clientContextCommand);
        
        // Convertir la respuesta del dominio a DTO y devolverla
        ResponseEntity<UserPreferenceResponseDTO> result = ResponseEntity.ok(userInputMapper.toUserPreferenceResponseDTO(response));
        
        log.info("updateUserPreferences - end");
        return result;
    }

    @Override
    public ResponseEntity<Void> disableNotifications(String userId, DisableNotificationsRequestDTO requestDTO) {
        
        log.info("disableNotifications - init");
        
        // Crear el comando para deshabilitar las notificaciones
        log.debug("disableNotifications - Call userInputMapper.toDisableNotificationsCommand");
        DisableNotificationsCommand command = userInputMapper.toDisableNotificationsCommand(userId, requestDTO);

        log.debug("disableNotifications - Call requestContextUtil.getClientContext");
        ClientContextCommand clientContextCommand = requestContextUtil.getClientContext(userId);
        
        log.debug("disableNotifications - Call disableNotificationsUseCase.disable");
        disableNotificationsUseCase.disable(command, clientContextCommand);
        
        ResponseEntity<Void> result = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        
        log.info("disableNotifications - end");
        return result;
    }
}
