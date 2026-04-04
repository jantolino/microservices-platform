package edu.market.notification.infrastructure.adapter.output.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.market.notification.domain.port.output.service.SerializationServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura para la serialización de objetos.
 * Implementa el puerto de salida SerializationServicePort utilizando Jackson ObjectMapper.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SerializationServiceAdapter implements SerializationServicePort {
    
    private final ObjectMapper objectMapper;    

    /**
     * Serializa un objeto a formato JSON.
     * 
     * @param object Objeto a serializar
     * @return Representación JSON del objeto como String
     */
    @Override
    public String serialize(Object object) {
        try {
            log.debug("SerializationServiceAdapter.serialize - Serializando objeto: {}", object.getClass().getSimpleName());
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("SerializationServiceAdapter.serialize - Error al serializar objeto: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Deserializa un JSON a un objeto del tipo especificado.
     * 
     * @param json JSON a deserializar
     * @param type Clase del objeto a deserializar
     * @return Objeto deserializado o null si ocurre un error
     */
    @Override
    public <T> T deserialize(String json, Class<T> type) {
        try {
            log.debug("SerializationServiceAdapter.deserialize - Deserializando JSON a tipo: {}", type.getSimpleName());
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("SerializationServiceAdapter.deserialize - Error al deserializar JSON a tipo {}: {}", 
                    type.getSimpleName(), e.getMessage());
            return null;
        }
    }
}
