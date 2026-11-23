package cl.duoc.veterinaria.auth.controller;

import cl.duoc.veterinaria.auth.dto.UsuarioAdminRequest;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.dto.UsuarioUpdateRequest;
import cl.duoc.veterinaria.auth.exception.ApiErrorResponse;
import cl.duoc.veterinaria.auth.security.UsuarioAutenticado;
import cl.duoc.veterinaria.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios. Listar, crear y desactivar: solo ADMIN. Ver y modificar: el dueño de la cuenta o ADMIN.")
@ApiResponse(responseCode = "401", description = "Sin token, token alterado o vencido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios (ADMIN)", description = "Nunca incluye contraseñas ni hashes.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario con rol (ADMIN)", description = "Alta hecha por el administrador, que elige el rol.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest req) {
        return UsuarioResponse.from(usuarioService.crear(req.nombre(), req.email(), req.clave(), req.rol()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver usuario", description = "Un usuario solo puede ver su propia cuenta; ADMIN puede ver cualquiera.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "403", description = "Intento de ver la cuenta de otra persona",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public UsuarioResponse ver(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.ver(id, yo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar usuario",
            description = "El dueño de la cuenta o ADMIN. Solo ADMIN puede cambiar rol o estado (activo). "
                    + "La clave es opcional: si no se envía, no cambia.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Cuenta ajena, o intento de cambiar rol/estado sin ser ADMIN",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Correo duplicado, o un ADMIN intenta quitarse su propio rol",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest req,
                                      @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.actualizar(id, req, yo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar usuario (ADMIN)",
            description = "Borrado lógico: la cuenta se conserva pero ya no puede iniciar sesión.")
    @ApiResponse(responseCode = "204", description = "Usuario desactivado")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "No puedes desactivar tu propia cuenta",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void desactivar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        usuarioService.desactivar(id, yo);
    }
}
