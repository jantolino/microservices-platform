package edu.market.cart.domain.model;

import edu.market.cart.domain.exception.CartException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Modelo de dominio para un ítem dentro del carrito.
 *
 * Representa la línea de carrito con identificadores de producto, cantidad y precios
 * asociados.
 *
 * Mutación controlada: los cambios se realizan creando una nueva instancia via builder.
 */
public class CartItem {

    private final UUID id;
    private final String productId;
    private final String variantId;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    /**
     * Constructor con todos los campos.
     *
     * @param id        Identificador único del ítem
     * @param productId Identificador del producto
     * @param variantId Identificador de variante (opcional)
     * @param quantity  Cantidad solicitada (debe ser > 0)
     * @param unitPrice Precio unitario
     * @param lineTotal Total de la línea (unitPrice * quantity - descuentos)
     */
    private CartItem(UUID id,
                     String productId,
                     String variantId,
                     int quantity,
                     BigDecimal unitPrice,
                     BigDecimal lineTotal) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        validate();
    }

    private void validate() {
        if (productId == null || productId.isBlank()) {
            throw new CartException("productId required");
        }
        if (quantity <= 0) {
            throw new CartException("quantity must be > 0");
        }
        if (unitPrice == null) {
            throw new CartException("unitPrice required");
        }
        if (lineTotal == null) {
            throw new CartException("lineTotal required");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getVariantId() {
        return variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    /**
     * Inicia la construcción del ítem.
     *
     * @return builder de CartItem
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder para instanciar CartItem de forma controlada.
     */
    public static class Builder {
        private UUID id;
        private String productId;
        private String variantId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder withVariantId(String variantId) {
            this.variantId = variantId;
            return this;
        }

        public Builder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder withUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder withLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
            return this;
        }

        public CartItem build() {
            return new CartItem(
                    id != null ? id : UUID.randomUUID(),
                    productId,
                    variantId,
                    quantity,
                    unitPrice,
                    lineTotal != null ? lineTotal : (unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(Math.max(quantity, 0))) : null)
            );
        }
    }
}
