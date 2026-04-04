package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.PasswordChangeVO;
import edu.market.userservice.domain.vo.ProfileUpdateVO;
import edu.market.userservice.domain.vo.UserRegistrationVO;
import edu.market.userservice.domain.model.Role;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Puerto de entrada para el servicio de dominio de usuarios
 */
public interface UserDomainServicePort {
    
    /**
     * Registra un nuevo usuario en el sistema utilizando un Value Object
     * 
     * @param registrationData Value Object con los datos de registro
     * @return Usuario registrado
     */
    User registerUser(UserRegistrationVO registrationData, PasswordEncoder passwordEncoder);
    
    /**
     * Cambia la contraseña de un usuario utilizando un Value Object
     * 
     * @param passwordChangeData Value Object con los datos de cambio de contraseña
     * @return Usuario con contraseña actualizada
     */
    User changePassword(PasswordChangeVO passwordChangeData, PasswordEncoder passwordEncoder);
    
    /**
     * Restablece la contraseña de un usuario utilizando un Value Object
     * 
     * @param passwordChangeData Value Object con los datos de restablecimiento de contraseña
     * @return Usuario con contraseña restablecida
     */
    User resetPassword(PasswordChangeVO passwordChangeData, PasswordEncoder passwordEncoder);
    
    /**
     * Actualiza el perfil de un usuario utilizando un Value Object
     * 
     * @param profileUpdateData Value Object con los datos de actualización del perfil
     * @return Usuario con perfil actualizado
     */
    User updateProfile(ProfileUpdateVO profileUpdateData);
    
    /**
     * Activa la cuenta de un usuario
     * 
     * @param user Usuario
     * @return Usuario con cuenta activada
     */
    User activateUser(User user);
    
    /**
     * Desactiva la cuenta de un usuario
     * 
     * @param user Usuario
     * @return Usuario con cuenta desactivada
     */
    User deactivateUser(User user);
    
    /**
     * Bloquea la cuenta de un usuario
     * 
     * @param user Usuario
     * @return Usuario con cuenta bloqueada
     */
    User lockUser(User user);
    
    /**
     * Desbloquea la cuenta de un usuario
     * 
     * @param user Usuario
     * @return Usuario con cuenta desbloqueada
     */
    User unlockUser(User user);
    
    /**
     * Valida si un usuario es válido según las reglas de negocio
     * 
     * @param user Usuario a validar
     * @return true si el usuario es válido
     */
    boolean validateUser(User user);
    
    /**
     * Asigna roles a un usuario
     * 
     * @param user Usuario al que se asignarán roles
     * @param roles Roles a asignar
     * @return Usuario con roles asignados
     */
    User assignRoles(User user, List<Role> roles);
    
    /**
     * Verifica si un usuario tiene un rol específico
     * 
     * @param user Usuario a verificar
     * @param roleName Nombre del rol
     * @return true si el usuario tiene el rol
     */
    boolean hasRole(User user, String roleName);
    
    /**
     * Actualiza la información de un usuario existente
     * 
     * @param existingUser Usuario existente
     * @param updatedUser Usuario con datos actualizados
     * @return Usuario actualizado
     */
    User updateUserInfo(User existingUser, User updatedUser);
    
    /**
     * Verifica si una dirección de correo electrónico es válida
     * 
     * @param email Dirección de correo electrónico
     * @return true si el correo es válido
     */
    boolean isValidEmail(String email);
    
    /**
     * Verifica si una contraseña cumple con los requisitos de seguridad
     * 
     * @param password Contraseña a verificar
     * @return true si la contraseña es válida
     */
    boolean isValidPassword(String password);
}
