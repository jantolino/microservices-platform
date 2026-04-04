package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.model.User;
import java.util.Objects;

/**
 * Value Object para representar los datos necesarios para cambiar la contraseña de un usuario
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class PasswordChangeVO {
    
    private final User user;
    private final String currentPassword;
    private final String newPassword;
    private final String confirmPassword;
    private final String ipAddress;
    private final String userAgent;
    
    /**
     * Constructor para cambio de contraseña (requiere contraseña actual)
     * 
     * @param user Usuario que cambia su contraseña
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @param confirmPassword Confirmación de la nueva contraseña
     * @param ipAddress Dirección IP desde donde se realiza el cambio
     * @param userAgent Agente de usuario desde donde se realiza el cambio
     */
    public PasswordChangeVO(User user, String currentPassword, String newPassword, String confirmPassword, 
                           String ipAddress, String userAgent) {
        // Validaciones
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La confirmación de contraseña no puede estar vacía");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        this.user = user;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    /**
     * Constructor para reseteo de contraseña (no requiere contraseña actual)
     * 
     * @param user Usuario cuya contraseña se resetea
     * @param newPassword Nueva contraseña
     * @param confirmPassword Confirmación de la nueva contraseña
     * @param ipAddress Dirección IP desde donde se realiza el reseteo
     * @param userAgent Agente de usuario desde donde se realiza el reseteo
     */
    public PasswordChangeVO(User user, String newPassword, String confirmPassword, 
                           String ipAddress, String userAgent) {
        this(user, null, newPassword, confirmPassword, ipAddress, userAgent);
    }
    
    public User getUser() {
        return user;
    }
    
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public boolean isPasswordReset() {
        return currentPassword == null || currentPassword.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordChangeVO that = (PasswordChangeVO) o;
        return Objects.equals(user, that.user) &&
               Objects.equals(currentPassword, that.currentPassword) &&
               Objects.equals(newPassword, that.newPassword) &&
               Objects.equals(confirmPassword, that.confirmPassword) &&
               Objects.equals(ipAddress, that.ipAddress) &&
               Objects.equals(userAgent, that.userAgent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, currentPassword, newPassword, confirmPassword, ipAddress, userAgent);
    }
    
    @Override
    public String toString() {
        return "PasswordChangeVO{" +
                "user=" + user +
                ", currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    } 
}
