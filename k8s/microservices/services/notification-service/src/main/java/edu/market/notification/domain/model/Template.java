package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.exception.TemplateException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad que representa una plantilla para notificaciones.
 * 
 * Patrones de diseño implementados:
 * - Entity: Implementa el concepto de Entidad de Dominio con identificador único (UUID)
 * - Builder: Utiliza el patrón Builder para la construcción flexible de plantillas
 * - Versioning: Implementa versionado de plantillas mediante el método createNewVersion()
 * - Validator: Incluye validación interna de reglas de negocio en el método validate()
 * - Immutable Object: Los atributos principales son finales y solo se modifican a través
 *   de la creación de nuevas versiones
 * - Rich Domain Model: Encapsula comportamiento y reglas de negocio dentro de la entidad
 */
public class Template {
    
    private final UUID id;
    private final String code;
    private String name;
    private String content;
    private String subject;
    private String language;
    private final Set<NotificationChannelType> supportedChannels;
    private final int version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private UUID createdBy;
    private UUID updatedBy;
    private final SourceServiceType sourceService; // Servicio que originó la plantilla

    private Template(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.code = builder.code;
        this.name = builder.name;
        this.content = builder.content;
        this.subject = builder.subject;
        this.supportedChannels = new HashSet<>(builder.supportedChannels);
        this.version = builder.version != null ? builder.version : 1;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt;
        this.active = builder.active != null ? builder.active : true;
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
        this.sourceService = builder.sourceService != null ? builder.sourceService : SourceServiceType.UNKNOWN;
        this.language = builder.language;
        
        validate();
    }
    
    private void validate() {
        if (code == null || code.trim().isEmpty()) {
            throw new TemplateException("code required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new TemplateException("name required");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new TemplateException("content required");
        }
        
        if (supportedChannels.isEmpty()) {
            throw new TemplateException("supportedChannels required");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getSubject() {
        return subject;
    }

    public Set<NotificationChannelType> getSupportedChannels() {
        return new HashSet<>(supportedChannels);
    }

    public String getLanguage() {
        return language;
    }

    public int getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }
    
    public SourceServiceType getSourceService() {
        return sourceService;
    }

    // El método createNewVersion ha sido eliminado y su funcionalidad movida a TemplateOutputMapper.createUpdatedVersion

    /**
     * Activa o desactiva la plantilla
     * @param active Estado de activación
     * @param updatedBy Usuario que realiza el cambio
     */
    public void setActive(boolean active, UUID updatedBy) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Verifica si la plantilla soporta un canal específico
     * @param channel Canal a verificar
     * @return true si el canal es soportado
     */
    public boolean supportsChannel(NotificationChannelType channel) {
        return supportedChannels.contains(channel);
    }
    
    /**
     * Verifica si la plantilla soporta al menos uno de los canales especificados
     * @param channels Conjunto de canales a verificar
     * @return true si al menos uno de los canales es soportado
     */
    public boolean supportsChannel(Set<NotificationChannelType> channels) {
        if (channels == null || channels.isEmpty()) {
            return false;
        }
        
        // Verificar si hay al menos un canal en común entre los canales soportados
        // por la plantilla y los canales especificados
        for (NotificationChannelType channel : channels) {
            if (supportedChannels.contains(channel)) {
                return true;
            }
        }
        
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        
        private UUID id;
        private String code;
        private String name;
        private String content;
        private String subject;
        private Set<NotificationChannelType> supportedChannels = new HashSet<>();
        private Integer version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean active;
        private UUID createdBy;
        private UUID updatedBy;
        private SourceServiceType sourceService;
        private String language;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder withSupportedChannel(NotificationChannelType channel) {
            this.supportedChannels.add(channel);
            return this;
        }

        public Builder withSupportedChannels(Set<NotificationChannelType> channels) {
            if (channels != null) {
                this.supportedChannels.addAll(channels);
            }
            return this;
        }

        public Builder withVersion(Integer version) {
            this.version = version;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder withActive(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder withCreatedBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withUpdatedBy(UUID updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }
        
        public Builder withSourceService(SourceServiceType sourceService) {
            this.sourceService = sourceService;
            return this;
        }

        public Template build() {
            return new Template(this);
        }
    }
}
