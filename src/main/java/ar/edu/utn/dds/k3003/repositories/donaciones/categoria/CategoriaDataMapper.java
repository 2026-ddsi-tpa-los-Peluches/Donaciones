package ar.edu.utn.dds.k3003.repositories.donaciones.categoria;


import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.CategoriaDTO;
import ar.edu.utn.dds.k3003.model.donaciones.Categoria;


public class CategoriaDataMapper {
    public Categoria toCategoria(CategoriaDTO categoriaDTO) {
        return new Categoria(

                categoriaDTO.nombre(),
                categoriaDTO.descripcion()
        );
    }

    public CategoriaDTO toCategoriaDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId() != null ? categoria.getId().toString() : null,
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
