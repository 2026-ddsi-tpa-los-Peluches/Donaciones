package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonacionInvalidaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonadorNoAptoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonacionNoEncontradaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.IdentificadorNoEncontradoException;
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
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

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

    // Fachadas externas inyectadas por Spring
    private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
    private FachadaLogistica fachadaLogistica;

    // Mappers (no son beans, se instancian directamente)
    private final DonacionesDataMapper donacionesDataMapper = new DonacionesDataMapper();
    private final ProductoDataMapper productoDataMapper = new ProductoDataMapper();
    private final IdentificadoresDataMapper identificadoresDataMapper = new IdentificadoresDataMapper();
    private final CategoriaDataMapper categoriaDataMapper = new CategoriaDataMapper();

    // Métricas
    private final MeterRegistry meterRegistry;
    private final Counter cantDonacionesRegistradas;
    private final Counter cantDonacionesFallidas;
    private final Timer tiempoRegistroDonacion;

    // Constructor vacío requerido por Spring/JPA
    public Fachada(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.cantDonacionesRegistradas = Counter.builder("donaciones.registradas")
                .description("Cantidad de donaciones registradas")
                .tag("modulo", "donaciones")
                .register(meterRegistry);
        this.cantDonacionesFallidas = Counter.builder("donaciones.fallidas")
                .description("Cantidad de intentos de donación fallidos")
                .tag("modulo", "donaciones")
                .register(meterRegistry);

        this.tiempoRegistroDonacion = Timer.builder("donaciones.registro.tiempo")
                .description("Tiempo que tarda en registrarse una donación")
                .tag("modulo", "donaciones")
                .register(meterRegistry);
    }

    /*------------------------------------------------------------Donaciones--------------------------------------------------------------------------------- */
    @Override
    public DonacionDTO registrarDonacion(DonacionDTO donacionDTO) {
        // Timer mide el tiempo de ejecución completo
        return tiempoRegistroDonacion.record(() -> {
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

                fachadaLogistica.gestionarDonacion(
                        donacionGuardada.getDepositoID(),
                        donacionGuardada.getId().toString(),
                        donacionGuardada.getProducto() != null ? donacionGuardada.getProducto().getId().toString() : null,
                        donacionGuardada.getCantidad()
                );

                cantDonacionesRegistradas.increment();

                return this.donacionesDataMapper.toDonacionDTO(donacionGuardada);

            } catch (Exception e) {
                // Incrementamos el counter de fallo
                cantDonacionesFallidas.increment();
                throw e;
            }
        });
    }

    @Override
    public DonacionDTO buscarDonacionPorID(String donacionID) throws NoSuchElementException {
        val donacion = this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("Donación no encontrada: " + donacionID));
        return this.donacionesDataMapper.toDonacionDTO(donacion);
    }

    @Override
    public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado) throws NoSuchElementException {
        val donacion = this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("Donación no encontrada: " + donacionID));
        donacion.cambiarEstado(estado);
        this.donacionesRepository.save(donacion);
        return this.donacionesDataMapper.toDonacionDTO(donacion);
    }

    @Override
    public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha) throws NoSuchElementException {
        val donaciones = this.donacionesRepository.findByDonadorIDAndFechaGreaterThanEqual(donadorID, fecha);
        return donaciones.stream().map(this.donacionesDataMapper::toDonacionDTO).toList();
    }

    @Override
    public DonacionDTO registrarQuejaEnDonacion(String donacionID, String descripcion) {
        val donacion = this.donacionesRepository.findById(Long.parseLong(donacionID))
                .orElseThrow(() -> new DonacionNoEncontradaException("Donación no encontrada"));

        QuejaDTO quejaDTO = new QuejaDTO(null, donacionID, donacion.getDonadorID(), LocalDate.now(), descripcion);

        this.fachadaDonadoresYEntidades.agregarQueja(quejaDTO);

        donacion.agregarQueja(descripcion);
        this.donacionesRepository.save(donacion);

        return this.donacionesDataMapper.toDonacionDTO(donacion);
    }

    public List<DonacionDTO> obtenerTodasLasDonaciones() {
        return this.donacionesRepository.findAll().stream()
                .map(this.donacionesDataMapper::toDonacionDTO).toList();
    }

    public void eliminarDonacion(String id) {
        this.donacionesRepository.deleteById(Long.parseLong(id));
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
            identificador.validar(producto);
            producto.setIdentificador(identificador);
        }

        // Asociar subcategoria via categoria
        if (productoDTO.categoriaID() != null) {
            Categoria categoria = categoriaRepository.findById(Long.parseLong(productoDTO.categoriaID()))
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            // Buscamos subcategoria dentro de la categoria (si aplica)
        }

        val productoGuardado = this.productoRepository.save(producto);
        return this.productoDataMapper.toProductoDTO(productoGuardado);
    }

    @Override
    public ProductoDTO buscarProductoPorID(String productoID) throws NoSuchElementException {
        val producto = this.productoRepository.findById(Long.parseLong(productoID))
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado: " + productoID));
        return this.productoDataMapper.toProductoDTO(producto);
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        return this.productoRepository.findAll().stream()
                .map(this.productoDataMapper::toProductoDTO).toList();
    }

    public void eliminarProducto(String id) {
        this.productoRepository.deleteById(Long.parseLong(id));
    }

    public ProductoDTO actualizarProducto(String id, ProductoDTO productoDTO) {
        val producto = this.productoRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
        producto.setNombre(productoDTO.nombre());
        producto.setDescripcion(productoDTO.descripcion());
        this.productoRepository.save(producto);
        return this.productoDataMapper.toProductoDTO(producto);
    }

    /*------------------------------------------------------------Identificadores--------------------------------------------------------------------------------- */

    @Override
    public IdentificadorDTO agregarIdentificador(IdentificadorDTO identificadorDTO) {
        this.verificarIdentificadorIngresado(identificadorDTO);
        val identificador = this.identificadoresDataMapper.toIdentificador(identificadorDTO);
        val guardado = this.identificadoresRepository.save(identificador);
        return this.identificadoresDataMapper.toIdentificadorDTO(guardado);
    }

    @Override
    public IdentificadorDTO buscarIdentificadorPorID(String identificadorID) throws NoSuchElementException {
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
        return this.identificadoresRepository.findAll().stream()
                .map(this.identificadoresDataMapper::toIdentificadorDTO).toList();
    }

    public void eliminarIdentificador(String id) {
        this.identificadoresRepository.deleteById(Long.parseLong(id));
    }

    /*------------------------------------------------------------Categorias--------------------------------------------------------------------------------- */

    public CategoriaDTO agregarCategoria(CategoriaDTO categoriaDTO) {
        this.verificarCategoriaIngresada(categoriaDTO);
        val categoria = this.categoriaDataMapper.toCategoria(categoriaDTO);
        val guardada = this.categoriaRepository.save(categoria);
        return this.categoriaDataMapper.toCategoriaDTO(guardada);
    }

    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return this.categoriaRepository.findAll().stream()
                .map(this.categoriaDataMapper::toCategoriaDTO).toList();
    }

    public void eliminarCategoria(String id) {
        this.categoriaRepository.deleteById(Long.parseLong(id));
    }

    /*------------------------------------------------------------Validaciones--------------------------------------------------------------------------------- */

    private void verificarDonacionIngresada(DonacionDTO donacionDTO) {
        if (donacionDTO == null || donacionDTO.id() != null) {
            throw new DonacionInvalidaException("Donación inválida");
        }
    }

    private void verificarDonador(String donadorID) {

         this.fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
         if (!fachadaDonadoresYEntidades.puedeDonar(donadorID)) {

         throw new DonadorNoAptoException("El donador no se encuentra apto para donar");
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