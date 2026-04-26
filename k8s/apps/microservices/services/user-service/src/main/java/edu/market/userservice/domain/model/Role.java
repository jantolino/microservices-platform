package edu.market.userservice.domain.model;

/**
 * Modelo que representa un rol en el sistema
 */
public class Role {
    private Long id;
    private String name;
    private String description;
    
    // Constructor por defecto
    public Role() {
    }
    
    // Constructor completo
    public Role(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    

}
