package edu.market.userservice.infrastructure.adapter.input.web.controller;

import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.vo.LoginCredentialsVO;
import edu.market.userservice.domain.vo.LogoutAllRequestVO;
import edu.market.userservice.domain.vo.LogoutRequestVO;
import edu.market.userservice.domain.vo.SocialLoginRequestVO;
import edu.market.userservice.domain.vo.TokenRefreshRequestVO;
import edu.market.userservice.domain.vo.UserRegistrationVO;
import edu.market.userservice.application.port.input.LoginUserUseCasePort;
import edu.market.userservice.application.port.input.LogoutUserUseCasePort;
import edu.market.userservice.application.port.input.RefreshTokenUseCasePort;
import edu.market.userservice.application.port.input.RegisterUserUseCasePort;
import edu.market.userservice.domain.exception.AuthenticationException;
import edu.market.userservice.infrastructure.adapter.input.web.api.AuthApi;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.LoginRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.SignupRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.SocialLoginRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.AuthResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.MessageResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.ResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.mapper.AuthRequestMapper;
import edu.market.userservice.infrastructure.adapter.input.web.mapper.AuthResponseMapper;
import edu.market.userservice.infrastructure.adapter.input.web.mapper.ResponseMapper;
import edu.market.userservice.infrastructure.adapter.input.web.util.RequestContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthAdapter implements AuthApi {
    
    private final LoginUserUseCasePort loginUserUseCase;
    private final RegisterUserUseCasePort registerUserUseCase;
    private final LogoutUserUseCasePort logoutUserUseCase;
    private final RefreshTokenUseCasePort refreshTokenUseCase;
    private final AuthRequestMapper authRequestMapper;    
    private final AuthResponseMapper authResponseMapper;
    private final RequestContextUtil requestContextUtil;
    private final ResponseMapper responseMapper;

    @Override
    public ResponseDTO<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        
        log.info("Recibida solicitud de login para el usuario: {}", loginRequest.getEmail());
        log.debug("Dirección IP del cliente: {}", requestContextUtil.getClientIp());
        log.debug("User-Agent del cliente: {}", requestContextUtil.getUserAgent());
        
        // Crear el Value Object para las credenciales de login
        LoginCredentialsVO credentials = new LoginCredentialsVO(
            loginRequest.getEmail(),
            loginRequest.getPassword(),
            requestContextUtil.getClientIp(),
            requestContextUtil.getUserAgent()
        );
        
        // Ejecutar el caso de uso
        UserAuthToken token = loginUserUseCase.execute(credentials);
        
        // Convertir el token a DTO de respuesta usando el mapper
        AuthResponseDTO authResponse = authResponseMapper.toAuthResponseDTO(token);
        log.info("Login exitoso para el usuario: {}", loginRequest.getEmail());
        
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toSuccessResponseDTO(
                authResponse,
                "Login exitoso"
        );
        
    }

    @Override
    public ResponseDTO<AuthResponseDTO> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
        
        log.info("Recibida solicitud de registro para el usuario: {}", signupRequest.getEmail());
        log.debug("Dirección IP del cliente: {}", requestContextUtil.getClientIp());
        log.debug("User-Agent del cliente: {}", requestContextUtil.getUserAgent());
        
        // Convertir DTO a Value Object usando el mapper
        UserRegistrationVO registrationData = authRequestMapper.toUserRegistrationVO(
            signupRequest,
            requestContextUtil.getClientIp(),
            requestContextUtil.getUserAgent()
        );
        log.debug("VO de registro creado correctamente para el usuario: {}", registrationData.getUser().getEmail());
        
        // Ejecutar el caso de uso
        UserAuthToken token = registerUserUseCase.execute(registrationData);
        
        // Convertir el token a DTO de respuesta usando el mapper
        AuthResponseDTO authResponse = authResponseMapper.toAuthResponseDTO(token);
        log.info("Registro exitoso para el usuario: {}", signupRequest.getEmail());
        
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toSuccessResponseDTO(
                authResponse,
                "Registro exitoso"
        );
        
    }

    @Override
    public ResponseDTO<AuthResponseDTO> socialLogin(@Valid @RequestBody SocialLoginRequestDTO socialLoginRequest) {
        log.info("Recibida solicitud de login social para el usuario: {} con proveedor: {}", 
                socialLoginRequest.getEmail(), socialLoginRequest.getProvider());
        log.debug("Dirección IP del cliente: {}", requestContextUtil.getClientIp());
        log.debug("User-Agent del cliente: {}", requestContextUtil.getUserAgent());
        
        // Convertir DTO a Value Object usando el mapper
        SocialLoginRequestVO socialLoginData = authRequestMapper.toSocialLoginRequestVO(
            socialLoginRequest,
            requestContextUtil.getClientIp(),
            requestContextUtil.getUserAgent()
        );
        
        // Ejecutar el caso de uso
        UserAuthToken token = loginUserUseCase.execute(socialLoginData);
        
        // Convertir el token a DTO de respuesta usando el mapper
        AuthResponseDTO authResponse = authResponseMapper.toAuthResponseDTO(token);
        log.info("Login social exitoso para el usuario: {} con proveedor: {}", 
                socialLoginRequest.getEmail(), socialLoginRequest.getProvider());
                
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toSuccessResponseDTO(
                authResponse,
                "Login social exitoso"
        );
    }

    @Override
    public ResponseDTO<MessageResponseDTO> logout(@RequestHeader("Authorization") String token) {
        log.info("Recibida solicitud de cierre de sesión");
        log.debug("Dirección IP del cliente: {}", requestContextUtil.getClientIp());
        log.debug("User-Agent del cliente: {}", requestContextUtil.getUserAgent());
                
        // Crear el Value Object para la solicitud de logout usando el mapper
        String accessToken = token.replace("Bearer ", "");
        LogoutRequestVO logoutRequest = authRequestMapper.toLogoutRequestVO(
                accessToken,
                null, // No hay refreshToken en este endpoint
                requestContextUtil.getClientIp(),
                requestContextUtil.getUserAgent()
        );
        
        // Ejecutar el caso de uso
        boolean success = logoutUserUseCase.execute(logoutRequest);
        
        MessageResponseDTO messageResponse = new MessageResponseDTO();
        messageResponse.setMessage("Sesión cerrada exitosamente");
        messageResponse.setSuccess(success);
        
        if (success) {
            log.info("Sesión cerrada exitosamente");
        } else {
            log.warn("Intento de cierre de sesión para un token no válido o ya revocado");
        }
        
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toSuccessResponseDTO(
                messageResponse,
                "Operación de cierre de sesión completada"
        );
        
    }

    @Override
    public ResponseDTO<MessageResponseDTO> logoutAll(@RequestParam Long userId) {
        log.info("Recibida solicitud de cierre de todas las sesiones para el usuario con ID: {}", userId);
               
        // Crear el Value Object para la solicitud de logout all usando el mapper
        LogoutAllRequestVO logoutAllRequest = authRequestMapper.toLogoutAllRequestVO(userId);
        
        // Ejecutar el caso de uso
        int tokensRevoked = logoutUserUseCase.execute(logoutAllRequest);
        
        MessageResponseDTO messageResponse = new MessageResponseDTO();
        messageResponse.setMessage(tokensRevoked > 0 ? 
                "Se cerraron " + tokensRevoked + " sesiones activas" : 
                "No se encontraron sesiones activas");
        messageResponse.setSuccess(true);
        
        if (tokensRevoked > 0) {
            log.info("Se cerraron {} sesiones activas para el usuario con ID: {}", tokensRevoked, userId);
        } else {
            log.info("No se encontraron sesiones activas para el usuario con ID: {}", userId);
        }
        
        // Usar el ResponseMapper para crear la respuesta
        return responseMapper.toSuccessResponseDTO(
                messageResponse,
                "Operación de cierre de todas las sesiones completada"
        );
    }

    @Override
    public ResponseDTO<?> refreshToken(@RequestParam String refreshToken) {
        log.info("Recibida solicitud de renovación de token");
        log.debug("Dirección IP del cliente: {}", requestContextUtil.getClientIp());
        log.debug("User-Agent del cliente: {}", requestContextUtil.getUserAgent());
               
        // Crear el Value Object para la solicitud de renovación de token usando el mapper
        TokenRefreshRequestVO refreshRequest = authRequestMapper.toTokenRefreshRequestVO(
                refreshToken,
                requestContextUtil.getClientIp(),
                requestContextUtil.getUserAgent()
        );
        
        // Ejecutar el caso de uso
        Optional<UserAuthToken> tokenOpt = refreshTokenUseCase.execute(refreshRequest);
        
        if (tokenOpt.isPresent()) {
            // Convertir el token a DTO de respuesta usando el mapper
            AuthResponseDTO authResponse = authResponseMapper.toAuthResponseDTO(tokenOpt.get());
            log.info("Token renovado exitosamente");
            
            // Usar el ResponseMapper para crear la respuesta
            return responseMapper.toSuccessResponseDTO(
                    authResponse,
                    "Token renovado exitosamente"
            );
        } else {
            log.warn("Intento de renovación de token con un refresh token inválido");
            throw AuthenticationException.invalidToken();
        }
        
    }
}
