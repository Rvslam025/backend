package cl.duoc.veterinaria.clinica.security;


public record UsuarioAutenticado(Long id, String nombre, String rol) {

    
    public boolean esPersonal() {
        return "ADMIN".equals(rol) || "RECEPCIONISTA".equals(rol);
    }
}
