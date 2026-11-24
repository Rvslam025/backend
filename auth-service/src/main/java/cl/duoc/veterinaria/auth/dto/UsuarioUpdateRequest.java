package cl.duoc.veterinaria.auth.dto;

import cl.duoc.veterinaria.auth.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @Schema(description = "Nombre completo", example = "Camila Rojas Pérez")
        @NotBlank(message = "Ingresa el nombre") @Size(max = 100) String nombre,
        @Schema(description = "Correo, único en el sistema", example = "camila@correo.cl")
        @NotBlank(message = "Ingresa el correo") @Email(message = "Correo inválido") @Size(max = 150) String email,
        @Schema(description = "Nueva contraseña (opcional; si se omite no cambia)", example = "NuevaClave2026", nullable = true)
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String clave,
        @Schema(description = "Nuevo rol (opcional, solo ADMIN)", example = "RECEPCIONISTA", nullable = true)
        Rol rol,
        @Schema(description = "Activar o desactivar la cuenta (opcional, solo ADMIN)", example = "true", nullable = true)
        Boolean activo
) {}