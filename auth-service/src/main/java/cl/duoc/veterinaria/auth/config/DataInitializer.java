package cl.duoc.veterinaria.auth.config;

import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import cl.duoc.veterinaria.auth.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final UsuarioService usuarioService;
    private final String nombre;
    private final String email;
    private final String clave;

    public DataInitializer(UsuarioRepository repository, UsuarioService usuarioService,
                           @Value("${app.admin.nombre}") String nombre,
                           @Value("${app.admin.email}") String email,
                           @Value("${app.admin.password}") String clave) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.nombre = nombre;
        this.email = email;
        this.clave = clave;
    }

    @Override
    public void run(String... args) {
        if (!repository.existsByEmail(email.trim().toLowerCase())) {
            usuarioService.crear(nombre, email, clave, Rol.ADMIN);
        }
    }
}
