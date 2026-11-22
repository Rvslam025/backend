package cl.duoc.veterinaria.clinica.service;

import cl.duoc.veterinaria.clinica.dto.ServicioRequest;
import cl.duoc.veterinaria.clinica.exception.ConflictoException;
import cl.duoc.veterinaria.clinica.exception.DatosInvalidosException;
import cl.duoc.veterinaria.clinica.exception.RecursoNoEncontradoException;
import cl.duoc.veterinaria.clinica.model.Servicio;
import cl.duoc.veterinaria.clinica.repository.CitaRepository;
import cl.duoc.veterinaria.clinica.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicios;
    private final CitaRepository citas;

    public ServicioService(ServicioRepository servicios, CitaRepository citas) {
        this.servicios = servicios;
        this.citas = citas;
    }

    @Transactional(readOnly = true)
    public List<Servicio> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return servicios.findAllByOrderByCodigo();
        }
        return servicios.findByCategoriaOrderByCodigo(categoria);
    }

    @Transactional(readOnly = true)
    public Servicio buscar(String codigo) {
        return servicios.findById(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un servicio con el código " + codigo));
    }

    @Transactional
    public Servicio crear(ServicioRequest req) {
        if (req.codigo() == null || req.codigo().isBlank()) {
            throw new DatosInvalidosException("Ingresa el código del servicio");
        }
        String codigo = req.codigo().trim().toUpperCase();
        if (servicios.existsById(codigo)) {
            throw new ConflictoException("Ya existe un servicio con el código " + codigo);
        }
        Servicio s = new Servicio();
        s.setCodigo(codigo);
        copiarDatos(req, s);
        return servicios.save(s);
    }

    
    @Transactional
    public Servicio actualizar(String codigo, ServicioRequest req) {
        Servicio s = buscar(codigo);
        copiarDatos(req, s);
        return servicios.save(s);
    }

    @Transactional
    public void eliminar(String codigo) {
        Servicio s = buscar(codigo);
        if (citas.existsByServicioCodigo(s.getCodigo())) {
            throw new ConflictoException("No se puede eliminar " + s.getNombre() + ": tiene citas asociadas");
        }
        servicios.delete(s);
    }

    private void copiarDatos(ServicioRequest req, Servicio s) {
        s.setCategoria(req.categoria());
        s.setNombre(req.nombre().trim());
        s.setEspecie(req.especie().trim());
        s.setDuracion(req.duracion().trim());
        s.setPrecio(req.precio());
        s.setObservaciones(req.observaciones() == null ? "" : req.observaciones().trim());
    }
}
