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
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identificador_id")
    private Identificador identificador;

    protected Producto() {}

    public Producto(String nombre, String descripcion ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }


    public Producto(String nombre, String descripcion,Categoria categoria,Identificador identificador) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.identificador = identificador;
        this.categoria = categoria;
    }


    public boolean tieneID(String id) {
        return this.id != null && this.id.toString().equals(id);
    }

    public boolean tieneCategoria(String categoriaID) {
        return this.categoria != null
                && this.categoria.getId().toString().equals(categoriaID);
    }

    public String getCategoriaID() {
        return this.categoria != null ? this.categoria.getId().toString() : null;
    }

}