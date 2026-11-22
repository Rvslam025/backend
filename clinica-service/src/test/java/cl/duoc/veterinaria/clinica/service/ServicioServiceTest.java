package cl.duoc.veterinaria.clinica.service;

import cl.duoc.veterinaria.clinica.dto.ServicioRequest;
import cl.duoc.veterinaria.clinica.exception.ConflictoException;
import cl.duoc.veterinaria.clinica.model.Servicio;
import cl.duoc.veterinaria.clinica.repository.CitaRepository;
import cl.duoc.veterinaria.clinica.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicios;
    @Mock
    private CitaRepository citas;

    private ServicioService service;

    @BeforeEach
    void crearServicio() {
        service = new ServicioService(servicios, citas);
    }

    @Test
    void codigoRepetidoDa409() {
        when(servicios.existsById("SV001")).thenReturn(true);
        var req = new ServicioRequest("sv001", "Consultas", "Otra consulta", "Perro", "30 min", 15000, "");

        assertThrows(ConflictoException.class, () -> service.crear(req));
        verify(servicios, never()).save(any());
    }

    @Test
    void noSeEliminaUnServicioConCitas() {
        Servicio consulta = new Servicio();
        consulta.setCodigo("SV001");
        consulta.setNombre("Consulta general");
        when(servicios.findById("SV001")).thenReturn(Optional.of(consulta));
        when(citas.existsByServicioCodigo("SV001")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.eliminar("SV001"));
        verify(servicios, never()).delete(any());
    }
}
