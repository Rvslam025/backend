package cl.duoc.veterinaria.clinica.dto;

import cl.duoc.veterinaria.clinica.model.Servicio;


public record ServicioResponse(String codigo, String categoria, String nombre, String especie,
                               String duracion, Integer precio, String observaciones) {

    public static ServicioResponse from(Servicio s) {
        return new ServicioResponse(s.getCodigo(), s.getCategoria(), s.getNombre(), s.getEspecie(),
                s.getDuracion(), s.getPrecio(), s.getObservaciones());
    }
}
