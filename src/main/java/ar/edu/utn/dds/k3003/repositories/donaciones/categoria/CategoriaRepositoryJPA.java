package ar.edu.utn.dds.k3003.repositories.donaciones.categoria;

import ar.edu.utn.dds.k3003.model.donaciones.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepositoryJPA extends JpaRepository<Categoria, Long> {
}