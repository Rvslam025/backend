package cl.duoc.veterinaria.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Correo con el que se registró", example = "camila@correo.cl")
        @NotBlank(message = "Ingresa tu correo") String email,
        @Schema(description = "Contraseña", example = "Clave12345")
        @NotBlank(message = "Ingresa tu contraseña") String clave
) {}