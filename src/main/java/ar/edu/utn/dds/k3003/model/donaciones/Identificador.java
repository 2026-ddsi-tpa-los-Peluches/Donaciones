package ar.edu.utn.dds.k3003.model.donaciones;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.TipoIdentificadorEnum;
import ar.edu.utn.dds.k3003.exceptions.donaciones.ProductoInvalidoException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "identificadores")
public class Identificador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descripcion")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoIdentificadorEnum tipoEnum;

    // No se persiste, se reconstruye desde tipoEnum al cargar de la BD
    @Transient
    private TipoIdentificador tipoIdentificador;

    protected Identificador() {}

    public Identificador(String descripcion, TipoIdentificador tipoIdentificador) {
        this.descripcion = descripcion;
        this.tipoIdentificador = tipoIdentificador;
        this.tipoEnum = tipoIdentificador.getEnum();
    }

    public boolean tieneID(String id) {
        return this.id != null && this.id.toString().equals(id);
    }

    public void validar(Producto producto) {
        if (this.tipoIdentificador == null)
            this.tipoIdentificador = reconstruirTipo();
        if (!this.tipoIdentificador.esValido(producto))
            throw new ProductoInvalidoException("Producto Inválido");
    }

    public TipoIdentificadorEnum getTipoIdentificador() {
        return this.tipoEnum;
    }

    private TipoIdentificador reconstruirTipo() {
        return switch (this.tipoEnum) {
            case CODIGODEBARRAS -> new CodigoDeBarras();
            case QR -> new CodigoQR();
        };
    }
}