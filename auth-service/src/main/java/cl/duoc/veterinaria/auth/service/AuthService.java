package cl.duoc.veterinaria.auth.service;

import cl.duoc.veterinaria.auth.dto.LoginRequest;
import cl.duoc.veterinaria.auth.dto.LoginResponse;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.exception.CredencialesInvalidasException;
import cl.duoc.veterinaria.auth.model.Usuario;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import cl.duoc.veterinaria.auth.security.JwtService;
import cl.duoc.veterinaria.auth.security.LimiteIntentosLogin;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LimiteIntentosLogin limiteIntentos;
    private final String hashFalso;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       LimiteIntentosLogin limiteIntentos) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limiteIntentos = limiteIntentos;
        this.hashFalso = passwordEncoder.encode("clave-falsa-para-igualar-tiempos");
    }

    public LoginResponse login(LoginRequest req) {
        String correo = req.email().trim().toLowerCase();
        limiteIntentos.verificar(correo);

        Usuario u = repository.findByEmail(correo).orElse(null);
        String hash = (u != null) ? u.getPasswordHash() : hashFalso;
        boolean claveOk = passwordEncoder.matches(req.clave(), hash);

        if (u == null || !claveOk || !u.isActivo()) {
            limiteIntentos.registrarFallo(correo);
            throw new CredencialesInvalidasException();
        }
        limiteIntentos.limpiar(correo);
        return new LoginResponse(jwtService.generarToken(u), "Bearer",
                jwtService.getExpiracionSegundos(), UsuarioResponse.from(u));
    }
}