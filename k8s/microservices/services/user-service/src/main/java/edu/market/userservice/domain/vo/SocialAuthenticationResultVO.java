package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;

import java.util.Objects;

/**
 * Value Object que encapsula el resultado de una autenticación social
 */
public final class SocialAuthenticationResultVO {
    
    private final User user;
    private final UserAuthToken token;
    private final boolean isNewUser;
    
    /**
     * Constructor para crear un objeto SocialAuthenticationResultVO
     * 
     * @param user Usuario autenticado
     * @param token Token de autenticación generado
     * @param isNewUser Indica si es un usuario nuevo o existente
     */
    public SocialAuthenticationResultVO(User user, UserAuthToken token, boolean isNewUser) {
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.token = Objects.requireNonNull(token, "Token cannot be null");
        this.isNewUser = isNewUser;
    }
    
    /**
     * @return Usuario autenticado
     */
    public User getUser() {
        return user;
    }
    
    /**
     * @return Token de autenticación generado
     */
    public UserAuthToken getToken() {
        return token;
    }
    
    /**
     * @return true si es un usuario nuevo, false si es un usuario existente
     */
    public boolean isNewUser() {
        return isNewUser;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialAuthenticationResultVO that = (SocialAuthenticationResultVO) o;
        return isNewUser == that.isNewUser &&
               Objects.equals(user, that.user) &&
               Objects.equals(token, that.token);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, token, isNewUser);
    }
    
    @Override
    public String toString() {
        return "SocialAuthenticationResultVO{" +
                "user=" + user.getId() +
                ", token=" + token.getId() +
                ", isNewUser=" + isNewUser +
                '}';
    }
}
