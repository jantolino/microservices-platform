package edu.market.notification.application.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Respuesta que representa una plantilla de notificación.
 * Objeto de transferencia que contiene los datos resultantes de una operación sobre plantillas.
 */
public record TemplateResponse(
    String templateId,
    String templateType,
    String name,
    String subject,
    String content,
    String contentType,
    Map<String, Object> metadata,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
