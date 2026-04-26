package edu.market.cart.domain.port.output.persistence;

import edu.market.cart.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia para el agregado Cart.
 * Define operaciones CRUD necesarias para la capa de dominio sin exponer detalles de infraestructura.
 */
public interface CartRepositoryPort {

    /**
     * Persiste o actualiza un carrito.
     *
     * @param cart agregado a guardar
     * @return carrito guardado
     */
    Cart save(Cart cart);

    /**
     * Busca un carrito por su identificador.
     *
     * @param cartId id del carrito
     * @return Optional con el carrito si existe
     */
    Optional<Cart> findById(UUID cartId);

    /**
     * Busca un carrito activo de un usuario.
     *
     * @param userId id del usuario
     * @return Optional con el carrito activo
     */
    Optional<Cart> findActiveByUserId(UUID userId);

    /**
     * Busca un carrito activo por sesión invitada.
     *
     * @param sessionId id de sesión
     * @return Optional con el carrito activo
     */
    Optional<Cart> findActiveBySessionId(String sessionId);

    /**
     * Elimina un carrito.
     *
     * @param cartId id del carrito
     */
    void deleteById(UUID cartId);

    /**
     * Busca un carrito con bloqueo pesimista.
     *
     * @param cartId id del carrito
     * @return Optional con el carrito bloqueado
     */
    Optional<Cart> findByIdWithLock(UUID cartId);

    /**
     * Elimina todos los carritos de un usuario.
     *
     * @param userId id del usuario
     */
    void deleteByUserId(UUID userId);

    /**
     * Elimina todos los carritos de una sesión invitada.
     *
     * @param sessionId id de sesión
     */
    void deleteBySessionId(String sessionId);
}
