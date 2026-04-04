package edu.market.userservice.application.usecase.user;

import edu.market.userservice.application.port.input.UpdateUserProfileUseCasePort;
import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.port.output.UserDomainServicePort;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.vo.ProfileUpdateRequestVO;
import edu.market.userservice.domain.vo.ProfileUpdateVO;

/**
 * Implementación del caso de uso para actualizar el perfil de un usuario
 */
public class UpdateUserProfileUseCase implements UpdateUserProfileUseCasePort {
    
    private final UserDomainServicePort userDomainService;
    private final UserRepositoryPort userRepository;
    
    public UpdateUserProfileUseCase(UserDomainServicePort userDomainService, UserRepositoryPort userRepository) {
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
    }
    
    /**
     * Actualiza el perfil básico de un usuario
     * 
     * @param profileUpdateRequest Value Object con los datos de actualización del perfil
     * @return Usuario actualizado
     */
    @Override
    public User execute(ProfileUpdateRequestVO profileUpdateRequest) {
        // Obtener el usuario del repositorio
        User user = userRepository.findById(profileUpdateRequest.getUserId())
                .orElseThrow(() -> UserException.userNotFound(profileUpdateRequest.getUserId()));

        ProfileUpdateVO profileUpdateVO = new ProfileUpdateVO(user, profileUpdateRequest.getFirstName(), profileUpdateRequest.getLastName(), profileUpdateRequest.getPhone());
        
        // Aplicar la lógica de dominio para actualizar el usuario
        User updated = userDomainService.updateProfile(profileUpdateVO);
        
        // Persistir los cambios en la base de datos
        return userRepository.save(updated);
    }
}
