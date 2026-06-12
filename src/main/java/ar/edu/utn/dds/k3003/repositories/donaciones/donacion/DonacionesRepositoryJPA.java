package ar.edu.utn.dds.k3003.repositories.donaciones.donacion;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonacionesRepositoryJPA extends JpaRepository<Donacion, Long> {
    List<Donacion> findByDonadorIDAndFechaGreaterThanEqualOrderByFechaAsc(String donadorID, LocalDate fecha);
}