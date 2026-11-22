package cl.duoc.veterinaria.clinica.controller;

import cl.duoc.veterinaria.clinica.dto.ServicioRequest;
import cl.duoc.veterinaria.clinica.dto.ServicioResponse;
import cl.duoc.veterinaria.clinica.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

   
    @GetMapping
    public List<ServicioResponse> listar(@RequestParam(required = false) String categoria) {
        return servicioService.listar(categoria).stream().map(ServicioResponse::from).toList();
    }

    
    @GetMapping("/{codigo}")
    public ServicioResponse ver(@PathVariable String codigo) {
        return ServicioResponse.from(servicioService.buscar(codigo));
    }

    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponse crear(@Valid @RequestBody ServicioRequest req) {
        return ServicioResponse.from(servicioService.crear(req));
    }

    
    @PutMapping("/{codigo}")
    public ServicioResponse actualizar(@PathVariable String codigo, @Valid @RequestBody ServicioRequest req) {
        return ServicioResponse.from(servicioService.actualizar(codigo, req));
    }

    
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        servicioService.eliminar(codigo);
    }
}
