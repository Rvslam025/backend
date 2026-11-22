package cl.duoc.veterinaria.auth.dto;

import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol, boolean activo, LocalDateTime creadoEn) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo(), u.getCreadoEn());
    }
}