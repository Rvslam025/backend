package cl.duoc.veterinaria.auth.service;

import cl.duoc.veterinaria.auth.dto.LoginRequest;
import cl.duoc.veterinaria.auth.dto.LoginResponse;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.exception.CredencialesInvalidasException;
import cl.duoc.veterinaria.auth.model.Usuario;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import cl.duoc.veterinaria.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashFalso;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        // Se compara contra este hash cuando el correo no existe, para que el tiempo de respuesta sea parecido
        this.hashFalso = passwordEncoder.encode("clave-falsa-para-igualar-tiempos");
    }

    public LoginResponse login(LoginRequest req) {
        Usuario u = repository.findByEmail(req.email().trim().toLowerCase()).orElse(null);
        String hash = (u != null) ? u.getPasswordHash() : hashFalso;
        boolean claveOk = passwordEncoder.matches(req.clave(), hash);

        // Mismo error si el correo no existe, la clave falla o el usuario está inactivo
        if (u == null || !claveOk || !u.isActivo()) {
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(jwtService.generarToken(u), "Bearer",
                jwtService.getExpiracionSegundos(), UsuarioResponse.from(u));
    }
}