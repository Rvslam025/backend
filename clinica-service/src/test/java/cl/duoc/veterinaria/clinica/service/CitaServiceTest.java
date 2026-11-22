package cl.duoc.veterinaria.clinica.service;

import cl.duoc.veterinaria.clinica.dto.CitaRequest;
import cl.duoc.veterinaria.clinica.exception.AccesoDenegadoException;
import cl.duoc.veterinaria.clinica.exception.ConflictoException;
import cl.duoc.veterinaria.clinica.exception.DatosInvalidosException;
import cl.duoc.veterinaria.clinica.model.Cita;
import cl.duoc.veterinaria.clinica.model.EstadoCita;
import cl.duoc.veterinaria.clinica.model.Servicio;
import cl.duoc.veterinaria.clinica.repository.CitaRepository;
import cl.duoc.veterinaria.clinica.repository.ServicioRepository;
import cl.duoc.veterinaria.clinica.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    private static final ZoneId CHILE = ZoneId.of("America/Santiago");
    
    private static final Clock RELOJ = Clock.fixed(LocalDateTime.of(2026, 11, 2, 8, 0).atZone(CHILE).toInstant(), CHILE);
    private static final LocalDate MANANA = LocalDate.of(2026, 11, 3);

    @Mock
    private CitaRepository citas;
    @Mock
    private ServicioRepository servicios;

    private CitaService service;

    private final UsuarioAutenticado ana = new UsuarioAutenticado(1L, "Ana", "CLIENTE");
    private final UsuarioAutenticado recepcion = new UsuarioAutenticado(5L, "Rosa", "RECEPCIONISTA");

    @BeforeEach
    void crearServicio() {
        service = new CitaService(citas, servicios, RELOJ);
    }

    private Servicio servicio(String codigo, String nombre, int precio) {
        Servicio s = new Servicio();
        s.setCodigo(codigo);
        s.setNombre(nombre);
        s.setPrecio(precio);
        return s;
    }

    private Cita citaDe(Long clienteId, EstadoCita estado) {
        Cita c = new Cita();
        c.setId(10L);
        c.setClienteId(clienteId);
        c.setEstado(estado);
        return c;
    }

    @Test
    void creaLaCitaPendienteConDuenoYPrecioPuestosPorElServidor() {
        when(servicios.findById("SV001")).thenReturn(Optional.of(servicio("SV001", "Consulta general", 15000)));
        when(citas.existsByFechaAndHoraAndEstadoIn(eq(MANANA), eq(LocalTime.of(10, 0)), any())).thenReturn(false);
        when(citas.saveAndFlush(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = service.crear(new CitaRequest("Luna", "sv001", MANANA, LocalTime.of(10, 0)), ana);

        assertEquals(EstadoCita.PENDIENTE, cita.getEstado());
        assertEquals(15000, cita.getPrecio());
        assertEquals(1L, cita.getClienteId());
    }

    @Test
    void urgenciaFueraDeHorarioSumaElRecargo() {
        when(servicios.findById("SV002")).thenReturn(Optional.of(servicio("SV002", "Consulta urgencia", 25000)));
        when(citas.existsByFechaAndHoraAndEstadoIn(any(), any(), any())).thenReturn(false);
        when(citas.saveAndFlush(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = service.crear(new CitaRequest("Rayo", "SV002", MANANA, LocalTime.of(20, 0)), ana);

        assertEquals(35000, cita.getPrecio());
    }

    @Test
    void horarioOcupadoDa409YNoGuarda() {
        when(servicios.findById("SV001")).thenReturn(Optional.of(servicio("SV001", "Consulta general", 15000)));
        when(citas.existsByFechaAndHoraAndEstadoIn(any(), any(), any())).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(new CitaRequest("Luna", "SV001", MANANA, LocalTime.of(10, 0)), ana));
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    void servicioNormalNoSeAgendaDespuesDeLas19() {
        when(servicios.findById("SV001")).thenReturn(Optional.of(servicio("SV001", "Consulta general", 15000)));

        assertThrows(DatosInvalidosException.class,
                () -> service.crear(new CitaRequest("Luna", "SV001", MANANA, LocalTime.of(20, 0)), ana));
    }

    @Test
    void noSeAgendaEnElPasado() {
        when(servicios.findById("SV001")).thenReturn(Optional.of(servicio("SV001", "Consulta general", 15000)));
        LocalDate ayer = LocalDate.of(2026, 11, 1);

        assertThrows(DatosInvalidosException.class,
                () -> service.crear(new CitaRequest("Luna", "SV001", ayer, LocalTime.of(10, 0)), ana));
    }

    @Test
    void clienteNoPuedeVerLaCitaDeOtroCambiandoElId() {
        when(citas.findById(10L)).thenReturn(Optional.of(citaDe(2L, EstadoCita.PENDIENTE)));

        assertThrows(AccesoDenegadoException.class, () -> service.ver(10L, ana));
    }

    @Test
    void recepcionPuedeVerCualquierCita() {
        when(citas.findById(10L)).thenReturn(Optional.of(citaDe(2L, EstadoCita.PENDIENTE)));

        assertEquals(10L, service.ver(10L, recepcion).getId());
    }

    @Test
    void noSePuedeConfirmarUnaCitaCancelada() {
        when(citas.findById(10L)).thenReturn(Optional.of(citaDe(1L, EstadoCita.CANCELADA)));

        assertThrows(ConflictoException.class, () -> service.confirmar(10L));
    }
}
