package edu.market.notification.domain.port.output.event;


/**
 * Puerto de salida para la publicación de eventos de dominio.
 * 
 * Patrones de diseño implementados:
 * - Ports and Adapters (Hexagonal Architecture): Define una interfaz para servicios externos al dominio
 * - Event-Driven Architecture: Permite la comunicación asíncrona entre componentes mediante eventos
 * - Transactional Outbox: Garantiza la consistencia entre la transacción de dominio y la publicación de eventos
 * - Observer: Los eventos son publicados y pueden ser consumidos por observadores
 * - Dependency Inversion: El dominio depende de abstracciones, no de implementaciones concretas
 */
public interface EventPublisherPort {
    
    /**
     * Publica un evento de dominio
     * @param event Evento a publicar
     */
    void publish();

    /**
     * Publica un evento de dominio
     * @param event Evento a publicar
     */
    void publishCallback();
    
    /**
     * Verifica si el publicador de eventos está disponible
     * @return true si está disponible
     */
    boolean isAvailable();
}
