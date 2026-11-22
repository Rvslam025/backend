package cl.duoc.veterinaria.clinica.service;

import cl.duoc.veterinaria.clinica.dto.CitaRequest;
import cl.duoc.veterinaria.clinica.dto.ReprogramarRequest;
import cl.duoc.veterinaria.clinica.exception.AccesoDenegadoException;
import cl.duoc.veterinaria.clinica.exception.ConflictoException;
import cl.duoc.veterinaria.clinica.exception.DatosInvalidosException;
import cl.duoc.veterinaria.clinica.exception.RecursoNoEncontradoException;
import cl.duoc.veterinaria.clinica.model.Cita;
import cl.duoc.veterinaria.clinica.model.EstadoCita;
import cl.duoc.veterinaria.clinica.model.Servicio;
import cl.duoc.veterinaria.clinica.repository.CitaRepository;
import cl.duoc.veterinaria.clinica.repository.ServicioRepository;
import cl.duoc.veterinaria.clinica.security.UsuarioAutenticado;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static cl.duoc.veterinaria.clinica.model.EstadoCita.*;

@Service
public class CitaService {

    private static final String HORARIO_OCUPADO = "Ese horario ya está ocupado. Elige otra fecha u hora";

    private final CitaRepository citas;
    private final ServicioRepository servicios;
    private final Clock reloj;

    public CitaService(CitaRepository citas, ServicioRepository servicios, Clock reloj) {
        this.citas = citas;
        this.servicios = servicios;
        this.reloj = reloj;
    }

    
    @Transactional
    public Cita crear(CitaRequest req, UsuarioAutenticado yo) {
        String codigo = req.codigoServicio().trim().toUpperCase();
        Servicio servicio = servicios.findById(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un servicio con el código " + codigo));
        validarFechaHora(servicio, req.fecha(), req.hora());
        if (citas.existsByFechaAndHoraAndEstadoIn(req.fecha(), req.hora(), EstadoCita.ACTIVOS)) {
            throw new ConflictoException(HORARIO_OCUPADO);
        }

        Cita cita = new Cita();
        cita.setClienteId(yo.id());
        cita.setClienteNombre(yo.nombre());
        cita.setMascota(req.mascota().trim());
        cita.setServicio(servicio);
        cita.setFecha(req.fecha());
        cita.setHora(req.hora());
        cita.setPrecio(ReglasHorario.calcularPrecio(servicio, req.hora()));
        cita.setEstado(PENDIENTE);
        return guardar(cita);
    }

    @Transactional(readOnly = true)
    public List<Cita> listarMias(UsuarioAutenticado yo) {
        return citas.findByClienteIdOrderByFechaAscHoraAsc(yo.id());
    }

    
    @Transactional(readOnly = true)
    public List<Cita> listarTodas(String estado) {
        if (estado == null || estado.isBlank()) {
            return citas.findAllByOrderByFechaAscHoraAsc();
        }
        try {
            return citas.findByEstadoOrderByFechaAscHoraAsc(EstadoCita.valueOf(estado.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException("Estado no válido: " + estado);
        }
    }

    
    @Transactional(readOnly = true)
    public Cita ver(Long id, UsuarioAutenticado yo) {
        Cita cita = buscar(id);
        if (!yo.esPersonal() && !cita.esDe(yo.id())) {
            throw new AccesoDenegadoException("No puedes ver la cita de otra persona");
        }
        return cita;
    }

    @Transactional
    public Cita confirmar(Long id) {
        Cita cita = buscar(id);
        exigirEstado(cita, "confirmar", Set.of(PENDIENTE, REPROGRAMADA));
        cita.setEstado(CONFIRMADA);
        return citas.save(cita);
    }

    @Transactional
    public Cita rechazar(Long id) {
        Cita cita = buscar(id);
        exigirEstado(cita, "rechazar", EstadoCita.ACTIVOS);
        cita.setEstado(RECHAZADA);
        return citas.save(cita);
    }

    
    @Transactional
    public Cita reprogramar(Long id, ReprogramarRequest req) {
        Cita cita = buscar(id);
        exigirEstado(cita, "reprogramar", EstadoCita.ACTIVOS);
        validarFechaHora(cita.getServicio(), req.fecha(), req.hora());
        if (citas.existsByFechaAndHoraAndEstadoInAndIdNot(req.fecha(), req.hora(), EstadoCita.ACTIVOS, id)) {
            throw new ConflictoException(HORARIO_OCUPADO);
        }
        cita.setFecha(req.fecha());
        cita.setHora(req.hora());
        cita.setPrecio(ReglasHorario.calcularPrecio(cita.getServicio(), req.hora()));
        cita.setEstado(REPROGRAMADA);
        return guardar(cita);
    }

    
    @Transactional
    public Cita cancelar(Long id, UsuarioAutenticado yo) {
        Cita cita = buscar(id);
        if (!cita.esDe(yo.id())) {
            throw new AccesoDenegadoException("Solo puedes cancelar tus propias citas");
        }
        exigirEstado(cita, "cancelar", EstadoCita.ACTIVOS);
        cita.setEstado(CANCELADA);
        return citas.save(cita);
    }

    private Cita buscar(Long id) {
        return citas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));
    }

    private void exigirEstado(Cita cita, String accion, Set<EstadoCita> permitidos) {
        if (!permitidos.contains(cita.getEstado())) {
            throw new ConflictoException("No se puede " + accion + " una cita "
                    + cita.getEstado().name().toLowerCase());
        }
    }

    private void validarFechaHora(Servicio servicio, LocalDate fecha, LocalTime hora) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!LocalDateTime.of(fecha, hora).isAfter(ahora)) {
            throw new DatosInvalidosException("La fecha y la hora deben ser futuras");
        }
        if (!ReglasHorario.horaPermitida(servicio, hora)) {
            throw new DatosInvalidosException(servicio.esUrgencia()
                    ? "Las urgencias se atienden de 09:00 a 12:00 y de 15:00 a 21:00, en bloques de una hora"
                    : "Este servicio se atiende de 09:00 a 12:00 y de 15:00 a 18:00, en bloques de una hora");
        }
    }

  
    private Cita guardar(Cita cita) {
        try {
            return citas.saveAndFlush(cita);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException(HORARIO_OCUPADO);
        }
    }
}
