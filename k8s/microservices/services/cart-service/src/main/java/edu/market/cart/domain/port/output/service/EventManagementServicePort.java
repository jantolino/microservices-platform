package edu.market.cart.domain.port.output.service;

import edu.market.cart.domain.enums.EventStatusType;
import edu.market.cart.domain.enums.CartEventType;
import edu.market.cart.domain.model.TransactionalOutbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de salida para la gestión de eventos del carrito.
 * Define operaciones de soporte para manejar el ciclo de vida de eventos de carrito
 * (outbox y publicaciones) en colaboración con un adaptador externo.
 */
public interface EventManagementServicePort {
    
    /**
     * 1. GESTIÓN DE EVENTOS PROGRAMADOS
     */
    
    /**
     * Filtra una lista de eventos para obtener solo aquellos que están listos para ser procesados.
     * Esto incluye eventos no programados y eventos programados cuya fecha de programación ya ha pasado.
     *
     * @param events Lista de eventos a filtrar
     * @return Lista de eventos listos para procesar
     */
    List<TransactionalOutbox> filterEventsReadyToProcess(List<TransactionalOutbox> events);    
    
    
    /**
     * Valida si una transición de estado es permitida según las reglas de negocio.
     *
     * @param currentStatus Estado actual
     * @param newStatus Estado propuesto
     * @return true si la transición es válida, false en caso contrario
     */
    boolean isValidStatusTransition(EventStatusType currentStatus, EventStatusType newStatus);
    
    /**
     * 3. PROCESAMIENTO DE EVENTOS POR PRIORIDAD
     */
    
    /**
     * Ordena una lista de eventos según su prioridad y otros factores relevantes.
     *
     * @param events Lista de eventos a ordenar
     * @return Lista ordenada de eventos
     */
    List<TransactionalOutbox> sortEventsByPriority(List<TransactionalOutbox> events);
    
    /**
     * Agrupa eventos relacionados para procesamiento conjunto.
     *
     * @param events Lista de eventos a agrupar
     * @return Mapa de eventos agrupados por algún criterio (ej: tipo, agregado)
     */
    Map<String, List<TransactionalOutbox>> groupRelatedEvents(List<TransactionalOutbox> events);
    
    /**
     * 4. MANEJO DE REINTENTOS Y ERRORES
     */
    
    /**
     * Determina si un evento debe ser reintentado según la política de reintentos.
     *
     * @param event Evento a evaluar
     * @param maxRetries Número máximo de reintentos permitidos
     * @return true si el evento debe reintentarse, false en caso contrario
     */
    boolean shouldRetryEvent(TransactionalOutbox event, int maxRetries);
    
    
    /**
     * 5. EXTRACCIÓN Y VALIDACIÓN DE DATOS
     */
    
    /**
     * Extrae un identificador relevante del payload según el tipo de evento de carrito.
     *
     * @param payload Contenido serializado del evento
     * @param eventType Tipo de evento para determinar la estrategia de deserialización
     * @return ID relevante o null si no se puede extraer
     */
    UUID extractEventFromPayload(String payload, CartEventType eventType);
    
    /**
     * Extrae la fecha programada del payload de un evento de carrito, si aplica.
     *
     * @param payload Contenido serializado del evento
     * @return Fecha programada o null si no se puede extraer o no está presente
     */
    LocalDateTime extractScheduledTimeFromPayload(String payload);
    
    /**
     * Valida que el payload contenga toda la información necesaria.
     *
     * @param payload Contenido serializado del evento
     * @param requiredFields Campos requeridos en el payload
     * @return true si el payload es válido, false en caso contrario
     */
    boolean validatePayload(String payload, List<String> requiredFields);
}
