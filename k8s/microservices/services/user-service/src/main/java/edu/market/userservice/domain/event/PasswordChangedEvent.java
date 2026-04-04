package edu.market.userservice.domain.event;

import edu.market.userservice.domain.model.User;

/**
 * Evento que se dispara cuando un usuario cambia su contraseña
 */
public class PasswordChangedEvent extends DomainEvent {
    
    private final Long userId;
    private final String email;
    private final String ipAddress;
    private final String userAgent;
    private final boolean resetPassword;
    
    public PasswordChangedEvent(User user, String ipAddress, String userAgent, boolean resetPassword) {
        super();
        this.userId = user.getId();
        this.email = user.getEmail();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.resetPassword = resetPassword;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public boolean isResetPassword() {
        return resetPassword;
    }
}
