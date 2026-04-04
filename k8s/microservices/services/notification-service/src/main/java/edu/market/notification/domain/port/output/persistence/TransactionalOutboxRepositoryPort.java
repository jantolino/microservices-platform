package edu.market.notification.domain.port.output.persistence;

import edu.market.notification.domain.model.TransactionalOutbox;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para el repositorio de eventos outbox.
 * 
 * Este puerto define las operaciones necesarias para implementar el patrón Transactional Outbox,
 * permitiendo guardar y recuperar eventos que deben ser procesados de forma asíncrona.
 */
public interface TransactionalOutboxRepositoryPort {
    
    /**
     * Guarda un evento en la tabla outbox.
     * 
     * @param event El evento a guardar
     * @return El evento guardado
     */
    TransactionalOutbox save(TransactionalOutbox event);

    /*
     * Busca un evento por su ID.
     * 
     * @param uuid ID del evento a buscar
     * @return Optional con el evento si existe, vacío si no
     */
    Optional<TransactionalOutbox> findByUuid(UUID uuid);
    
    /**
     * Encuentra eventos no procesados.
     * 
     * @return Lista de eventos pendientes de procesar
     */
    List<TransactionalOutbox> findByProcessedFalse();
    
    /**
     * Encuentra eventos no procesados de un tipo específico.
     * 
     * @param eventType El tipo de evento a buscar
     * @return Lista de eventos pendientes de procesar del tipo especificado
     */
    List<TransactionalOutbox> findByProcessedFalseAndEventType(String eventType);
    
    /**
     * Encuentra eventos no procesados con un número de reintentos menor que el máximo especificado.
     * 
     * @param maxRetries El número máximo de reintentos permitidos
     * @return Lista de eventos pendientes de procesar con reintentos por debajo del límite
     */
    List<TransactionalOutbox> findByProcessedFalseAndRetryCountLessThan(int maxRetries);
    
    /**
     * Encuentra eventos no procesados con un número de reintentos mayor o igual que el máximo especificado.
     * 
     * @param maxRetries El número máximo de reintentos permitidos
     * @return Lista de eventos que han excedido el límite de reintentos
     */
    List<TransactionalOutbox> findByProcessedFalseAndRetryCountGreaterThanEqual(int maxRetries);
        
    /**
     * Elimina eventos que ya han sido procesados y son más antiguos que un período determinado.
     * 
     * @param daysOld Número de días de antigüedad para considerar un evento como eliminable
     * @return Número de eventos eliminados
     */
    int deleteProcessedEventsBefore(int daysOld);

    
}
