package edu.market.userservice.infrastructure.adapter.input.web.controller;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.PaginationRequestVO;
import edu.market.userservice.domain.vo.ProfileUpdateRequestVO;
import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.application.port.input.DeleteUserUseCasePort;
import edu.market.userservice.application.port.input.GetUserUseCasePort;
import edu.market.userservice.application.port.input.UpdateUserProfileUseCasePort;
import edu.market.userservice.infrastructure.adapter.input.web.api.UserApi;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.UserUpdateRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.ResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.UserResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.mapper.UserRequestMapper;
import edu.market.userservice.infrastructure.adapter.input.web.mapper.UserResponseMapper;
import edu.market.userservice.infrastructure.adapter.input.web.mapper.ResponseMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserAdapter implements UserApi {
    
    private final GetUserUseCasePort getUserUseCase;
    private final UpdateUserProfileUseCasePort updateUserProfileUseCase;
    private final DeleteUserUseCasePort deleteUserUseCase;
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;
    private final ResponseMapper responseMapper;
    
    @Override
    public ResponseDTO<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("init - getUserById para ID: {}", id);
               
        // Ejecutar caso de uso directamente con el ID
        User user = getUserUseCase.execute(id);
        
        // Convertir resultado del dominio a DTO de respuesta
        UserResponseDTO userResponseDTO = userResponseMapper.toResponseDTO(user);
        
        // Usar el ResponseMapper para crear la respuesta
        ResponseDTO<UserResponseDTO> response = responseMapper.toSuccessResponseDTO(
                userResponseDTO,
                "Usuario recuperado exitosamente"
        );
                
        log.info("end - getUserById para ID: {}", id);
        return response;
        
    }

    @Override
    public ResponseDTO<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequestDTO request) {
        log.info("init - updateUser para ID: {}", id);
                
        // Convertir DTO a VO usando el mapper de entrada
        ProfileUpdateRequestVO profileUpdateRequest = userRequestMapper.toProfileUpdateRequestVO(id, request);
        log.debug("VO creado correctamente para actualización de perfil, ID: {}", id);

        // Ejecutar el caso de uso con el VO
        User updated = updateUserProfileUseCase.execute(profileUpdateRequest);
        
        // Convertir resultado del dominio a DTO de respuesta
        UserResponseDTO userResponseDTO = userResponseMapper.toResponseDTO(updated);
        
        // Usar el ResponseMapper para crear la respuesta
        ResponseDTO<UserResponseDTO> response = responseMapper.toSuccessResponseDTO(
                userResponseDTO,
                "Usuario actualizado exitosamente"
        );
                
        log.info("end - updateUser para ID: {}", id);
        return response;
        
    }

    @Override
    public ResponseDTO<List<UserResponseDTO>> listUsers() {
        log.info("init - listUsers");
                
        // Convertir parámetros a VO usando el mapper de entrada
        PaginationRequestVO paginationRequest = userRequestMapper.toPaginationRequestVO(0, 100);
        log.debug("VO de paginación creado: página 0, tamaño 100");
        
        // Ejecutar caso de uso con el VO
        List<User> users = getUserUseCase.execute(paginationRequest);
        log.debug("Obtenidos {} usuarios del caso de uso", users.size());
        
        // Convertir resultados del dominio a DTOs de respuesta
        List<UserResponseDTO> responseDTOs = users.stream()
                .map(userResponseMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        log.info("end - listUsers: {} usuarios encontrados", responseDTOs.size());
        
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toSuccessResponseDTO(
                responseDTOs,
                "Usuarios recuperados exitosamente"
        );
        
    }

    @Override
    public ResponseDTO<Void> deleteUser(@PathVariable Long id) {
        
        log.info("init - deleteUser para ID: {}", id);
               
        // Ejecutar caso de uso directamente con el ID
        deleteUserUseCase.execute(id);
        
        log.info("end - deleteUser para ID: {}", id);
        
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toResponseDTO(
                null,
                "Usuario eliminado exitosamente",
                HttpStatus.NO_CONTENT
        );
       
    }
}
