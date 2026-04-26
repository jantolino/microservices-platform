package edu.market.notification.domain.vo;

import edu.market.notification.domain.exception.InvalidEntityException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Value Object que representa al destinatario de una notificación.
 * Implementado como un record para garantizar inmutabilidad.
 */
public record RecipientVO(UUID userId, String email, String phoneNumber, String deviceToken, Map<String, Object> attributes) {

    /**
     * Constructor compacto con validación
     */
    public RecipientVO {
        // Asegurar inmutabilidad defensiva para el mapa de atributos
        attributes = attributes != null 
            ? Collections.unmodifiableMap(new HashMap<>(attributes)) 
            : Collections.emptyMap();
        
        // Validar que tenga al menos un método de contacto
        if (userId == null && email == null && phoneNumber == null && deviceToken == null) {
            throw new InvalidEntityException("contact method required");
        }
    }

    /**
     * Obtiene un atributo específico del destinatario
     * @param key Clave del atributo
     * @return Valor del atributo o null si no existe
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Verifica si el destinatario tiene un atributo específico
     * @param key Clave del atributo
     * @return true si existe el atributo
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Builder para construir instancias de RecipientVO de forma fluida
     */
    public static class Builder {
        private UUID userId;
        private String email;
        private String phoneNumber;
        private String deviceToken;
        private Map<String, Object> attributes = new HashMap<>();

        public Builder withUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
            return this;
        }

        public Builder withAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder withAttributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public RecipientVO build() {
            return new RecipientVO(userId, email, phoneNumber, deviceToken, attributes);
        }
    }
}
