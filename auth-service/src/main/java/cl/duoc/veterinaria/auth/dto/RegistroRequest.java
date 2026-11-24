package cl.duoc.veterinaria.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @Schema(description = "Nombre completo", example = "Camila Rojas")
        @NotBlank(message = "Ingresa tu nombre") @Size(max = 100) String nombre,
        @Schema(description = "Correo, único en el sistema", example = "camila@correo.cl")
        @NotBlank(message = "Ingresa tu correo") @Email(message = "Correo inválido") @Size(max = 150) String email,
        @Schema(description = "Contraseña de 8 a 72 caracteres", example = "Clave12345")
        @NotBlank(message = "Ingresa una contraseña")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String clave
) {}