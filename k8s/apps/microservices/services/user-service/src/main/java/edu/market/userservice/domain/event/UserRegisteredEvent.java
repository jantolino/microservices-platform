package edu.market.userservice.domain.event;

import edu.market.userservice.domain.model.User;

/**
 * Evento que se dispara cuando un usuario se registra en el sistema
 */
public class UserRegisteredEvent extends DomainEvent {
    
    private final Long userId;
    private final String email;
    private final String name;
    private final boolean socialLogin;
    
    public UserRegisteredEvent(User user, boolean socialLogin) {
        super();
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.socialLogin = socialLogin;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isSocialLogin() {
        return socialLogin;
    }
}
