package edu.market.cart.domain.vo;

import edu.market.cart.domain.exception.MoneyException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object monetario inmutable.
 *
 * Sigue el estilo de validación compacto de NotificationContentVO: valida en el
 * constructor y asegura inmutabilidad.
 */
public record MoneyVO(BigDecimal amount, String currency) {

    public MoneyVO {
        if (amount == null) {
            throw new MoneyException("amount required");
        }
        if (currency == null || currency.isBlank()) {
            throw new MoneyException("currency required");
        }
    }

    public MoneyVO add(MoneyVO other) {
        ensureSameCurrency(other);
        return new MoneyVO(amount.add(other.amount), currency);
    }

    public MoneyVO subtract(MoneyVO other) {
        ensureSameCurrency(other);
        return new MoneyVO(amount.subtract(other.amount), currency);
    }

    private void ensureSameCurrency(MoneyVO other) {
        if (!Objects.equals(currency, other.currency)) {
            throw new MoneyException("currency mismatch");
        }
    }
}
