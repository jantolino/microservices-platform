package edu.market.userservice.application.usecase.user;

import edu.market.userservice.application.port.input.GetUserUseCasePort;
import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.vo.PaginationRequestVO;

import java.util.List;

/**
 * Caso de uso para obtener un usuario por su ID
 */
public class GetUserUseCase implements GetUserUseCasePort {
    
    private final UserRepositoryPort userRepository;
    
    public GetUserUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Obtiene un usuario por su ID
     * 
     * @param userId Value Object con el ID del usuario a obtener
     * @return Usuario encontrado o vacío si no existe
     */
    @Override
    public User execute(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserException.userNotFound(userId));
    }

    /**
     * Obtiene una lista de usuarios con paginación
     * 
     * @param paginationRequest Value Object con los parámetros de paginación
     * @return Lista de usuarios
     */
    @Override
    public List<User> execute(PaginationRequestVO paginationRequest) {
        return userRepository.findAll(paginationRequest.getPage(), paginationRequest.getSize());
    }
}
