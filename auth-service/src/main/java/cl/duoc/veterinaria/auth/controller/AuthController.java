package cl.duoc.veterinaria.auth.controller;

import cl.duoc.veterinaria.auth.dto.LoginRequest;
import cl.duoc.veterinaria.auth.dto.LoginResponse;
import cl.duoc.veterinaria.auth.dto.RegistroRequest;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.security.UsuarioAutenticado;
import cl.duoc.veterinaria.auth.service.AuthService;
import cl.duoc.veterinaria.auth.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return UsuarioResponse.from(usuarioService.crear(req.nombre(), req.email(), req.clave(), Rol.CLIENTE));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.perfil(yo.id()));
    }
}