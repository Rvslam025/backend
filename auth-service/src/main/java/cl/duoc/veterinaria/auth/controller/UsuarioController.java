package cl.duoc.veterinaria.auth.controller;

import cl.duoc.veterinaria.auth.dto.UsuarioAdminRequest;
import cl.duoc.veterinaria.auth.dto.UsuarioResponse;
import cl.duoc.veterinaria.auth.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest req) {
        return UsuarioResponse.from(usuarioService.crear(req.nombre(), req.email(), req.clave(), req.rol()));
    }
}
