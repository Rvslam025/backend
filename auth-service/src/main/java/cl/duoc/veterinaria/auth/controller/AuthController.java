package cl.duoc.veterinaria.auth.controller;

import cl.duoc.veterinaria.auth.dto.LoginRequest;
import cl.duoc.veterinaria.auth.dto.LoginResponse;
import cl.duoc.veterinaria.auth.dto.RegistroRequest;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.exception.ApiErrorResponse;
import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.security.UsuarioAutenticado;
import cl.duoc.veterinaria.auth.service.AuthService;
import cl.duoc.veterinaria.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y datos de la sesión actual")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

        @PostMapping("/register")
        @ResponseStatus(HttpStatus.CREATED)
        @SecurityRequirements
        @Operation(summary = "Registrar usuario",
                description = "Registro público. El rol lo asigna el servidor: siempre CLIENTE (dueño de mascota).")
        @ApiResponse(responseCode = "201", description = "Usuario creado")
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        @ApiResponse(responseCode = "409", description = "El correo ya está registrado",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
            return UsuarioResponse.from(usuarioService.crear(req.nombre(), req.email(), req.clave(), Rol.CLIENTE));
        }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión",
            description = "Devuelve un JWT con vigencia limitada. Con credenciales incorrectas responde un error genérico.")
    @ApiResponse(responseCode = "200", description = "Login correcto, incluye el token")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Demasiados intentos de login",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado",
            description = "Si la cuenta fue desactivada, el token deja de servir aquí.")
    @ApiResponse(responseCode = "200", description = "Usuario de la sesión actual")
    @ApiResponse(responseCode = "401", description = "Sin token, token alterado/vencido o cuenta desactivada",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.perfil(yo.id()));
    }
}
