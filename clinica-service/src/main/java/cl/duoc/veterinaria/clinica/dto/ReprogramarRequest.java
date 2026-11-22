package cl.duoc.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;


public record ReprogramarRequest(
        @NotNull(message = "Selecciona una fecha") LocalDate fecha,
        @NotNull(message = "Selecciona una hora") LocalTime hora
) {}
