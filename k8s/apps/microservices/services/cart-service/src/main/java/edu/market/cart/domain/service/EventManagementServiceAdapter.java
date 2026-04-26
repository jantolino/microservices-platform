package edu.market.cart.domain.service;

import edu.market.cart.domain.enums.CartEventType;
import edu.market.cart.domain.enums.EventStatusType;
import edu.market.cart.domain.model.TransactionalOutbox;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.service.EventManagementServicePort;
import edu.market.cart.domain.port.output.service.SerializationServicePort;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de eventos del dominio.
 * Proporciona funcionalidades para manejar el ciclo de vida de los eventos,
 * sincronizar estados entre entidades y gestionar la lógica de procesamiento de eventos.
 */
public class EventManagementServiceAdapter implements EventManagementServicePort {
    
    private final SerializationServicePort serialization;
    private final LoggingPort log;
    
   
    public EventManagementServiceAdapter(SerializationServicePort serialization, LoggingPort log) {
        this.serialization = serialization;
        this.log = log;
    }
    
    /**
     * Filtra una lista de eventos para obtener solo aquellos que están listos para ser procesados.
     * Esto incluye eventos no programados y eventos programados cuya fecha de programación ya ha pasado.
     *
     * @param events Lista de eventos a filtrar
     * @return Lista de eventos listos para procesar
     */
    @Override
    public List<TransactionalOutbox> filterEventsReadyToProcess(List<TransactionalOutbox> events) {
        log.info("filterEventsReadyToProcess - Init");
        
        if (events == null || events.isEmpty()) {
            log.debug("filterEventsReadyToProcess - Empty events list, returning empty list");
            log.info("filterEventsReadyToProcess - End");
            return Collections.emptyList();
        }
        
        LocalDateTime now = LocalDateTime.now();
        log.debug("filterEventsReadyToProcess - Current time: {}", now);
        
        List<TransactionalOutbox> result = events.stream()
                .filter(event -> {
                    // Excluir eventos que ya están siendo procesados, fueron enviados o cancelados
                    if (event.getStatusType() == EventStatusType.PROCESSING || 
                        event.getStatusType() == EventStatusType.SENT || 
                        event.getStatusType() == EventStatusType.CANCELLED) {
                        log.debug("filterEventsReadyToProcess - Event {} excluded from processing due to status {}", 
                                  event.getId(), event.getStatusType());
                        return false;
                    }
                    
                    // Si es un evento programado (SCHEDULED), verificar si ya pasó la fecha programada
                    if (event.getStatusType() == EventStatusType.SCHEDULED) {
                        LocalDateTime scheduledTime = extractScheduledTimeFromPayload(event.getPayload());
                        if (scheduledTime == null) {
                            log.warn("filterEventsReadyToProcess - Event {} with SCHEDULED status but without valid scheduled date", event.getId());
                            return false;
                        }
                        boolean isReady = !scheduledTime.isAfter(now);
                        if (!isReady) {
                            log.debug("filterEventsReadyToProcess - Scheduled event {} not ready yet, scheduled for {}", event.getId(), scheduledTime);
                        }
                        return isReady;
                    }
                    
                    // Los eventos PENDING y FAILED se procesan inmediatamente
                    log.debug("filterEventsReadyToProcess - Event {} with status {} ready to process", event.getId(), event.getStatusType());
                    return true;
                })
                .collect(Collectors.toList());
        
        log.info("filterEventsReadyToProcess - End");
        return result;
    }
    
    /**
     * Valida si una transición de estado es permitida según las reglas de negocio.
     * Las transiciones válidas se basan en el flujo de procesamiento de eventos:
     * - PENDING puede pasar a PROCESSING (cuando se procesa), SCHEDULED (cuando se programa), FAILED (si falla) o CANCELLED (si se cancela)
     * - SCHEDULED puede pasar a PROCESSING (cuando llega su hora), FAILED (si falla) o CANCELLED (si se cancela)
     * - PROCESSING puede pasar a SENT (si se envía correctamente), FAILED (si falla) o CANCELLED (si se cancela)
     * - FAILED puede pasar a PROCESSING (reintentos) o CANCELLED (si se abandona)
     * - SENT y CANCELLED son estados terminales
     *
     * @param currentStatus Estado actual
     * @param newStatus Estado propuesto
     * @return true si la transición es válida, false en caso contrario
     */
    @Override
    public boolean isValidStatusTransition(EventStatusType currentStatus, EventStatusType newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        
        // Definir las transiciones válidas según las reglas de negocio
        switch (currentStatus) {
            case PENDING:
                // Desde PENDING se puede pasar a PROCESSING, SCHEDULED, CANCELLED o FAILED
                return newStatus == EventStatusType.PROCESSING || 
                       newStatus == EventStatusType.SCHEDULED || 
                       newStatus == EventStatusType.CANCELLED || 
                       newStatus == EventStatusType.FAILED;
                
            case SCHEDULED:
                // Desde SCHEDULED se puede pasar a PROCESSING, CANCELLED o FAILED
                return newStatus == EventStatusType.PROCESSING || 
                       newStatus == EventStatusType.CANCELLED || 
                       newStatus == EventStatusType.FAILED;
                
            case PROCESSING:
                // Desde PROCESSING se puede pasar a SENT, FAILED o CANCELLED
                return newStatus == EventStatusType.SENT || 
                       newStatus == EventStatusType.FAILED || 
                       newStatus == EventStatusType.CANCELLED;
                
            case FAILED:
                // Desde FAILED se puede reintentar (PROCESSING) o cancelar definitivamente
                return newStatus == EventStatusType.PROCESSING || 
                       newStatus == EventStatusType.CANCELLED;
                
            case SENT:
            case CANCELLED:
                // Un evento enviado o cancelado no debería cambiar de estado
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * Filtra y ordena una lista de eventos según su prioridad y otros factores relevantes.
     * Excluye eventos que no deben ser procesados (PROCESSING, SENT, CANCELLED).
     *
     * @param events Lista de eventos a ordenar
     * @return Lista filtrada y ordenada de eventos listos para procesar
     */
    @Override
    public List<TransactionalOutbox> sortEventsByPriority(List<TransactionalOutbox> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Filtrar eventos que no deben ser procesados
        return events.stream()
                // Excluir eventos que ya están siendo procesados o que ya fueron completados/cancelados
                .filter(event -> {
                    EventStatusType status = event.getStatusType();
                    return status != EventStatusType.PROCESSING && 
                           status != EventStatusType.SENT && 
                           status != EventStatusType.CANCELLED;
                })
                // Ordenar eventos por tipo de estado (prioridad implícita) y fecha de creación
                .sorted(Comparator
                        // Prioridad por tipo de estado (orden personalizado)
                        .comparing(TransactionalOutbox::getStatusType, this::compareEventStatusPriority)
                        // Luego por fecha de creación (más antiguos primero)
                        .thenComparing(TransactionalOutbox::getCreatedAt))
                .collect(Collectors.toList());
    }
    
    /**
     * Método auxiliar para comparar la prioridad de los tipos de estado de eventos.
     * Define el orden de prioridad para el procesamiento de eventos.
     * Nota: Los estados PROCESSING, SENT y CANCELLED ya son filtrados antes de la ordenación.
     * 
     * @param status1 Primer estado a comparar
     * @param status2 Segundo estado a comparar
     * @return Valor negativo si status1 tiene mayor prioridad, positivo si status2 tiene mayor prioridad
     */
    private int compareEventStatusPriority(EventStatusType status1, EventStatusType status2) {
        // Definir el orden de prioridad (menor número = mayor prioridad)
        Map<EventStatusType, Integer> priorityMap = new HashMap<>();
        priorityMap.put(EventStatusType.PENDING, 1);    // Mayor prioridad para eventos pendientes
        priorityMap.put(EventStatusType.FAILED, 2);     // Luego eventos fallidos
        priorityMap.put(EventStatusType.SCHEDULED, 3);  // Menor prioridad para eventos programados
        
        // Obtener prioridad numérica (valor por defecto 99 para estados desconocidos)
        int priority1 = priorityMap.getOrDefault(status1, 99);
        int priority2 = priorityMap.getOrDefault(status2, 99);
        
        // Comparar prioridades
        return Integer.compare(priority1, priority2);
    }
    
    /**
     * Agrupa eventos relacionados para procesamiento conjunto.
     *
     * @param events Lista de eventos a agrupar
     * @return Mapa de eventos agrupados por algún criterio (ej: tipo, agregado)
     */
    @Override
    public Map<String, List<TransactionalOutbox>> groupRelatedEvents(List<TransactionalOutbox> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Agrupar eventos por ID de agregado
        return events.stream()
                .filter(event -> event.getAggregateId() != null && !event.getAggregateId().isEmpty())
                .collect(Collectors.groupingBy(TransactionalOutbox::getAggregateId));
    }
    
    /**
     * Determina si un evento debe ser reintentado según la política de reintentos.
     *
     * @param event Evento a evaluar
     * @param maxRetries Número máximo de reintentos permitidos
     * @return true si el evento debe reintentarse, false en caso contrario
     */
    @Override
    public boolean shouldRetryEvent(TransactionalOutbox event, int maxRetries) {
        if (event == null) {
            return false;
        }
        
        // No reintentar eventos que ya han sido procesados o cancelados
        if (event.isProcessed() || event.getStatusType() == EventStatusType.CANCELLED) {
            return false;
        }
        
        // Verificar si se ha excedido el límite de reintentos
        return event.getRetryCount() <= maxRetries;
    }
    
    
    /**
     * Extrae un identificador relevante del payload de un evento de carrito.
     *
     * @param payload Contenido serializado del evento
     * @param eventType Tipo de evento para determinar la estrategia de deserialización
     * @return ID relevante o null si no se puede extraer
     */
    @Override
    public UUID extractEventFromPayload(String payload, CartEventType eventType) {
        log.info("extractEventFromPayload - Init");
        
        if (payload == null || payload.isEmpty()) {
            log.debug("extractEventFromPayload - Payload is null or empty");
            log.info("extractEventFromPayload - End");
            return null;
        }
        
        try {
            log.debug("extractEventFromPayload - Processing payload for event type: {}", eventType);
            // Para ahora, usamos enfoque genérico: buscar campo aggregateId o id
            return extractIdFromGenericPayload(payload);
        } catch (Exception e) {
            log.warn("extractEventFromPayload - Error extracting ID from payload: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae el ID de un payload genérico usando un Map.
     * Este es el enfoque de respaldo cuando no se puede usar una deserialización específica.
     *
     * @param payload Contenido serializado del evento
     * @return ID relevante o null si no se puede extraer
     */
    private UUID extractIdFromGenericPayload(String payload) {
        
        log.info("extractIdFromGenericPayload - Init");
        
        try {
            // Convertir el payload a un Map para poder acceder a sus propiedades
            log.debug("extractIdFromGenericPayload - Deserializing payload to Map");
            Map<String, Object> payloadMap = serialization.deserialize(payload, Map.class);
            
            // Intentar con los campos más comunes
            for (String key : List.of("aggregateId", "cartId", "id")) {
                if (payloadMap.containsKey(key) && payloadMap.get(key) != null) {
                    String idStr = payloadMap.get(key).toString();
                    UUID id = UUID.fromString(idStr);
                    log.debug("extractIdFromGenericPayload - ID extracted from {} field: {}", key, id);                
                    return id;
                }
            }
            
            log.debug("extractIdFromGenericPayload - Could not extract ID from payload");            
            return null;
        } catch (Exception e) {
            log.warn("extractIdFromGenericPayload - Error extracting ID from generic payload: {}", e.getMessage());            
            return null;
        }
    }
    
    /**
     * Extrae la fecha programada del payload de un evento (si aplica).
     *
     * @param payload Contenido serializado del evento
     * @return Fecha programada o null si no se puede extraer o no está presente
     */
    @Override
    public LocalDateTime extractScheduledTimeFromPayload(String payload) {
        
        log.info("extractScheduledTimeFromPayload - Init");
        
        if (payload == null || payload.isEmpty()) {
            log.debug("extractScheduledTimeFromPayload - Payload is null or empty");            
            return null;
        }
        
        try {
            log.debug("extractScheduledTimeFromPayload - Deserializing payload to Map");
            Map<String, Object> payloadMap = serialization.deserialize(payload, Map.class);
            if (payloadMap.containsKey("scheduledTime") && payloadMap.get("scheduledTime") != null) {
                String scheduled = payloadMap.get("scheduledTime").toString();
                LocalDateTime parsed = LocalDateTime.parse(scheduled);
                log.debug("extractScheduledTimeFromPayload - Scheduled date extracted from payload: {}", parsed);                
                return parsed;
            }
            
            log.debug("extractScheduledTimeFromPayload - Could not extract scheduled date from payload");            
            return null;
        } catch (Exception e) {
            // Si hay un error al deserializar o al acceder a la fecha, devolver null
            log.warn("extractScheduledTimeFromPayload - Error extracting scheduled date from payload: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Valida que el payload contenga toda la información necesaria.
     *
     * @param payload Contenido serializado del evento
     * @param requiredFields Campos requeridos en el payload
     * @return true si el payload es válido, false en caso contrario
     */
    @Override
    public boolean validatePayload(String payload, List<String> requiredFields) {
        log.info("validatePayload - Init");
        
        if (payload == null || payload.isEmpty() || requiredFields == null || requiredFields.isEmpty()) {
            log.debug("validatePayload - Invalid input parameters");            
            return false;
        }
        
        try {
            // Convertir el payload a un Map para poder acceder a sus propiedades
            log.debug("validatePayload - Deserializing payload to Map");
            Map<String, Object> payloadMap = serialization.deserialize(payload, Map.class);
            
            // Verificar que todos los campos requeridos estén presentes y no sean nulos
            log.debug("validatePayload - Checking required fields: {}", requiredFields);
            for (String field : requiredFields) {
                if (!payloadMap.containsKey(field) || payloadMap.get(field) == null) {
                    log.debug("validatePayload - Required field '{}' not found or null in payload", field);                    
                    return false;
                }
            }
            
            log.debug("validatePayload - All required fields are present");            
            return true;
        } catch (Exception e) {
            // Si hay un error al procesar el JSON, el payload no es válido
            log.warn("validatePayload - Error validating payload: {}", e.getMessage());
            return false;
        }
    }
}