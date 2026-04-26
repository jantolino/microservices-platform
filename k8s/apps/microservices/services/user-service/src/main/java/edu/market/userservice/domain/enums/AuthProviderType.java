package edu.market.userservice.domain.enums;

/**
 * Tipos de proveedores de autenticación soportados
 */
public enum AuthProviderType {
    
    LOCAL("local"),
    GOOGLE("google"),
    FACEBOOK("facebook"),
    GITHUB("github"),
    TWITTER("twitter"),
    APPLE("apple");
    
    private final String value;
    
    AuthProviderType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Obtiene el enum a partir de su valor en string
     * 
     * @param value Valor del proveedor
     * @return AuthProviderType correspondiente o LOCAL si no se encuentra
     */
    public static AuthProviderType fromValue(String value) {
        if (value == null) {
            return LOCAL;
        }
        
        for (AuthProviderType type : AuthProviderType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        return LOCAL;
    }
}
