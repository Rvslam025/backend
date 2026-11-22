package cl.duoc.veterinaria.auth.dto;

import cl.duoc.veterinaria.auth.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @NotBlank(message = "Ingresa el nombre") @Size(max = 100) String nombre,
        @NotBlank(message = "Ingresa el correo") @Email(message = "Correo inválido") @Size(max = 150) String email,
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String clave,
        Rol rol,
        Boolean activo
) {}