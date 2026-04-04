package edu.market.cart.domain.exception;

/** Excepción base para todas las excepciones del dominio de carrito. */
public abstract class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
