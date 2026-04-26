package edu.market.userservice.domain.service;

// Eliminamos importaciones no utilizadas ya que la publicación de eventos se trasladó a los casos de uso
import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.domain.model.Role;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.port.output.UserDomainServicePort;
import edu.market.userservice.domain.vo.PasswordChangeVO;
import edu.market.userservice.domain.vo.ProfileUpdateVO;
import edu.market.userservice.domain.vo.UserRegistrationVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de dominio para usuarios
 */
public class UserDomainService implements UserDomainServicePort {
    
    private final Logger log = LoggerFactory.getLogger(UserDomainService.class);
    
    // Ya no necesitamos el EventPublisherPort aquí, se utilizará en los casos de uso
    
    public UserDomainService() {
        // Constructor sin dependencias
    }    
    
    
    @Override
    public User registerUser(UserRegistrationVO registrationData, PasswordEncoder passwordEncoder) {
        
        try {
            log.info("init - registerUser");

            User user = registrationData.getUser();            
                        
            // Validar usuario
            log.debug("registerUser validating user data for: {}", user.getEmail());
            validateUserForRegistration(user);
            
            // Validar contraseña
            log.debug("registerUser validating password");
            validatePassword(registrationData.getRawPassword(), registrationData.getConfirmPassword());

            // Preparar usuario para registro (codificar contraseña y configurar campos de estado y auditoría)
            log.debug("prepareUserForRegistration for user: {}", user.getEmail());
            user.prepareForRegistration(passwordEncoder);
            
            // La publicación del evento se trasladará al caso de uso después de la persistencia
            
            log.info("end - registerUser");
            return user;
        } catch (Exception e) {
            log.error("registerUser error: {}", e.getMessage(), e);
            throw e;
        }
    }   
    
    
    @Override
    public User changePassword(PasswordChangeVO passwordChangeData, PasswordEncoder passwordEncoder) {
        
        try {
            log.info("init - changePassword");

            User user = passwordChangeData.getUser();
            
            // Validar nueva contraseña
            log.debug("changePassword validating new password for user: {}", user.getId());
            validatePassword(passwordChangeData.getNewPassword(), passwordChangeData.getConfirmPassword());
            
            // Cambiar contraseña usando el método de dominio
            log.debug("changePassword updating password for user: {}", user.getId());
            user.changePassword(passwordChangeData.getNewPassword(), passwordEncoder);
            
            // La publicación del evento se trasladará al caso de uso después de la persistencia
            
            log.info("end - changePassword");
            return user;
        } catch (Exception e) {
            log.error("changePassword error: {}", e.getMessage(), e);
            throw e;
        }
    }   
   
    
    @Override
    public User resetPassword(PasswordChangeVO passwordChangeData, PasswordEncoder passwordEncoder) {
        
        try {
            log.info("init - resetPassword");
            
            User user = passwordChangeData.getUser();
            
            // Validar nueva contraseña
            log.debug("resetPassword validating new password for user: {}", user.getId());
            validatePassword(passwordChangeData.getNewPassword(), passwordChangeData.getConfirmPassword());
            
            // Cambiar contraseña usando el método de dominio
            log.debug("resetPassword updating password for user: {}", user.getId());
            user.changePassword(passwordChangeData.getNewPassword(), passwordEncoder);
            
            // La publicación del evento se trasladará al caso de uso después de la persistencia
            
            log.info("end - resetPassword");
            return user;
        } catch (Exception e) {
            log.error("resetPassword error: {}", e.getMessage(), e);
            throw e;
        }
    }    
  
    
    @Override
    public User updateProfile(ProfileUpdateVO profileUpdateData) {
        
        try {
            log.info("init - updateProfile");
            
            User user = profileUpdateData.getUser();
            
            // Actualizar información básica
            log.debug("updateProfile updating basic info for user: {}", user.getId());
            log.debug("updateProfile new values: firstName={}, lastName={}, phone={}", profileUpdateData.getFirstName(), profileUpdateData.getLastName(), profileUpdateData.getPhone());
            user.updateBasicInfo(profileUpdateData.getFirstName(), profileUpdateData.getLastName(), profileUpdateData.getPhone());
            
            log.info("end - updateProfile");
            return user;
        } catch (Exception e) {
            log.error("updateProfile error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public User activateUser(User user) {
        try {
            log.info("init - activateUser");
            
            if (user == null || user.getId() == null) {
                log.debug("activateUser user is null or has no id");
                log.info("end - activateUser");
                throw UserException.userNotFound(0L);
            }
            
            // Usar el método de dominio para activar
            user.activate();
            
            log.info("end - activateUser");
            return user;
        } catch (Exception e) {
            log.error("activateUser error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public User deactivateUser(User user) {
        try {
            log.info("init - deactivateUser");
            
            if (user == null || user.getId() == null) {
                log.debug("deactivateUser user is null or has no id");
                log.info("end - deactivateUser");
                throw UserException.userNotFound(0L);
            }
            
            // Usar el método de dominio para desactivar
            user.deactivate();
            
            log.info("end - deactivateUser");
            return user;
        } catch (Exception e) {
            log.error("deactivateUser error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public User lockUser(User user) {
        try {
            log.info("init - lockUser");
            
            if (user == null || user.getId() == null) {
                log.debug("lockUser user is null or has no id");
                log.info("end - lockUser");
                throw UserException.userNotFound(0L);
            }
            
            // Usar el método de dominio para bloquear
            user.lock();
            
            log.info("end - lockUser");
            return user;
        } catch (Exception e) {
            log.error("lockUser error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public User unlockUser(User user) {
        try {
            log.info("init - unlockUser");
            
            if (user == null || user.getId() == null) {
                log.debug("unlockUser user is null or has no id");
                log.info("end - unlockUser");
                throw UserException.userNotFound(0L);
            }
            
            // Usar el método de dominio para desbloquear
            user.unlock();
            
            log.info("end - unlockUser");
            return user;
        } catch (Exception e) {
            log.error("unlockUser error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean validateUser(User user) {
        try {
            log.info("init - validateUser");
            
            if (user == null) {
                log.debug("validateUser user is null");
                log.info("end - validateUser");
                return false;
            }
            
            // Validar campos obligatorios
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.debug("validateUser email is null or empty");
                log.info("end - validateUser");
                return false;
            }
            
            // Validar formato de email
            if (!isValidEmail(user.getEmail())) {
                log.debug("validateUser invalid email format: {}", user.getEmail());
                log.info("end - validateUser");
                return false;
            }
            
            log.debug("validateUser user is valid: {}", user.getEmail());
            log.info("end - validateUser");
            return true;
        } catch (Exception e) {
            log.error("validateUser error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validateUserForRegistration(User user) {
        try {
            log.info("init - validateUserForRegistration");
            
            if (user == null) {
                log.debug("validateUserForRegistration user is null");
                log.info("end - validateUserForRegistration");
                throw UserException.userNotFound(0L);
            }
            
            // Validar email
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.debug("validateUserForRegistration email is null or empty");
                log.info("end - validateUserForRegistration");
                throw UserException.invalidEmailFormat();
            }
            
            if (!isValidEmail(user.getEmail())) {
                log.debug("validateUserForRegistration invalid email format: {}", user.getEmail());
                log.info("end - validateUserForRegistration");
                throw UserException.invalidEmailFormat();
            }
            
            // Validar nombre y apellido
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty() ||
                user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                log.debug("validateUserForRegistration invalid name or lastname");
                log.info("end - validateUserForRegistration");
                throw new UserException("El nombre y apellido son obligatorios", "USER_006");
            }
            
            log.debug("validateUserForRegistration user data is valid for: {}", user.getEmail());
            log.info("end - validateUserForRegistration");
        } catch (Exception e) {
            log.error("validateUserForRegistration error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validatePassword(String password, String confirmPassword) {
        try {
            log.info("init - validatePassword");
            
            // Validar que la contraseña no sea nula
            if (password == null || password.trim().isEmpty()) {
                log.debug("validatePassword password is null or empty");
                log.info("end - validatePassword");
                throw UserException.invalidPasswordFormat();
            }
            
            // Validar formato de contraseña
            if (!isValidPassword(password)) {
                log.debug("validatePassword invalid password format");
                log.info("end - validatePassword");
                throw UserException.invalidPasswordFormat();
            }
            
            // Validar que las contraseñas coincidan
            if (!password.equals(confirmPassword)) {
                log.debug("validatePassword passwords do not match");
                log.info("end - validatePassword");
                throw UserException.passwordMismatch();
            }
            
            log.debug("validatePassword password is valid");
            log.info("end - validatePassword");
        } catch (Exception e) {
            log.error("validatePassword error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean isValidEmail(String email) {
        try {
            log.info("init - isValidEmail");
            
            // Usar el método estático de User para validar email
            boolean isValid = User.isValidEmail(email);
            log.debug("isValidEmail result for {}: {}", email, isValid);
            log.info("end - isValidEmail");
            return isValid;
        } catch (Exception e) {
            log.error("isValidEmail error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean isValidPassword(String password) {
        try {
            log.info("init - isValidPassword");
            
            // Usar el método estático de User para validar contraseña
            boolean isValid = User.isValidPassword(password);
            log.debug("isValidPassword result: {}", isValid);
            log.info("end - isValidPassword");
            return isValid;
        } catch (Exception e) {
            log.error("isValidPassword error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User assignRoles(User user, List<Role> roles) {
        if (user == null || user.getId() == null) {
            throw UserException.userNotFound(0L);
        }
        
        if (roles == null || roles.isEmpty()) {
            return user; // No hay roles para asignar
        }
        
        // Reemplazar los roles existentes con los nuevos roles
        user.setRoles(roles);
        user.setUpdatedAt(LocalDateTime.now());
        
        return user;
    }

    @Override
    public boolean hasRole(User user, String roleName) {
        if (user == null || roleName == null || roleName.trim().isEmpty()) {
            return false;
        }
        
        // Verificar si el usuario tiene el rol especificado
        return user.getRoles() != null && 
               user.getRoles().stream()
                   .anyMatch(role -> roleName.equalsIgnoreCase(role.getName()));
    }

    @Override
    public User updateUserInfo(User existingUser, User updatedUser) {
        if (existingUser == null || existingUser.getId() == null) {
            throw UserException.userNotFound(0L);
        }
        
        if (updatedUser == null) {
            throw UserException.invalidEmailFormat(); // Usamos esta excepción como alternativa
        }
        
        // Actualizar campos básicos usando el método de dominio
        if (updatedUser.getFirstName() != null && updatedUser.getLastName() != null) {
            existingUser.updateBasicInfo(
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhone()
            );
        }
        
        // Actualizar correo electrónico si ha cambiado y es válido
        // El método setEmail ahora incluye validación y actualiza updatedAt
        if (updatedUser.getEmail() != null && 
            !updatedUser.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(updatedUser.getEmail());
        }       
        
        return existingUser;
    }
}
