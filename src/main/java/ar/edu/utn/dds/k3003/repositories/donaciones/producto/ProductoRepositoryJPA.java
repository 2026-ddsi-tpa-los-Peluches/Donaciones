package ar.edu.utn.dds.k3003.repositories.donaciones.producto;

import ar.edu.utn.dds.k3003.model.donaciones.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepositoryJPA extends JpaRepository<Producto, Long> {
    List<Producto> findBySubcategoria_Id(Long subcategoriaId);
}