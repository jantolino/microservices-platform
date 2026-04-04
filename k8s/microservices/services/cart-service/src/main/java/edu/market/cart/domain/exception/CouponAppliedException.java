package edu.market.cart.domain.exception;

/**
 * Excepción específica para errores en el Value Object CouponApplied.
 */
public class CouponAppliedException extends DomainException {

    public CouponAppliedException(String message) {
        super(message);
    }

    public CouponAppliedException(String message, Throwable cause) {
        super(message, cause);
    }
}
