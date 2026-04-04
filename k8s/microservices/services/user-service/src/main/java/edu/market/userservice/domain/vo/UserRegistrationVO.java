package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.model.User;
import java.util.Objects;

/**
 * Value Object para representar la información de registro de un usuario
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class UserRegistrationVO {
    
    private final User user;
    private final String rawPassword;
    private final String confirmPassword;
    private final String ipAddress;
    private final String userAgent;
    
    public UserRegistrationVO(User user, String rawPassword, String confirmPassword, String ipAddress, String userAgent) {
        // Validaciones
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La confirmación de contraseña no puede estar vacía");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        this.user = user;
        this.rawPassword = rawPassword;
        this.confirmPassword = confirmPassword;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getRawPassword() {
        return rawPassword;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationVO that = (UserRegistrationVO) o;
        return Objects.equals(user, that.user) &&
               Objects.equals(rawPassword, that.rawPassword) &&
               Objects.equals(confirmPassword, that.confirmPassword) &&
               Objects.equals(ipAddress, that.ipAddress) &&
               Objects.equals(userAgent, that.userAgent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, rawPassword, confirmPassword, ipAddress, userAgent);
    }
    
    @Override
    public String toString() {
        return "UserRegistrationVO{" +
                "user=" + user +
                ", rawPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
