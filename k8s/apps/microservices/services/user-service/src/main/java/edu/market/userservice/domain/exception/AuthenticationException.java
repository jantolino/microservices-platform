package edu.market.userservice.domain.exception;

/**
 * Excepción para errores de autenticación
 */
public class AuthenticationException extends DomainException {
    
    public static final String CODE_INVALID_CREDENTIALS = "AUTH_001";
    public static final String CODE_USER_NOT_FOUND = "AUTH_002";
    public static final String CODE_ACCOUNT_LOCKED = "AUTH_003";
    public static final String CODE_ACCOUNT_DISABLED = "AUTH_004";
    public static final String CODE_INVALID_TOKEN = "AUTH_005";
    public static final String CODE_TOKEN_EXPIRED = "AUTH_006";
    public static final String CODE_INVALID_PROVIDER = "AUTH_007";
    
    public AuthenticationException(String message, String code) {
        super(message, code);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Credenciales inválidas", CODE_INVALID_CREDENTIALS);
    }
    
    public static AuthenticationException userNotFound() {
        return new AuthenticationException("Usuario no encontrado", CODE_USER_NOT_FOUND);
    }
    
    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Cuenta bloqueada", CODE_ACCOUNT_LOCKED);
    }
    
    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("Cuenta desactivada", CODE_ACCOUNT_DISABLED);
    }
    
    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Token inválido", CODE_INVALID_TOKEN);
    }
    
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Token expirado", CODE_TOKEN_EXPIRED);
    }
    
    public static AuthenticationException invalidProvider() {
        return new AuthenticationException("Proveedor de autenticación no soportado", CODE_INVALID_PROVIDER);
    }
}
