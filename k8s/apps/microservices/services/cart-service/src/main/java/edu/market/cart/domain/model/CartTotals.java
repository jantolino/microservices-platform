package edu.market.cart.domain.model;

import edu.market.cart.domain.exception.CartException;

import java.math.BigDecimal;

/**
 * Modelo de dominio para los totales del carrito.
 *
 * Contiene subtotal, descuentos, impuestos, envío y total calculado.
 * Los valores deben ser no nulos y consistentes.
 */
public class CartTotals {

    private final BigDecimal subtotal;
    private final BigDecimal discounts;
    private final BigDecimal taxes;
    private final BigDecimal shipping;
    private final BigDecimal total;

    /**
     * Constructor con todos los campos.
     *
     * @param subtotal  Subtotal de los ítems
     * @param discounts Descuentos aplicados
     * @param taxes     Impuestos
     * @param shipping  Envío estimado
     * @param total     Total final
     */
    private CartTotals(BigDecimal subtotal,
                       BigDecimal discounts,
                       BigDecimal taxes,
                       BigDecimal shipping,
                       BigDecimal total) {
        this.subtotal = subtotal;
        this.discounts = discounts;
        this.taxes = taxes;
        this.shipping = shipping;
        this.total = total;
        validate();
    }

    private void validate() {
        if (subtotal == null || discounts == null || taxes == null || shipping == null || total == null) {
            throw new CartException("totals fields required");
        }
    }

    public static CartTotals zero() {
        return new CartTotals(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscounts() {
        return discounts;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /**
     * Inicia la construcción de los totales.
     *
     * @return builder de CartTotals
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder para instanciar CartTotals de forma controlada.
     */
    public static class Builder {
        private BigDecimal subtotal = BigDecimal.ZERO;
        private BigDecimal discounts = BigDecimal.ZERO;
        private BigDecimal taxes = BigDecimal.ZERO;
        private BigDecimal shipping = BigDecimal.ZERO;
        private BigDecimal total = BigDecimal.ZERO;

        public Builder withSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder withDiscounts(BigDecimal discounts) {
            this.discounts = discounts;
            return this;
        }

        public Builder withTaxes(BigDecimal taxes) {
            this.taxes = taxes;
            return this;
        }

        public Builder withShipping(BigDecimal shipping) {
            this.shipping = shipping;
            return this;
        }

        public Builder withTotal(BigDecimal total) {
            this.total = total;
            return this;
        }

        public CartTotals build() {
            return new CartTotals(subtotal, discounts, taxes, shipping, total);
        }
    }
}
