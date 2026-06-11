package ar.edu.utn.dds.k3003.model.donaciones;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.exceptions.donaciones.CambioEstadoInvalidoException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donador_id", nullable = false)
    private String donadorID;

    @Column(name = "deposito_id")
    private String depositoID;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDonacionEnum estado;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEstado> historialEstados = new ArrayList<>();

    protected Donacion() {}

    public Donacion(String donadorID, String depositoID, String descripcion, Integer cantidad) {
        this.donadorID = donadorID;
        this.depositoID = depositoID;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.estado = EstadoDonacionEnum.INGRESADA;
        this.fecha = LocalDate.now();
        this.historialEstados.add(new HistorialEstado(this.estado, this.fecha, this));
    }

    public void agregarQueja(String descripcion) {
        this.cambiarEstado(EstadoDonacionEnum.CONQUEJA);
        this.descripcion = this.descripcion + ". Queja: " + descripcion;
    }

    public void cambiarEstado(EstadoDonacionEnum nuevoEstado) {
        if (!this.cambioValido(nuevoEstado))
            throw new CambioEstadoInvalidoException("No se puede cambiar al estado solicitado");
        this.estado = nuevoEstado;
        this.historialEstados.add(new HistorialEstado(nuevoEstado, LocalDate.now(), this));
    }

    private boolean cambioValido(EstadoDonacionEnum nuevoEstado) {
        return switch (this.estado) {
            case INGRESADA -> nuevoEstado == EstadoDonacionEnum.ACEPTADA;
            case ACEPTADA  -> nuevoEstado == EstadoDonacionEnum.CONQUEJA;
            case CONQUEJA  -> false;
        };
    }

    public boolean tieneID(String id) {
        return this.id != null && this.id.toString().equals(id);
    }

    public boolean donadorConID(String donadorID) {
        return this.donadorID.equals(donadorID);
    }

    public boolean aPartirDeFecha(LocalDate fecha) {
        return !this.fecha.isBefore(fecha);
    }
}