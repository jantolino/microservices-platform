package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.model.User;
import java.util.Objects;

/**
 * Value Object para representar los datos necesarios para actualizar el perfil de un usuario
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class ProfileUpdateVO {
    
    private final User user;
    private final String firstName;
    private final String lastName;
    private final String phone;
    
    /**
     * Constructor para actualización de perfil
     * 
     * @param user Usuario cuyo perfil se va a actualizar
     * @param firstName Nuevo nombre del usuario
     * @param lastName Nuevo apellido del usuario
     * @param phone Nuevo teléfono del usuario
     */
    public ProfileUpdateVO(User user, String firstName, String lastName, String phone) {
        // Validaciones
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileUpdateVO that = (ProfileUpdateVO) o;
        return Objects.equals(user, that.user) &&
               Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(phone, that.phone);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, firstName, lastName, phone);
    }
    
    @Override
    public String toString() {
        return "ProfileUpdateVO{" +
                "user=" + user +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }   
}
