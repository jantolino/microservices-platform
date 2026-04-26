package edu.market.userservice.application.usecase.user;

import edu.market.userservice.application.port.input.DeleteUserUseCasePort;
import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.domain.port.output.UserRepositoryPort;

/**
 * Caso de uso para eliminar un usuario
 */
public class DeleteUserUseCase implements DeleteUserUseCasePort {
    
    private final UserRepositoryPort userRepository;
    
    public DeleteUserUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Elimina un usuario del sistema
     * 
     * @param userId Value Object con el ID del usuario a eliminar
     * @return true si el usuario fue eliminado correctamente
     */
    @Override
    public boolean execute(Long userId) {      
        
        // Verificar que el usuario existe antes de eliminarlo
        if (userRepository.findById(userId).isEmpty()) {
            throw UserException.userNotFound(userId);
        }
        
        userRepository.deleteById(userId);
        return true;
    }
}
