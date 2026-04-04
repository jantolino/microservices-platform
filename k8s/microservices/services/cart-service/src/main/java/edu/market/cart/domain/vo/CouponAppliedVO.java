package edu.market.cart.domain.vo;

import edu.market.cart.domain.exception.CouponAppliedException;

import java.time.OffsetDateTime;

/**
 * Value Object para cupones aplicados.
 *
 * Sigue el patrón de validación compacto de NotificationContentVO: valida en
 * el constructor y garantiza inmutabilidad (record).
 */
public record CouponAppliedVO(String code,
                              String description,
                              OffsetDateTime expiresAt,
                              boolean percentage,
                              double value) {

    public CouponAppliedVO {
        if (code == null || code.isBlank()) {
            throw new CouponAppliedException("code required");
        }
        if (value <= 0) {
            throw new CouponAppliedException("value must be > 0");
        }
    }

    public static CouponAppliedVO percentage(String code, String description, OffsetDateTime expiresAt, double percent) {
        return new CouponAppliedVO(code, description, expiresAt, true, percent);
    }

    public static CouponAppliedVO amount(String code, String description, OffsetDateTime expiresAt, double amount) {
        return new CouponAppliedVO(code, description, expiresAt, false, amount);
    }
}
