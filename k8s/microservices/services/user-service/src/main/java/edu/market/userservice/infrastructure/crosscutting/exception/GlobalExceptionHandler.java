package edu.market.userservice.infrastructure.crosscutting.exception;

import edu.market.userservice.domain.exception.AuthenticationException;
import edu.market.userservice.domain.exception.DomainException;
import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.ResponseDTO;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/** Manejador global de excepciones para la API REST */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Maneja excepciones de validación de argumentos de método (anotaciones @Valid) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDTO> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ResponseDTO errorResponse =
        ResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.name())
            .message("Error de validación")
            .path(request.getDescription(false).replace("uri=", ""))
            .data(errors)            
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /** Maneja excepciones de violación de restricciones (validación de parámetros) */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseDTO> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation -> {
              String propertyPath = violation.getPropertyPath().toString();
              String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
              String errorMessage = violation.getMessage();
              errors.put(fieldName, errorMessage);
            });

    ResponseDTO errorResponse =
        ResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.name())
            .message("Error de validación")
            .path(request.getDescription(false).replace("uri=", ""))
            .data(errors)
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /** Maneja excepciones de autenticación */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ResponseDTO> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {

    ResponseDTO errorResponse =
        ResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.name())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  /** Maneja excepciones relacionadas con usuarios */
  @ExceptionHandler(UserException.class)
  public ResponseEntity<ResponseDTO> handleUserException(
      UserException ex, WebRequest request) {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    // Determinar el código de estado HTTP según el tipo de excepción
    if (ex.getMessage().contains("no encontrado") || ex.getMessage().contains("not found")) {
      status = HttpStatus.NOT_FOUND;
    }

    ResponseDTO errorResponse =
        ResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(status.name())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, status);
  }

  /** Maneja excepciones generales del dominio */
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ResponseDTO> handleDomainException(
      DomainException ex, WebRequest request) {

    ResponseDTO errorResponse =
        ResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.name())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))            
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /** Maneja excepciones no controladas */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseDTO> handleGlobalException(Exception ex, WebRequest request) {

    ResponseDTO errorResponse =
        ResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
            .message("Error interno del servidor: " + ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
