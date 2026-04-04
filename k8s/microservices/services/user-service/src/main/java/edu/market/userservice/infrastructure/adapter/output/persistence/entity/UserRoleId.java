package edu.market.userservice.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long roleId;
}
