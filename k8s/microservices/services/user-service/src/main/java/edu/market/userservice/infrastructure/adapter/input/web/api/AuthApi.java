package edu.market.userservice.infrastructure.adapter.input.web.api;

import edu.market.userservice.infrastructure.adapter.input.web.dto.request.LoginRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.SignupRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.SocialLoginRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.AuthResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.MessageResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** API para operaciones de autenticación y gestión de sesiones */
@RequestMapping("/api/auth")
public interface AuthApi {

  /**
   * Autentica a un usuario con credenciales locales
   *
   * @param loginRequest Credenciales de login
   * @param request Información de la petición HTTP
   * @return Respuesta con token de autenticación
   */
  @PostMapping("/login")
  ResponseDTO<AuthResponseDTO> login(
      @Valid @RequestBody LoginRequestDTO loginRequest);

  /**
   * Registra un nuevo usuario en el sistema
   *
   * @param signupRequest Datos del nuevo usuario
   * @param request Información de la petición HTTP
   * @return Respuesta con token de autenticación
   */
  @PostMapping("/signup")
  ResponseDTO<AuthResponseDTO> signup(
      @Valid @RequestBody SignupRequestDTO signupRequest);

  /**
   * Procesa autenticación mediante proveedor social (Google, Facebook, etc.)
   *
   * @param socialLoginRequest Datos de autenticación social
   * @param request Información de la petición HTTP
   * @return Respuesta con token de autenticación
   */
  @PostMapping("/social-login")
  ResponseDTO<AuthResponseDTO> socialLogin(
      @Valid @RequestBody SocialLoginRequestDTO socialLoginRequest);

  /**
   * Cierra la sesión actual del usuario
   *
   * @param token Token de autenticación a invalidar
   * @param request Información de la petición HTTP
   * @return Mensaje de confirmación
   */
  @PostMapping("/logout")
  ResponseDTO<MessageResponseDTO> logout(@RequestHeader("Authorization") String token);

  /**
   * Cierra todas las sesiones activas de un usuario
   *
   * @param userId ID del usuario
   * @return Mensaje de confirmación
   */
  @PostMapping("/logout-all")
  ResponseDTO<MessageResponseDTO> logoutAll(@RequestParam Long userId);

  /**
   * Actualiza un token expirado usando un refresh token
   *
   * @param refreshToken Token de actualización
   * @param request Información de la petición HTTP
   * @return Nuevo token de autenticación
   */
  @PostMapping("/refresh-token")
  ResponseDTO<?> refreshToken(@RequestParam String refreshToken);
}
