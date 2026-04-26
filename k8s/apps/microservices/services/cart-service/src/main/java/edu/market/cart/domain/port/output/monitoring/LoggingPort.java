package edu.market.cart.domain.port.output.monitoring;

/**
 * Puerto para el logging en el dominio.
 * 
 * Patrones de diseño implementados:
 * - Ports and Adapters (Hexagonal Architecture): Define una interfaz para servicios externos al dominio
 * - Dependency Inversion: El dominio depende de abstracciones, no de implementaciones concretas
 * - Facade: Simplifica la interfaz de logging para el dominio
 * - Cross-Cutting Concern: Maneja una preocupación transversal (logging) de manera consistente
 */
public interface LoggingPort {
    
    // Métodos para mensajes simples
    void info(String message);
    void debug(String message);
    void warn(String message);
    void error(String message);
    void error(String message, Throwable throwable);
    
    // Métodos para mensajes con formato
    /**
     * Registra un mensaje de nivel INFO con formato.
     * 
     * @param format Formato del mensaje (compatible con String.format)
     * @param args Argumentos para el formato
     */
    void info(String format, Object... args);
    
    /**
     * Registra un mensaje de nivel DEBUG con formato.
     * 
     * @param format Formato del mensaje (compatible con String.format)
     * @param args Argumentos para el formato
     */
    void debug(String format, Object... args);
    
    /**
     * Registra un mensaje de nivel WARN con formato.
     * 
     * @param format Formato del mensaje (compatible con String.format)
     * @param args Argumentos para el formato
     */
    void warn(String format, Object... args);
    
    /**
     * Registra un mensaje de nivel ERROR con formato.
     * 
     * @param format Formato del mensaje (compatible con String.format)
     * @param args Argumentos para el formato
     */
    void error(String format, Object... args);
    
    /**
     * Registra un mensaje de nivel ERROR con formato y excepción.
     * 
     * @param format Formato del mensaje (compatible con String.format)
     * @param throwable Excepción a registrar
     * @param args Argumentos para el formato
     */
    void error(String format, Throwable throwable, Object... args);
}
