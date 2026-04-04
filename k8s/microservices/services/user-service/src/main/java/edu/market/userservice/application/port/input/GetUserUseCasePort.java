package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.PaginationRequestVO;

import java.util.List;

/**
 * Puerto de entrada para el caso de uso de obtención de usuario por ID
 */
public interface GetUserUseCasePort {
    
    /**
     * Obtiene un usuario por su ID
     * 
     * @param userId Value Object con el ID del usuario a obtener
     * @return Usuario encontrado
     * @throws UserException Si el usuario no existe
     */
    User execute(Long userId);

    /**
     * Obtiene una lista de usuarios con paginación
     * 
     * @param paginationRequest Value Object con los parámetros de paginación
     * @return Lista de usuarios
     */
    List<User> execute(PaginationRequestVO paginationRequest);
}
