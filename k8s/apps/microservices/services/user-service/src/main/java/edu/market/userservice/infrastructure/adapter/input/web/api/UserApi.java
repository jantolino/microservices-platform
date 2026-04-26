package edu.market.userservice.infrastructure.adapter.input.web.api;

import org.springframework.web.bind.annotation.*;

import edu.market.userservice.infrastructure.adapter.input.web.dto.response.ResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.UserResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.UserUpdateRequestDTO;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios")
@RequestMapping("/api/users")
public interface UserApi {

  @GetMapping("/{id}")
  @Operation(summary = "Obtener usuario por ID")
  ResponseDTO<UserResponseDTO> getUserById(@PathVariable Long id);

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar usuario existente")
  ResponseDTO<UserResponseDTO> updateUser(
      @PathVariable Long id, @RequestBody UserUpdateRequestDTO request);

  @GetMapping
  @Operation(summary = "Listar todos los usuarios")
  ResponseDTO<List<UserResponseDTO>> listUsers();

  @DeleteMapping("/{id}")
  @Operation(summary = "Eliminar usuario por ID")
  ResponseDTO<Void> deleteUser(@PathVariable Long id);
}
