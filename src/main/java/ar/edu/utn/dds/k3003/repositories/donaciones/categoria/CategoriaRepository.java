package ar.edu.utn.dds.k3003.repositories.donaciones.categoria;

import ar.edu.utn.dds.k3003.model.donaciones.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    Optional<Categoria> buscarCategoriaPorId(String categoriaID);

    Categoria guardar(Categoria nuevaCategoria);

    void actualizar(Categoria categoria);

    List<Categoria> buscarTodos();

    void eliminarPorId(String id);
}
