package edu.market.userservice.domain.vo;

import java.util.Objects;

/**
 * Value Object para representar una solicitud de paginación
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class PaginationRequestVO {
    
    private final int page;
    private final int size;
    
    /**
     * Constructor para la solicitud de paginación
     * 
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     */
    public PaginationRequestVO(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("El número de página no puede ser negativo");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que cero");
        }
        this.page = page;
        this.size = size;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    /**
     * Calcula el offset para consultas SQL basado en la página y tamaño
     * 
     * @return Offset calculado
     */
    public int getOffset() {
        return page * size;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationRequestVO that = (PaginationRequestVO) o;
        return page == that.page && size == that.size;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(page, size);
    }
    
    @Override
    public String toString() {
        return "PaginationRequestVO{" +
                "page=" + page +
                ", size=" + size +
                '}';
    }
}
