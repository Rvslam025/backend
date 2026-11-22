package cl.duoc.veterinaria.auth.security;

public record UsuarioAutenticado(Long id, String nombre, String rol) {

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }
}
