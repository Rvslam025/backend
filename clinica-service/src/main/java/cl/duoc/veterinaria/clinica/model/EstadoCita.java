package cl.duoc.veterinaria.clinica.model;

import java.util.EnumSet;
import java.util.Set;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    REPROGRAMADA,
    RECHAZADA,
    CANCELADA;

    
    public static final Set<EstadoCita> ACTIVOS = EnumSet.of(PENDIENTE, CONFIRMADA, REPROGRAMADA);
}
