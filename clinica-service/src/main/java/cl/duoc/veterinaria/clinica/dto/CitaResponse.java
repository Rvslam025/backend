package cl.duoc.veterinaria.clinica.dto;

import cl.duoc.veterinaria.clinica.model.Cita;

import java.time.LocalDateTime;


public record CitaResponse(Long id, Long clienteId, String clienteNombre, String mascota,
                           String codigoServicio, String servicio, String fecha, String hora,
                           Integer precio, String estado, LocalDateTime creadaEn) {

    public static CitaResponse from(Cita c) {
        return new CitaResponse(c.getId(), c.getClienteId(), c.getClienteNombre(), c.getMascota(),
                c.getServicio().getCodigo(), c.getServicio().getNombre(),
                c.getFecha().toString(), c.getHora().toString(),
                c.getPrecio(), c.getEstado().name().toLowerCase(), c.getCreadaEn());
    }
}
