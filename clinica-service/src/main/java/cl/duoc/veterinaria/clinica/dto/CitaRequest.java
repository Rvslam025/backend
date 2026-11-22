package cl.duoc.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;


public record CitaRequest(
        @NotBlank(message = "Ingresa el nombre de tu mascota") @Size(max = 60) String mascota,
        @NotBlank(message = "Selecciona un servicio") String codigoServicio,
        @NotNull(message = "Selecciona una fecha") LocalDate fecha,
        @NotNull(message = "Selecciona una hora") LocalTime hora
) {}
