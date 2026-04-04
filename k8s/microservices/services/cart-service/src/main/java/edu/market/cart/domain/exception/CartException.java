package edu.market.cart.domain.exception;

/**
 * Excepción general del dominio de carrito para validaciones y reglas de negocio.
 */
public class CartException extends DomainException {
    
    public CartException(String message) {
        super(message);
    }
    
    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
