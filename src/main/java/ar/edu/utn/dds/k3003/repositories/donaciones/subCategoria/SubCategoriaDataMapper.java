package ar.edu.utn.dds.k3003.repositories.donaciones.subCategoria;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.SubCategoriaDTO;
import ar.edu.utn.dds.k3003.model.donaciones.SubCategoria;

public class SubCategoriaDataMapper {

    public SubCategoria toSubCategoria(SubCategoriaDTO dto) {
        if (dto == null) return null;
        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setNombre(dto.nombre());
        subCategoria.setDescripcion(dto.descripcion());
        return subCategoria;
    }

    public SubCategoriaDTO toSubCategoriaDTO(SubCategoria subCategoria) {
        if (subCategoria == null) return null;
        return new SubCategoriaDTO(
                subCategoria.getId() != null ? subCategoria.getId().toString() : null,
                subCategoria.getNombre(),
                subCategoria.getDescripcion(),
                subCategoria.getCategoria() != null ? subCategoria.getCategoria().getId().toString() : null
        );
    }
}