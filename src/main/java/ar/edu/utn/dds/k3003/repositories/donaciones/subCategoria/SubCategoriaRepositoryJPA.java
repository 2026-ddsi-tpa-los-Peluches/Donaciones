package ar.edu.utn.dds.k3003.repositories.donaciones.subCategoria;

import ar.edu.utn.dds.k3003.model.donaciones.Identificador;
import ar.edu.utn.dds.k3003.model.donaciones.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubCategoriaRepositoryJPA extends JpaRepository<SubCategoria, Long> {
}