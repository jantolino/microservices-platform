package edu.market.cart.domain.exception;

/**
 * Excepción específica para errores en el Value Object Money.
 */
public class MoneyException extends DomainException {

    public MoneyException(String message) {
        super(message);
    }

    public MoneyException(String message, Throwable cause) {
        super(message, cause);
    }
}
