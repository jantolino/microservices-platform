package edu.market.userservice.domain.vo;

import java.util.Objects;

/**
 * Value Object para las credenciales de inicio de sesión
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class LoginCredentialsVO {
    
    private final String email;
    private final String password;
    private final String ipAddress;
    private final String userAgent;
    
    public LoginCredentialsVO(String email, String password, String ipAddress, String userAgent) {
        // Validaciones
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        this.email = email;
        this.password = password;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
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
        LoginCredentialsVO that = (LoginCredentialsVO) o;
        return Objects.equals(email, that.email) &&
               Objects.equals(password, that.password) &&
               Objects.equals(ipAddress, that.ipAddress) &&
               Objects.equals(userAgent, that.userAgent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email, password, ipAddress, userAgent);
    }
    
    @Override
    public String toString() {
        return "LoginCredentialsVO{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
