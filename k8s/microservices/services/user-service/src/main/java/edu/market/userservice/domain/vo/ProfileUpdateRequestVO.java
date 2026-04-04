package edu.market.userservice.domain.vo;

import java.util.Objects;

/**
 * Value Object para representar una solicitud de actualización de perfil de usuario
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class ProfileUpdateRequestVO {
    
    private final Long userId;
    private final String firstName;
    private final String lastName;
    private final String phone;
    
    /**
     * Constructor para la solicitud de actualización de perfil
     * 
     * @param userId ID del usuario
     * @param firstName Nombre
     * @param lastName Apellido
     * @param phone Teléfono
     */
    public ProfileUpdateRequestVO(Long userId, String firstName, String lastName, String phone) {
        // Validaciones
        if (userId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
    
    public Long getUserId() {
        return userId;
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
        ProfileUpdateRequestVO that = (ProfileUpdateRequestVO) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(phone, that.phone);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, firstName, lastName, phone);
    }
    
    @Override
    public String toString() {
        return "ProfileUpdateRequestVO{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
