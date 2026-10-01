package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.componentes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.componentes.LogisticaClient;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonacionInvalidaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonadorNoAptoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonacionNoEncontradaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.IdentificadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.CategoriaNoEncontradaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.ProductoNoEncontradoException;
import ar.edu.utn.dds.k3003.model.donaciones.*;
import ar.edu.utn.dds.k3003.repositories.donaciones.categoria.CategoriaDataMapper;
import ar.edu.utn.dds.k3003.repositories.donaciones.categoria.CategoriaRepositoryJPA;
import ar.edu.utn.dds.k3003.repositories.donaciones.donacion.DonacionesDataMapper;
import ar.edu.utn.dds.k3003.repositories.donaciones.donacion.DonacionesRepositoryJPA;
import ar.edu.utn.dds.k3003.repositories.donaciones.identificador.IdentificadoresDataMapper;
import ar.edu.utn.dds.k3003.repositories.donaciones.identificador.IdentificadoresRepositoryJPA;
import ar.edu.utn.dds.k3003.repositories.donaciones.producto.ProductoDataMapper;
import ar.edu.utn.dds.k3003.repositories.donaciones.producto.ProductoRepositoryJPA;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class Fachada implements FachadaDonaciones {

    // Repositories inyectados por Spring
    @Autowired
    private DonacionesRepositoryJPA donacionesRepository;
    @Autowired
    private IdentificadoresRepositoryJPA identificadoresRepository;
    @Autowired
    private ProductoRepositoryJPA productoRepository;
    @Autowired
    private CategoriaRepositoryJPA categoriaRepository;

    @Autowired
    private LogisticaClient logisticaClient;

    @Autowired
    private DonadoresYEntidadesClient donadoresYEntidadesClient;

    // Mappers (no son beans, se instancian directamente)
    private final DonacionesDataMapper donacionesDataMapper = new DonacionesDataMapper();
    private final ProductoDataMapper productoDataMapper = new ProductoDataMapper();
    private final IdentificadoresDataMapper identificadoresDataMapper = new IdentificadoresDataMapper();
    private final CategoriaDataMapper categoriaDataMapper = new CategoriaDataMapper();

    // Métricas
    private final Counter cantDonacionesRegistradas;
    private final Counter cantDonacionesFallidas;
    //private final Timer tiempoRegistroDonacion;
    private final Counter cantProductosRegistrados;
    private final Counter cantQuejasRegistradas;
    private final Counter cantCategoriasRegistradas;

    // Constructor vacío requerido por Spring/JPA
    public Fachada(MeterRegistry meterRegistry) {

        this.cantDonacionesRegistradas = Counter.builder("donaciones.registradas")
                .register(meterRegistry);
        this.cantDonacionesFallidas = Counter.builder("donaciones.fallidas")
                .register(meterRegistry);
        // this.tiempoRegistroDonacion = Timer.builder("donaciones.registro.tiempo")
        //         .register(meterRegistry);

        this.cantProductosRegistrados = Counter.builder("productos.registrados")
                .register(meterRegistry);

        this.cantQuejasRegistradas = Counter.builder("quejas.registradas")
                .description("Cantidad total de quejas registradas, independientemente de la donación")
                .register(meterRegistry);
        this.cantCategoriasRegistradas = Counter.builder("cateorias.registradas")
                .register(meterRegistry);

        // cantidad de donaciones actuales en tiempo real, o sea si se elima una donacion, impacta
        Gauge.builder("donaciones.activas", donacionesRepository, CrudRepository::count)
                .description("Cantidad de donaciones actualmente existentes")
                .register(meterRegistry);

        Gauge.builder("productos.activos", productoRepository, CrudRepository::count)
                .description("Cantidad de productos actualmente existentes")
                .register(meterRegistry);

    }

    /*------------------------------------------------------------Donaciones--------------------------------------------------------------------------------- */
    @Override
    public DonacionDTO registrarDonacion(DonacionDTO donacionDTO) {
        try {
            this.verificarDonacionIngresada(donacionDTO);
            this.verificarDonador(donacionDTO.donadorID());

            val donacion = this.donacionesDataMapper.toDonacion(donacionDTO);

            if (donacionDTO.productoID() != null) {
                Producto producto = productoRepository.findById(Long.parseLong(donacionDTO.productoID()))
                        .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
                donacion.setProducto(producto);
            }

            val donacionGuardada = this.donacionesRepository.save(donacion);

            this.logisticaClient.gestionarDonacion(
                    donacionGuardada.getDepositoID(),
                    donacionGuardada.getId().toString(),
                    donacionGuardada.getProducto() != null
                            ? donacionGuardada.getProducto().getId().toString()
                            : null,
                    donacionGuardada.getCantidad()
            );

            cantDonacionesRegistradas.increment();

            log.info("Donación creada: id={}, donadorID={}, depositoID={}, cantidad={}",
                    donacionGuardada.getId(), donacionGuardada.getDonadorID(),
                    donacionGuardada.getDepositoID(), donacionGuardada.getCantidad());

            return this.donacionesDataMapper.toDonacionDTO(donacionGuardada);

        } catch (Exception e) {
            // Incrementamos el counter de fallo
            cantDonacionesFallidas.increment();
            if (e instanceof DonacionInvalidaException
                    || e instanceof DonadorNoAptoException
                    || e instanceof DonadorNoEncontradoException
                    || e instanceof ProductoNoEncontradoException) {
                log.warn("Registro de donación rechazado: {}", e.getMessage());
            } else {
                log.error("Error inesperado al registrar donación", e);
            }
            throw e;
        }
    }

    @Override
    public DonacionDTO buscarDonacionPorID(String donacionID) throws NoSuchElementException {
        val donacion = this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("Donación no encontrada: " + donacionID));
        log.info("Consulta de Donacion realizada por ID con exito: donacionID={}",donacionID);
        return this.donacionesDataMapper.toDonacionDTO(donacion);
    }

    @Override
    public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado) throws NoSuchElementException {
        val donacion = this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("Donación no encontrada: " + donacionID));
        donacion.cambiarEstado(estado);
        this.donacionesRepository.save(donacion);
        log.info("Estado de Donación cambiado: id={}, estado={}", donacionID, estado);
        return this.donacionesDataMapper.toDonacionDTO(donacion);
    }

    @Override
    public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha) throws NoSuchElementException {
        log.info("Consulta de donaciones por Donador y Fecha de Inicio: donacionID={} fecha={}",donadorID,fecha);
        val donaciones = this.donacionesRepository.findByDonadorIDAndFechaGreaterThanEqualOrderByFechaAsc(donadorID, fecha);
        return donaciones.stream().map(this.donacionesDataMapper::toDonacionDTO).toList();
    }

    @Override
    public DonacionDTO registrarQuejaEnDonacion(String donacionID, String descripcion) {
        val donacion = this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("Donación no encontrada"));

        QuejaDTO quejaDTO = new QuejaDTO(null, donacionID, donacion.getDonadorID(), LocalDate.now(), descripcion);

        donacion.agregarQueja(descripcion);

        this.donacionesRepository.save(donacion);
        this.donadoresYEntidadesClient.agregarQueja(quejaDTO);

        cantQuejasRegistradas.increment();

        log.info("Queja creada: donacionID={}, donadorID={}", donacionID, donacion.getDonadorID());

        return this.donacionesDataMapper.toDonacionDTO(donacion);
    }

    public List<DonacionDTO> obtenerTodasLasDonaciones() {
        log.info("Consulta de todas las Donaciones realizadas: cantidad={}", donacionesRepository.count());
        return this.donacionesRepository.findAll().stream()
                .map(this.donacionesDataMapper::toDonacionDTO).toList();
    }

    public void eliminarDonacion(String id) {
        this.donacionesRepository.deleteById(Long.parseLong(id));
        log.info("Donación eliminada: id={}", id);
    }

    /*------------------------------------------------------------Productos--------------------------------------------------------------------------------- */

    @Override
    public ProductoDTO agregarProducto(ProductoDTO productoDTO) {
        this.verificarProductoIngresado(productoDTO);
        val producto = this.productoDataMapper.toProducto(productoDTO);

        // Asociar identificador
        if (productoDTO.identificadorID() != null) {

            Identificador identificador = identificadoresRepository.findById(Long.parseLong(productoDTO.identificadorID()))
                    .orElseThrow(() -> new IdentificadorNoEncontradoException("Identificador no encontrado"));

            producto.setIdentificador(identificador);

            identificador.validar(producto);
        }

        // Asociar categoria
        if (productoDTO.categoriaID() != null) {
            Categoria categoria = categoriaRepository.findById(Long.parseLong(productoDTO.categoriaID()))
                    .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada"));

            producto.setCategoria(categoria);
        }

        val productoGuardado = this.productoRepository.save(producto);
        cantProductosRegistrados.increment();

        val resultado = this.productoDataMapper.toProductoDTO(productoGuardado);
        log.info("Producto creado: id={}, identificadorID={}, categoriaID={}",
                resultado.id(), productoDTO.identificadorID(), productoDTO.categoriaID());
        return resultado;
    }

    @Override
    public ProductoDTO buscarProductoPorID(String productoID) throws NoSuchElementException {
        try {
            Long id = Long.parseLong(productoID);
            val producto = this.productoRepository.findById(id)
                    .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado: " + productoID));
            log.info("Consulta de Producto por ID: productoID={}",productoID);
            return this.productoDataMapper.toProductoDTO(producto);
        } catch (NumberFormatException e) {
            // Si el ID no es numérico (ej: "PROD-101"), lanzamos la misma excepción
            throw new ProductoNoEncontradoException("Producto no encontrado : " + productoID);
        }
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        log.info("Consulta de todos los Productos: cantidad={}", productoRepository.count());
        return this.productoRepository.findAll().stream()
                .map(this.productoDataMapper::toProductoDTO).toList();
    }

    public void eliminarProducto(String id) {
        this.productoRepository.deleteById(Long.parseLong(id));
        log.info("Producto eliminado: id={}", id);
    }

    public ProductoDTO actualizarProducto(String id, ProductoDTO productoDTO) {
        val producto = this.productoRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
        producto.setNombre(productoDTO.nombre());
        producto.setDescripcion(productoDTO.descripcion());
        this.productoRepository.save(producto);
        log.info("Producto actualizado: productoID={}", id);
        return this.productoDataMapper.toProductoDTO(producto);
    }

    /*------------------------------------------------------------Identificadores--------------------------------------------------------------------------------- */

    @Override
    public IdentificadorDTO agregarIdentificador(IdentificadorDTO identificadorDTO) {
        this.verificarIdentificadorIngresado(identificadorDTO);
        val identificador = this.identificadoresDataMapper.toIdentificador(identificadorDTO);
        val guardado = this.identificadoresRepository.save(identificador);

        val resultado = this.identificadoresDataMapper.toIdentificadorDTO(guardado);
        log.info("Identificador creado: id={}", resultado.id());
        return resultado;
    }

    @Override
    public IdentificadorDTO buscarIdentificadorPorID(String identificadorID) throws NoSuchElementException {
        log.info("Consulta de Identificador por ID: identificadorID={}", identificadorID);
        val identificador = this.identificadoresRepository.findById(Long.parseLong(identificadorID))
                .orElseThrow(() -> new IdentificadorNoEncontradoException("Identificador no encontrado: " + identificadorID));
        return this.identificadoresDataMapper.toIdentificadorDTO(identificador);
    }

    @Override
    public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {

    }

    @Override
    public void setFachadaLogistica(FachadaLogistica fachadaLogistica) {

    }

    public List<IdentificadorDTO> obtenerTodasLosIdentificadores() {
        log.info("Consulta de todos los Identificadores: cantidad={}", identificadoresRepository.count());
        return this.identificadoresRepository.findAll().stream()
                .map(this.identificadoresDataMapper::toIdentificadorDTO).toList();
    }

    public void eliminarIdentificador(String id) {
        this.identificadoresRepository.deleteById(Long.parseLong(id));
        log.info("Identificador eliminado: id={}", id);
    }

    /*------------------------------------------------------------Categorias--------------------------------------------------------------------------------- */

    public CategoriaDTO agregarCategoria(CategoriaDTO categoriaDTO) {
        this.verificarCategoriaIngresada(categoriaDTO);
        val categoria = this.categoriaDataMapper.toCategoria(categoriaDTO);
        val guardada = this.categoriaRepository.save(categoria);

        cantCategoriasRegistradas.increment();

        val resultado = this.categoriaDataMapper.toCategoriaDTO(guardada);
        log.info("Categoría creada: id={}", resultado.id());
        return resultado;
    }

    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        log.info("Consulta de todas las Categorias: cantidad={}", categoriaRepository.count());
        return this.categoriaRepository.findAll().stream()
                .map(this.categoriaDataMapper::toCategoriaDTO).toList();
    }

    public void eliminarCategoria(String id) {
        this.categoriaRepository.deleteById(Long.parseLong(id));
        log.info("Categoría eliminada: id={}", id);
    }

    /*------------------------------------------------------------Validaciones--------------------------------------------------------------------------------- */

    private void verificarDonacionIngresada(DonacionDTO donacionDTO) {
        if (donacionDTO == null || donacionDTO.id() != null) {
            throw new DonacionInvalidaException("Donación inválida");
        }
    }

    private void verificarDonador(String donadorID) {

        try {

            if (!donadoresYEntidadesClient.puedeDonar(donadorID)) {
                log.warn("Donador {} no apto para donar", donadorID);
                throw new DonadorNoAptoException(
                        "El donador no se encuentra apto para donar");
            }

        } catch (HttpServerErrorException e) {

            if (e.getResponseBodyAsString()
                    .contains("No existe el donador")) {

                log.warn("Donador {} no encontrado en DonadoresYEntidades", donadorID);
                throw new DonadorNoEncontradoException(
                        e.getResponseBodyAsString());
            }

            log.error("Error al consultar DonadoresYEntidades para el donador {}", donadorID, e);
            throw e;
        }
    }

    private void verificarProductoIngresado(ProductoDTO productoDTO) {
        if (productoDTO == null || productoDTO.id() != null) {
            throw new DonacionInvalidaException("Producto inválido");
        }
    }

    private void verificarIdentificadorIngresado(IdentificadorDTO identificadorDTO) {
        if (identificadorDTO == null || identificadorDTO.id() != null) {
            throw new DonacionInvalidaException("Identificador inválido");
        }
    }

    private void verificarCategoriaIngresada(CategoriaDTO categoriaDTO) {
        if (categoriaDTO == null || categoriaDTO.id() != null) {
            throw new DonacionInvalidaException("Categoria inválida");
        }
    }

    /*------------------------------------------------------------Setters Fachadas--------------------------------------------------------------------------------- */


}