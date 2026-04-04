package edu.market.userservice.domain.exception;

/**
 * Excepción para errores relacionados con usuarios
 */
public class UserException extends DomainException {
    
    public static final String CODE_EMAIL_ALREADY_EXISTS = "USER_001";
    public static final String CODE_INVALID_EMAIL_FORMAT = "USER_002";
    public static final String CODE_INVALID_PASSWORD_FORMAT = "USER_003";
    public static final String CODE_USER_NOT_FOUND = "USER_004";
    public static final String CODE_PASSWORD_MISMATCH = "USER_005";
    
    public UserException(String message, String code) {
        super(message, code);
    }
    
    public static UserException emailAlreadyExists(String email) {
        return new UserException("El email '" + email + "' ya está registrado", CODE_EMAIL_ALREADY_EXISTS);
    }
    
    public static UserException invalidEmailFormat() {
        return new UserException("Formato de email inválido", CODE_INVALID_EMAIL_FORMAT);
    }
    
    public static UserException invalidPasswordFormat() {
        return new UserException("La contraseña debe tener al menos 8 caracteres, incluyendo una letra y un número", CODE_INVALID_PASSWORD_FORMAT);
    }
    
    public static UserException userNotFound(Long id) {
        return new UserException("Usuario con ID " + id + " no encontrado", CODE_USER_NOT_FOUND);
    }
    
    public static UserException userNotFoundByEmail(String email) {
        return new UserException("Usuario con email '" + email + "' no encontrado", CODE_USER_NOT_FOUND);
    }
    
    public static UserException passwordMismatch() {
        return new UserException("Las contraseñas no coinciden", CODE_PASSWORD_MISMATCH);
    }
}
