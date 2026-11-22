package cl.duoc.veterinaria.clinica.repository;

import cl.duoc.veterinaria.clinica.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, String> {
    List<Servicio> findAllByOrderByCodigo();
    List<Servicio> findByCategoriaOrderByCodigo(String categoria);
}
