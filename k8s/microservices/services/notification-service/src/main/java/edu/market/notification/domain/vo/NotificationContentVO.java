package edu.market.notification.domain.vo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.market.notification.domain.exception.NotificationException;

/**
 * Value Object que representa el contenido de una notificación.
 * Implementado como un record para garantizar inmutabilidad.
 */
public record NotificationContentVO(String body, Map<String, Object> attributes) {

    /**
     * Constructor compacto con validación
     */
    public NotificationContentVO {
        
        if (body == null) {
            throw new NotificationException("body required");
        }

        if (body.trim().isEmpty()) {
            throw new NotificationException("body required");
        }
        
        // Asegurar inmutabilidad defensiva para el mapa de atributos
        attributes = attributes != null 
            ? Collections.unmodifiableMap(new HashMap<>(attributes)) 
            : Collections.emptyMap();
    }

    /**
     * Obtiene un atributo específico del contenido
     * @param key Clave del atributo
     * @return Valor del atributo o null si no existe
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Verifica si existe un atributo específico
     * @param key Clave del atributo
     * @return true si existe
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Crea un nuevo objeto NotificationContent con un cuerpo y sin atributos
     * @param body Cuerpo del mensaje
     * @return Nuevo objeto NotificationContent
     */
    public static NotificationContentVO of(String body) {
        return new NotificationContentVO(body, null);
    }

    /**
     * Crea un nuevo objeto NotificationContent con cuerpo y atributos
     * @param body Cuerpo del mensaje
     * @param attributes Atributos adicionales
     * @return Nuevo objeto NotificationContent
     */
    public static NotificationContentVO of(String body, Map<String, Object> attributes) {
        return new NotificationContentVO(body, attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationContentVO that = (NotificationContentVO) o;
        return Objects.equals(body, that.body) && 
               Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, attributes);
    }

    @Override
    public String toString() {
        return "NotificationContentVO{" +
                "body='" + body + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
