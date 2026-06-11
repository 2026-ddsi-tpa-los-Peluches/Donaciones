package ar.edu.utn.dds.k3003.repositories.donaciones.identificador;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.model.donaciones.*;

public class IdentificadoresDataMapper {
    public Identificador toIdentificador(IdentificadorDTO identificadorDTO) {
        return new Identificador(
                identificadorDTO.descripcion(),
                this.tipoIdentificador(identificadorDTO)
                );
    }

    private TipoIdentificador tipoIdentificador(IdentificadorDTO identificadorDTO) {
        return switch (identificadorDTO.tipo()) {
            case CODIGODEBARRAS -> new CodigoDeBarras();
            case QR -> new CodigoQR();
        };
    }

    public IdentificadorDTO toIdentificadorDTO(Identificador identificador) {
        return new IdentificadorDTO(
                identificador.getId() != null ? identificador.getId().toString() : null,
                identificador.getTipoIdentificador(),
                identificador.getDescripcion()
        );
    }
}
