package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.model.MetadataLogin;

import java.util.List;
import java.util.Optional;

/**
 * Puerto para operaciones de repositorio de metadatos de login
 */
public interface MetadataLoginRepositoryPort {
    
    /**
     * Guarda un metadata de login
     * @param metadataLogin Metadata a guardar
     * @return Metadata guardado
     */
    MetadataLogin save(MetadataLogin metadataLogin);
    
    /**
     * Busca metadatos de login por ID de usuario
     * @param userId ID del usuario
     * @return Lista de metadatos de login
     */
    List<MetadataLogin> findByUserId(Long userId);
    
    /**
     * Busca un metadata de login por ID de usuario y proveedor
     * @param userId ID del usuario
     * @param provider Proveedor de autenticación
     * @return Metadata encontrado o vacío
     */
    Optional<MetadataLogin> findByUserIdAndProvider(Long userId, String provider);
    
    /**
     * Busca un metadata de login por proveedor y ID de proveedor
     * @param provider Proveedor de autenticación
     * @param providerId ID del usuario en el proveedor
     * @return Metadata encontrado o vacío
     */
    Optional<MetadataLogin> findByProviderAndProviderId(String provider, String providerId);
}
