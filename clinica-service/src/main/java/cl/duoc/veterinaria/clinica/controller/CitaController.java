package cl.duoc.veterinaria.clinica.controller;

import cl.duoc.veterinaria.clinica.dto.CitaRequest;
import cl.duoc.veterinaria.clinica.dto.CitaResponse;
import cl.duoc.veterinaria.clinica.dto.ReprogramarRequest;
import cl.duoc.veterinaria.clinica.security.UsuarioAutenticado;
import cl.duoc.veterinaria.clinica.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse crear(@Valid @RequestBody CitaRequest req, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return CitaResponse.from(citaService.crear(req, yo));
    }

    
    @GetMapping("/mias")
    public List<CitaResponse> mias(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return citaService.listarMias(yo).stream().map(CitaResponse::from).toList();
    }

    
    @GetMapping
    public List<CitaResponse> todas(@RequestParam(required = false) String estado) {
        return citaService.listarTodas(estado).stream().map(CitaResponse::from).toList();
    }

   
    @GetMapping("/{id}")
    public CitaResponse ver(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return CitaResponse.from(citaService.ver(id, yo));
    }

    
    @PatchMapping("/{id}/confirmar")
    public CitaResponse confirmar(@PathVariable Long id) {
        return CitaResponse.from(citaService.confirmar(id));
    }

    
    @PatchMapping("/{id}/rechazar")
    public CitaResponse rechazar(@PathVariable Long id) {
        return CitaResponse.from(citaService.rechazar(id));
    }

    
    @PatchMapping("/{id}/reprogramar")
    public CitaResponse reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarRequest req) {
        return CitaResponse.from(citaService.reprogramar(id, req));
    }

   
    @PatchMapping("/{id}/cancelar")
    public CitaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return CitaResponse.from(citaService.cancelar(id, yo));
    }
}
