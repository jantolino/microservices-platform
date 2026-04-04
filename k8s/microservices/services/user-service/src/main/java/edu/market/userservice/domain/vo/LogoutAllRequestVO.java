package edu.market.userservice.domain.vo;

/**
 * Value Object para la solicitud de cierre de todas las sesiones de un usuario
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class LogoutAllRequestVO {
    
    private final Long userId;
    
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Constructor para la solicitud de cierre de todas las sesiones
     * 
     * @param userId ID del usuario cuyas sesiones se cerrarán
     */
    public LogoutAllRequestVO(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario debe ser un valor positivo");
        }
        this.userId = userId;
    }
}
