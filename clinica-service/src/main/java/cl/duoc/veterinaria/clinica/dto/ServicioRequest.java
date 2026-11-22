package cl.duoc.veterinaria.clinica.dto;

import jakarta.validation.constraints.*;


public record ServicioRequest(
        @Pattern(regexp = "^[A-Za-z]{2}[0-9]{3}$", message = "El código debe tener 2 letras y 3 números (ej. SV006)")
        String codigo,
        @NotBlank(message = "Selecciona una categoría")
        @Pattern(regexp = "Consultas|Vacunación|Cirugía|Desparasitación|Exámenes|Otros", message = "Categoría no válida")
        String categoria,
        @NotBlank(message = "Ingresa el nombre del servicio") @Size(max = 100) String nombre,
        @NotBlank(message = "Selecciona la especie") @Size(max = 30) String especie,
        @NotBlank(message = "Ingresa la duración") @Size(max = 20) String duracion,
        @NotNull(message = "Ingresa el precio") @Positive(message = "El precio debe ser mayor a 0")
        @Max(value = 10_000_000, message = "El precio es demasiado alto") Integer precio,
        @Size(max = 255) String observaciones
) {}
