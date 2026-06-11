package ar.edu.utn.dds.k3003.model.donaciones;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identificador_id")
    private Identificador identificador;

    protected Producto() {}

    public Producto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean tieneID(String id) {
        return this.id != null && this.id.toString().equals(id);
    }

    public boolean tieneSubcategoria(String subcategoriaID) {
        return this.subcategoria != null
                && this.subcategoria.getId().toString().equals(subcategoriaID);
    }

    public String getSubcategoriaID() {
        return this.subcategoria != null ? this.subcategoria.getId().toString() : null;
    }

    public void setSubcategoriaID(String subcategoriaID) {
    }

}