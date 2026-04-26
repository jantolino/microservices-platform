package edu.market.notification.domain.vo;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.market.notification.domain.exception.NotificationException;

/**
 * Value Object que encapsula los criterios de filtrado para búsqueda de notificaciones.
 * Implementa validaciones para garantizar la integridad de los criterios.
 */
public record NotificationFilterCriteriaVO(
    UUID userId,
    String notificationType,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    int limit,
    int offset
) {
    /**
     * Constructor con validación de parámetros
     */
    public NotificationFilterCriteriaVO {
        // Validaciones de negocio
        if (limit <= 0) {
            throw new NotificationException("limit invalid");
        }
        
        if (offset < 0) {
            throw new NotificationException("offset invalid");
        }
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new NotificationException("date range invalid");
        }
    }
    
    /**
     * Builder estático para facilitar la creación con valores por defecto
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID userId;
        private String notificationType;
        private String status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int limit = 20; // Valor por defecto
        private int offset = 0; // Valor por defecto

        public Builder withUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withNotificationType(String notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder withOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public NotificationFilterCriteriaVO build() {
            return new NotificationFilterCriteriaVO(
                userId, 
                notificationType, 
                status, 
                startDate, 
                endDate, 
                limit, 
                offset
            );
        }
    }
}
