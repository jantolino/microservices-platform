package edu.market.cart.domain.model;

import edu.market.cart.domain.enums.CartStatus;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.vo.CouponAppliedVO;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de dominio para el carrito de compras.
 *
 * Este modelo representa el carrito de un usuario o sesión. Los ítems, cupones,
 * totales y estado se gestionan como parte del agregado y se utilizan en el
 * flujo de compra.
 *
 * La mutación es controlada: se exponen métodos explícitos para reemplazar
 * ítems, cupones, totales o estado, actualizando updatedAt y validando con
 * CartException cuando faltan datos requeridos.
 */
public class Cart {

    private final UUID id;
    private final UUID userId;
    private final String sessionId;
    private CartStatus status;
    private List<CartItem> items;
    private CartTotals totals;
    private List<CouponAppliedVO> coupons;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Constructor con todos los campos.
     *
     * @param id          Identificador único del carrito
     * @param userId      Identificador del usuario propietario
     * @param sessionId   Identificador de sesión
     * @param status      Estado del carrito (ACTIVE, LOCKED, CHECKOUT_READY)
     * @param items       Ítems agregados al carrito
     * @param totals      Totales calculados (subtotal, descuentos, impuestos, envío, total)
     * @param coupons     Cupones o promociones aplicadas
     * @param createdAt   Fecha y hora de creación
     * @param updatedAt   Fecha y hora de última actualización
     */
    private Cart(UUID id,
                 UUID userId,
                 String sessionId,
                 CartStatus status,
                 List<CartItem> items,
                 CartTotals totals,
                 List<CouponApplied> coupons,
                 OffsetDateTime createdAt,
                 OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.status = status;
        this.items = List.copyOf(items);
        this.totals = totals;
        this.coupons = List.copyOf(coupons);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validate();
    }

    /**
     * Valida los invariantes del agregado y lanza CartException si falta algún dato requerido.
     */
    private void validate() {
        if (userId == null) {
            throw new CartException("userId required");
        }
        if (sessionId == null) {
            throw new CartException("sessionId required");
        }
        if (items == null) {
            throw new CartException("items required");
        }
        if (coupons == null) {
            throw new CartException("coupons required");
        }
        if (totals == null) {
            throw new CartException("totals required");
        }
    }

    /**
     * Inicia la construcción del carrito mediante el builder.
     *
     * @return instancia del builder de Cart
     */
    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public CartStatus getStatus() {
        return status;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public CartTotals getTotals() {
        return totals;
    }

    public List<CouponAppliedVO> getCoupons() {
        return Collections.unmodifiableList(coupons);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Reemplaza los ítems del carrito y actualiza updatedAt.
     *
     * @param newItems Ítems a establecer
     */
    public void replaceItems(List<CartItem> newItems) {
        if (newItems == null) {
            throw new CartException("items required");
        }
        this.items = List.copyOf(newItems);
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Reemplaza los cupones aplicados y actualiza updatedAt.
     *
     * @param newCoupons Cupones a establecer
     */
    public void replaceCoupons(List<CouponAppliedVO> newCoupons) {
        if (newCoupons == null) {
            throw new CartException("coupons required");
        }
        this.coupons = List.copyOf(newCoupons);
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Reemplaza los totales calculados y actualiza updatedAt.
     *
     * @param newTotals Totales a establecer
     */
    public void replaceTotals(CartTotals newTotals) {
        if (newTotals == null) {
            throw new CartException("totals required");
        }
        this.totals = newTotals;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Cambia el estado del carrito y refresca updatedAt.
     *
     * @param newStatus Estado a establecer
     */
    public void changeStatus(CartStatus newStatus) {
        if (newStatus == null) {
            throw new CartException("status required");
        }
        this.status = newStatus;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Builder para instanciar un Cart de forma controlada.
     */
    public static class Builder {
        private UUID id;
        private UUID userId;
        private String sessionId;
        private CartStatus status = CartStatus.ACTIVE;
        private List<CartItem> items = Collections.emptyList();
        private CartTotals totals = CartTotals.zero();
        private List<CouponAppliedVO> coupons = Collections.emptyList();
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withStatus(CartStatus status) {
            this.status = status;
            return this;
        }

        public Builder withItems(List<CartItem> items) {
            this.items = items;
            return this;
        }

        public Builder withTotals(CartTotals totals) {
            this.totals = totals;
            return this;
        }

        public Builder withCoupons(List<CouponAppliedVO> coupons) {
            this.coupons = coupons;
            return this;
        }

        public Builder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Cart build() {
            return new Cart(
                    id != null ? id : UUID.randomUUID(),
                    userId,
                    sessionId,
                    status != null ? status : CartStatus.ACTIVE,
                    items != null ? items : Collections.emptyList(),
                    totals != null ? totals : CartTotals.zero(),
                    coupons != null ? coupons : Collections.emptyList(),
                    createdAt != null ? createdAt : OffsetDateTime.now(),
                    updatedAt != null ? updatedAt : OffsetDateTime.now()
            );
        }
    }
}
