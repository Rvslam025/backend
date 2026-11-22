package cl.duoc.veterinaria.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "Ingresa tu nombre") @Size(max = 100) String nombre,
        @NotBlank(message = "Ingresa tu correo") @Email(message = "Correo inválido") @Size(max = 150) String email,
        @NotBlank(message = "Ingresa una contraseña")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String clave
) {}