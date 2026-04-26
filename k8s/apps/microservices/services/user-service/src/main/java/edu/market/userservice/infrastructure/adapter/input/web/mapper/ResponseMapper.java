package edu.market.userservice.infrastructure.adapter.input.web.mapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import edu.market.userservice.infrastructure.adapter.input.web.dto.response.ResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Mapper para construir respuestas estandarizadas
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseMapper {
    
    private final RequestContextUtil requestContextUtil;
    
    /**
     * Construye una respuesta exitosa con datos
     * 
     * @param <T> Tipo de datos
     * @param data Datos a incluir en la respuesta
     * @param message Mensaje descriptivo
     * @param status Estado HTTP
     * @return ResponseDTO con los datos y metadatos
     */
    public <T> ResponseDTO<T> toResponseDTO(T data, String message, HttpStatus status) {
        return ResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value() + " " + status.getReasonPhrase())
                .message(message)
                .path(requestContextUtil.getRequestPath())
                .data(data)
                .build();
    }
    
    /**
     * Construye una respuesta exitosa con datos y estado 200 OK
     * 
     * @param <T> Tipo de datos
     * @param data Datos a incluir en la respuesta
     * @param message Mensaje descriptivo
     * @return ResponseDTO con los datos y metadatos
     */
    public <T> ResponseDTO<T> toSuccessResponseDTO(T data, String message) {
        return toResponseDTO(data, message, HttpStatus.OK);
    }
    
    /**
     * Construye una respuesta de error
     * 
     * @param <T> Tipo de datos
     * @param message Mensaje de error
     * @param status Estado HTTP de error
     * @return ResponseDTO con el mensaje de error
     */
    public <T> ResponseDTO<T> toErrorResponseDTO(String message, HttpStatus status) {
        return ResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value() + " " + status.getReasonPhrase())
                .message(message)
                .path(requestContextUtil.getRequestPath())
                .build();
    }
}
