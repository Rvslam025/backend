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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro público, inicio de sesión con JWT y perfil del usuario autenticado")
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
    @Operation(summary = "Registrar un usuario",
            description = "Crea una cuenta nueva. El servidor asigna siempre el rol CLIENTE (dueño de mascota); "
                    + "quien se registra no puede elegirlo. La contraseña se guarda con hash BCrypt y nunca se devuelve.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos: campos vacíos, correo mal formado o contraseña de menos de 8 caracteres")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return UsuarioResponse.from(usuarioService.crear(req.nombre(), req.email(), req.clave(), Rol.CLIENTE));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión",
            description = "Busca al usuario por correo, compara la contraseña con su hash y, si coincide y la cuenta está activa, "
                    + "devuelve un JWT con vigencia. Copia el campo token y pégalo en Authorize para probar las rutas protegidas.")
    @ApiResponse(responseCode = "200", description = "Inicio de sesión correcto: token JWT, vigencia en segundos y datos del usuario")
    @ApiResponse(responseCode = "400", description = "Faltan el correo o la contraseña")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas (mismo mensaje si el correo no existe, la contraseña falla o la cuenta está inactiva)")
    @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos; hay que esperar antes de reintentar")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Ver mi perfil",
            description = "Devuelve los datos del usuario dueño del token, sin contraseña ni hash.")
    @ApiResponse(responseCode = "200", description = "Perfil del usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Sin token, token alterado o vencido, o cuenta desactivada")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.perfil(yo.id()));
    }
}
