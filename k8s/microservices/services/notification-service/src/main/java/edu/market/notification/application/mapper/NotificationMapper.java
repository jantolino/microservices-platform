package edu.market.notification.application.mapper;

import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.request.NotificationHistoryQuery;
import edu.market.notification.application.dto.request.ProcessDomainEventCommand;
import edu.market.notification.application.dto.response.NotificationHistoryResult;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.dto.response.NotificationStatusResponse;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;
import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.vo.NotificationContentVO;
import edu.market.notification.domain.vo.NotificationFilterCriteriaVO;
import edu.market.notification.domain.vo.RecipientVO;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio y objetos de transferencia de la aplicación.
 * Mantiene la separación entre capas siguiendo los principios de arquitectura hexagonal.
 * 
 * Este componente centraliza toda la lógica de conversión entre objetos de diferentes capas,
 * evitando que los objetos de transferencia tengan que conocer detalles de implementación de otras capas.
 */
public class NotificationMapper {

    /**
     * Convierte una entidad Notification a un objeto NotificationResponse
     * 
     * @param notification La entidad de dominio
     * @return El objeto de respuesta
     */
    public static NotificationResponse toResponse(Notification notification) {
        
        if (notification == null) {
            return null;
        }
            
        return new NotificationResponse(
            notification.getId(),
            notification.getSubject(),
            notification.getContent() != null ? notification.getContent().body() : null,
            notification.getRecipient() != null ? notification.getRecipient().userId().toString() : null,
            notification.getRecipient() != null ? notification.getRecipient().email() : null,
            notification.getChannels(),
            notification.getPriority(),
            notification.getStatus(),
            notification.getCreatedAt(),
            notification.getScheduledFor(),
            notification.getSentAt(),
            notification.getRetryCount() != null ? notification.getRetryCount() : 0,
            notification.getErrorMessage(),
            notification.getSourceService() != null ? notification.getSourceService().name() : null
        );
    }
    
    /**
     * Convierte una lista de entidades Notification a un NotificationHistoryResult
     * 
     * @param notifications Lista de entidades de dominio
     * @param totalCount Total de notificaciones (para paginación)
     * @param limit Límite de resultados por página
     * @param offset Desplazamiento para paginación
     * @return El VO de resultado con la lista de notificaciones
     */
    public static NotificationHistoryResult toHistoryResult(
            List<Notification> notifications, 
            int totalCount, 
            int limit, 
            int offset) {
        
        List<NotificationResponse> notificationVOs = notifications.stream()
            .map(NotificationMapper::toResponse)
            .collect(Collectors.toList());
            
        boolean hasMore = (offset + notifications.size()) < totalCount;
        
        return new NotificationHistoryResult(
            notificationVOs,
            totalCount,
            limit,
            offset,
            hasMore
        );
    }
    
    /**
     * Convierte un objeto de consulta (Query) a un objeto de criterios de filtrado
     * 
     * @param query Objeto de consulta de la capa de aplicación
     * @return Objeto de criterios de filtrado
     */
    public static NotificationFilterCriteriaVO toFilterCriteria(NotificationHistoryQuery query) {
        return NotificationFilterCriteriaVO.builder()
            .withUserId(query.userId())
            .withNotificationType(query.notificationType())
            .withStatus(query.status())
            .withStartDate(query.startDate())
            .withEndDate(query.endDate())
            .withLimit(query.limit())
            .withOffset(query.offset())
            .build();
    }
    
    /**
     * Crea un objeto NotificationStatusResponse a partir de una entidad Notification
     * 
     * @param notification La entidad de dominio
     * @param maxRetries El número máximo de reintentos permitidos
     * @return El objeto NotificationStatusResponse con la información del estado
     */
    public static NotificationStatusResponse toStatusResponse(Notification notification, int maxRetries) {
        if (notification == null) {
            return null;
        }
        
        // Usamos la fecha de envío o la fecha actual como última actualización
        LocalDateTime lastUpdated = notification.getSentAt() != null ? 
                notification.getSentAt() : LocalDateTime.now();
        
        // Determinar si la notificación puede reintentarse
        boolean canRetry = notification.canRetry(maxRetries);
        
        return new NotificationStatusResponse(
            notification.getId(),
            notification.getStatus(),
            lastUpdated,
            notification.getErrorMessage(),
            notification.getRetryCount() != null ? notification.getRetryCount() : 0,
            canRetry
        );
    }
    
    /**
     * Convierte un objeto NotificationCommand a una entidad Notification del dominio
     * 
     * @param command El objeto command con los datos de entrada
     * @return La entidad de dominio creada
     */
    /**
     * Convierte un evento de dominio a un comando de notificación.
     * 
     * @param eventCommand El comando con los datos del evento de dominio
     * @param userId El ID del usuario destinatario de la notificación
     * @return Un comando de notificación basado en el evento
     */
    public static NotificationCommand fromDomainEvent(ProcessDomainEventCommand eventCommand, UUID userId) {
        if (eventCommand == null || userId == null) {
            throw new IllegalArgumentException("Event command and user ID cannot be null");
        }
        
        // Extraer datos relevantes del payload del evento
        String subject = "Notificación: " + eventCommand.eventType();
        String content = "Se ha producido un evento de tipo '" + eventCommand.eventType() + "'";
        
        // Si hay datos adicionales en el payload, intentamos enriquecer el contenido
        if (eventCommand.payload() != null && !eventCommand.payload().isEmpty()) {
            // Aquí podríamos personalizar el contenido según el tipo de evento
            // Por ejemplo, si es un evento de pedido, podríamos incluir el número de pedido
            if (eventCommand.payload().containsKey("description")) {
                content += ": " + eventCommand.payload().get("description");
            }
        }
        
        // Configurar canales por defecto (email y push)
        Set<NotificationChannelType> channels = new HashSet<>();
        channels.add(NotificationChannelType.EMAIL);
        channels.add(NotificationChannelType.PUSH);
        
        // Crear el comando de notificación
        return new NotificationCommand(
            subject,
            content,
            userId.toString(),
            null, // El email se obtendrá del perfil de usuario en el caso de uso
            channels,
            NotificationPriorityType.NORMAL,
            null, // Sin programación específica
            eventCommand.source() // Usar la fuente del evento como servicio de origen
        );
    }
    
    public static Notification toEntity(NotificationCommand command) {
        if (command == null) {
            return null;
        }
        
        // Usamos directamente los canales que ya son enums
        Set<NotificationChannelType> channelEnums = command.channels();
            
        // Crear el objeto de contenido
        NotificationContentVO content = NotificationContentVO.of(command.content());
        
        // Crear el objeto de destinatario
        RecipientVO recipient = new RecipientVO.Builder()
            .withUserId(command.recipientId() != null ? UUID.fromString(command.recipientId()) : null)
            .withEmail(command.recipientEmail())
            .build();
        
        // Usamos directamente la prioridad que ya es un enum
        NotificationPriorityType priority = command.priority();
        
        // Convertir el servicio de origen de string a enum si existe
        SourceServiceType sourceService = null;
        if (command.sourceService() != null) {
            try {
                sourceService = SourceServiceType.valueOf(command.sourceService());
            } catch (IllegalArgumentException e) {
                // Si el servicio no es válido, se usa el valor por defecto (null)
            }
        }
        
        return new Notification.Builder()
            .withSubject(command.subject())
            .withContent(content)
            .withRequesterId(command.recipientId() != null ? UUID.fromString(command.recipientId()) : null)            
            .withRecipient(recipient)
            .withChannels(channelEnums)
            .withPriority(priority)
            .withScheduledFor(command.scheduledFor())
            .withSourceService(sourceService)
            .build();
    }
}
