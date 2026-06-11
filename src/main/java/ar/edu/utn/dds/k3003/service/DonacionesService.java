package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.exceptions.donaciones.*;
import ar.edu.utn.dds.k3003.model.donaciones.*;
import ar.edu.utn.dds.k3003.repositories.donaciones.categoria.CategoriaRepositoryJPA;
import ar.edu.utn.dds.k3003.repositories.donaciones.donacion.DonacionesRepositoryJPA;
import ar.edu.utn.dds.k3003.repositories.donaciones.identificador.IdentificadoresRepositoryJPA;
import ar.edu.utn.dds.k3003.repositories.donaciones.producto.ProductoRepositoryJPA;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonacionesService {

    @Autowired
    private DonacionesRepositoryJPA donacionesRepository;
    @Autowired
    private ProductoRepositoryJPA productoRepository;
    @Autowired
    private CategoriaRepositoryJPA categoriaRepository;
    @Autowired
    private IdentificadoresRepositoryJPA identificadoresRepository;

    public DonacionesService() {}

    public Donacion gestionarDonacionRecibida(Donacion donacion, String productoID) {
        val producto = this.buscarProducto(productoID);
        donacion.setProducto(producto);
        return this.donacionesRepository.save(donacion);
    }

    public Donacion registrarQueja(Donacion donacion, String descripcion) {
        donacion.agregarQueja(descripcion);
        return this.donacionesRepository.save(donacion);
    }

    public Producto buscarProducto(String productoID) {
        return this.productoRepository.findById(Long.parseLong(productoID))
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontró el producto con ID " + productoID));
    }

    public Donacion buscarDonacionPorId(String donacionID) {
        return this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("No se encontró donación con ID " + donacionID));
    }

    public List<Donacion> buscarDonacionPorDonadorYFechaInicio(String donadorID, LocalDate fecha) {
        val donaciones = this.donacionesRepository.findByDonadorIDAndFechaGreaterThanEqual(donadorID, fecha);
        if (donaciones.isEmpty()) {
            throw new DonacionNoEncontradaException(
                    "No se encontraron donaciones para el donador con ID " + donadorID + " a partir de la fecha " + fecha);
        }
        return donaciones;
    }

    public Donacion cambiarEstadoDonacion(String donacionID, EstadoDonacionEnum estado) {
        val donacion = this.buscarDonacionPorId(donacionID);
        donacion.cambiarEstado(estado);
        return this.donacionesRepository.save(donacion);
    }

    public Producto darAltaProducto(Producto producto, String categoriaID, String identificadorID) {
        val identificador = this.buscarIdentificador(identificadorID);

        if (categoriaID != null) {
            producto.setCategoria(this.buscarCategoria(categoriaID));
        }

        identificador.validar(producto);
        producto.setIdentificador(identificador);

        return this.productoRepository.save(producto);
    }

    public Identificador buscarIdentificador(String identificadorID) {
        return this.identificadoresRepository.findById(Long.parseLong(identificadorID))
                .orElseThrow(() -> new IdentificadorNoEncontradoException("No se encontró identificador con ID " + identificadorID));
    }

    public Categoria buscarCategoria(String categoriaID) {
        return this.categoriaRepository.findById(Long.parseLong(categoriaID))
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se encontró categoría con ID " + categoriaID));
    }

    public Identificador darAltaIdentificador(Identificador identificador) {
        return this.identificadoresRepository.save(identificador);
    }

    public List<Donacion> buscarTodasDonaciones() {
        return this.donacionesRepository.findAll();
    }

    public void eliminarDonacion(String id) {
        this.donacionesRepository.deleteById(Long.parseLong(id));
    }

    public List<Producto> buscarTodosLosProductos() {
        return this.productoRepository.findAll();
    }

    public void eliminarProducto(String id) {
        this.productoRepository.deleteById(Long.parseLong(id));
    }

    public Producto actualizarProducto(String id, ProductoDTO actualizacion) {
        val producto = this.buscarProducto(id);
        val identificador = this.buscarIdentificador(actualizacion.identificadorID());

        producto.setNombre(actualizacion.nombre());
        producto.setDescripcion(actualizacion.descripcion());
        if (actualizacion.categoriaID() != null) {
            producto.setCategoria(this.buscarCategoria(actualizacion.categoriaID()));
        }
        producto.setIdentificador(identificador);

        return this.productoRepository.save(producto);
    }

    public List<Identificador> buscarTodosLosIdentificadores() {
        return this.identificadoresRepository.findAll();
    }

    public void eliminarIdentificador(String id) {
        this.identificadoresRepository.deleteById(Long.parseLong(id));
    }

    public Categoria darAltaCategoria(Categoria categoria) {
        return this.categoriaRepository.save(categoria);
    }

    public List<Categoria> buscarTodasCategorias() {
        return this.categoriaRepository.findAll();
    }

    public void eliminarCategoria(String id) {
        this.categoriaRepository.deleteById(Long.parseLong(id));
    }
}