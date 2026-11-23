package cl.duoc.veterinaria.auth.controller;

import cl.duoc.veterinaria.auth.dto.UsuarioAdminRequest;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.dto.UsuarioUpdateRequest;
import cl.duoc.veterinaria.auth.exception.ApiErrorResponse;
import cl.duoc.veterinaria.auth.security.UsuarioAutenticado;
import cl.duoc.veterinaria.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios",
        description = "Gestión de usuarios. Listar, crear y desactivar son solo para ADMIN; "
                + "ver y modificar, para ADMIN o para el propio usuario")
@ApiResponse(responseCode = "401", description = "Sin token, token alterado o vencido")
@ApiResponse(responseCode = "403", description = "Rol insuficiente o intento de acceder a la cuenta de otra persona")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios (solo ADMIN)",
            description = "Devuelve todos los usuarios, activos e inactivos, sin contraseña ni hash.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un usuario con rol (solo ADMIN)",
            description = "Alta hecha por el administrador: él define el rol (CLIENTE, RECEPCIONISTA o ADMIN). "
                    + "La contraseña se guarda con hash BCrypt.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o rol inexistente")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest req) {
        return UsuarioResponse.from(usuarioService.crear(req.nombre(), req.email(), req.clave(), req.rol()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario (ADMIN o el propio usuario)",
            description = "Un usuario que no es ADMIN solo puede consultar su propia cuenta: "
                    + "cambiar el id en la URL devuelve 403.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    public UsuarioResponse ver(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long id,
                               @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.ver(id, yo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar un usuario (ADMIN o el propio usuario)",
            description = "Reemplaza nombre y correo; la contraseña es opcional (si se envía, se vuelve a hashear). "
                    + "Solo un ADMIN puede cambiar el rol o el estado activo, y un ADMIN no puede quitarse su propio rol ni desactivarse.")
    @ApiResponse(responseCode = "200", description = "Usuario modificado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    @ApiResponse(responseCode = "409", description = "El correo ya está en uso, o un ADMIN intentó quitarse su propio rol o desactivarse")
    public UsuarioResponse actualizar(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long id,
                                      @Valid @RequestBody UsuarioUpdateRequest req,
                                      @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.from(usuarioService.actualizar(id, req, yo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un usuario (solo ADMIN)",
            description = "Borrado lógico: la cuenta queda guardada con activo = false y ya no puede iniciar sesión.")
    @ApiResponse(responseCode = "204", description = "Usuario desactivado")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    @ApiResponse(responseCode = "409", description = "No puedes desactivar tu propia cuenta")
    public void desactivar(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long id,
                           @AuthenticationPrincipal UsuarioAutenticado yo) {
        usuarioService.desactivar(id, yo);
    }
}
