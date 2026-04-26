package edu.market.userservice.application.port.input;


/**
 * Puerto de entrada para el caso de uso de eliminación de usuario
 */
public interface DeleteUserUseCasePort {
    
    /**
     * Elimina un usuario del sistema
     * 
     * @param userId Value Object con el ID del usuario a eliminar
     * @return true si el usuario fue eliminado correctamente
     */
    boolean execute(Long userId);
}
