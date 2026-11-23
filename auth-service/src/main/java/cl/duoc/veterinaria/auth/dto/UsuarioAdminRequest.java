package cl.duoc.veterinaria.auth.dto;

import cl.duoc.veterinaria.auth.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAdminRequest(
        @Schema(description = "Nombre completo", example = "Pedro Soto")
        @NotBlank @Size(max = 100) String nombre,
        @Schema(description = "Correo, único en el sistema", example = "recepcion@sanmarcos.cl")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(description = "Contraseña de 8 a 72 caracteres", example = "Recepcion2026")
        @NotBlank @Size(min = 8, max = 72) String clave,
        @Schema(description = "Rol que asigna el administrador", example = "RECEPCIONISTA")
        @NotNull Rol rol
) {}
