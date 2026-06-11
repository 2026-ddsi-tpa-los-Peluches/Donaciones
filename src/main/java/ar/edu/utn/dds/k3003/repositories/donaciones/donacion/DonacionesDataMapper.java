package ar.edu.utn.dds.k3003.repositories.donaciones.donacion;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;

public class DonacionesDataMapper {
    public Donacion toDonacion(DonacionDTO donacionDTO) {
        return new Donacion(
                donacionDTO.donadorID(),
                donacionDTO.depositoID(),
                donacionDTO.descripcion(),
                donacionDTO.cantidad()
        );
    }

    public DonacionDTO toDonacionDTO(Donacion donacion) {
        return new DonacionDTO(
                donacion.getId() != null ? donacion.getId().toString() : null,
                donacion.getDonadorID(),
                donacion.getDepositoID(),
                donacion.getDescripcion(),
                donacion.getProducto() != null ? donacion.getProducto().getId().toString() : null,
                donacion.getCantidad(),
                donacion.getEstado()
        );
    }
}
