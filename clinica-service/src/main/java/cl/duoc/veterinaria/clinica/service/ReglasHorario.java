package cl.duoc.veterinaria.clinica.service;

import cl.duoc.veterinaria.clinica.model.Servicio;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;


public final class ReglasHorario {

    public static final int RECARGO_URGENCIA = 10_000;
    private static final int HORA_APERTURA = 9;
    private static final int HORA_CIERRE = 19;

   
    public static final List<LocalTime> HORAS_NORMALES = horas(9, 10, 11, 12, 15, 16, 17, 18);
    
    public static final List<LocalTime> HORAS_URGENCIA = horas(9, 10, 11, 12, 15, 16, 17, 18, 19, 20, 21);

    private ReglasHorario() {
    }

    public static boolean horaPermitida(Servicio servicio, LocalTime hora) {
        return (servicio.esUrgencia() ? HORAS_URGENCIA : HORAS_NORMALES).contains(hora);
    }

    public static boolean fueraDeHorario(LocalTime hora) {
        return hora.getHour() < HORA_APERTURA || hora.getHour() >= HORA_CIERRE;
    }

    
    public static int calcularPrecio(Servicio servicio, LocalTime hora) {
        if (servicio.esUrgencia() && fueraDeHorario(hora)) {
            return servicio.getPrecio() + RECARGO_URGENCIA;
        }
        return servicio.getPrecio();
    }

    private static List<LocalTime> horas(int... valores) {
        return IntStream.of(valores).mapToObj(h -> LocalTime.of(h, 0)).toList();
    }
}
