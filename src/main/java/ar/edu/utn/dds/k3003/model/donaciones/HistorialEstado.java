package ar.edu.utn.dds.k3003.model.donaciones;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "historial_estados")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDonacionEnum estado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donacion_id")
    private Donacion donacion;

    protected HistorialEstado() {}

    public HistorialEstado(EstadoDonacionEnum estado, LocalDate fecha, Donacion donacion) {
        this.estado = estado;
        this.fecha = fecha;
        this.donacion = donacion;
    }

    public String mostrarEstado() {
        return this.fecha.toString() + " - " + this.estado.name();
    }
}