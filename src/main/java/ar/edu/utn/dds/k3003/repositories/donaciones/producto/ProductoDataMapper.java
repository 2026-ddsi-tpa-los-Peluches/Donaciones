package ar.edu.utn.dds.k3003.repositories.donaciones.producto;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.model.donaciones.Producto;

public class ProductoDataMapper {
    public Producto toProducto(ProductoDTO productoDTO) {
        return new Producto(
                productoDTO.nombre(),
                productoDTO.descripcion()
        );
    }

    public ProductoDTO toProductoDTO(Producto producto) {
        return new ProductoDTO(
                producto.getId() != null ? producto.getId().toString() : null,
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoriaID(),
                producto.getIdentificador() != null ? producto.getIdentificador().getId().toString() : null
        );
    }
}
