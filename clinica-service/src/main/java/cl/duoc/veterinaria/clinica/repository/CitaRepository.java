package cl.duoc.veterinaria.clinica.repository;

import cl.duoc.veterinaria.clinica.model.Cita;
import cl.duoc.veterinaria.clinica.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByClienteIdOrderByFechaAscHoraAsc(Long clienteId);

    List<Cita> findAllByOrderByFechaAscHoraAsc();

    List<Cita> findByEstadoOrderByFechaAscHoraAsc(EstadoCita estado);

    
    boolean existsByFechaAndHoraAndEstadoIn(LocalDate fecha, LocalTime hora, Collection<EstadoCita> estados);

    
    boolean existsByFechaAndHoraAndEstadoInAndIdNot(LocalDate fecha, LocalTime hora,
                                                    Collection<EstadoCita> estados, Long id);

    boolean existsByServicioCodigo(String codigo);
}
