package ar.edu.utn.dds.k3003.repositories.donaciones.identificador;

import ar.edu.utn.dds.k3003.model.donaciones.Identificador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentificadoresRepositoryJPA extends JpaRepository<Identificador, Long> {
}