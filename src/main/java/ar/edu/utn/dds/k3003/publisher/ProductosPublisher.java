package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.events.productoEvents.ProductoActualizadoEvent;
import ar.edu.utn.dds.k3003.events.productoEvents.ProductoCreadoEvent;
import ar.edu.utn.dds.k3003.events.productoEvents.ProductoEliminadoEvent;
import ar.edu.utn.dds.k3003.model.donaciones.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductosPublisher {

    private final EventPublisher eventPublisher;

    public ProductosPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarProductoCreado(Producto producto) {
        String categoriaId = producto.getCategoria() != null ? String.valueOf(producto.getCategoria().getId()) : null;
        String categoriaNombre = producto.getCategoria() != null ? producto.getCategoria().getNombre() : null;

        String subcategoriaId = producto.getSubcategoria() != null ? String.valueOf(producto.getSubcategoria().getId()) : null;
        String subcategoriaNombre = producto.getSubcategoria() != null ? producto.getSubcategoria().getNombre() : null;

        String identificadorId = producto.getIdentificador() != null ? String.valueOf(producto.getIdentificador().getId()) : null;
        String identificadorTipo = producto.getIdentificador() != null && producto.getIdentificador().getTipoIdentificador() != null
                ? producto.getIdentificador().getTipoIdentificador().name() : null;

        ProductoCreadoEvent evento = new ProductoCreadoEvent(
                String.valueOf(producto.getId()),
                producto.getNombre(),
                producto.getDescripcion(),
                categoriaId,
                categoriaNombre,
                subcategoriaId,
                subcategoriaNombre,
                identificadorId,
                identificadorTipo
        );

        eventPublisher.publicar("producto-creado", evento);
        System.out.println(" Producto creado con detalles enviado a RabbitMQ ID: " + producto.getId());
    }

    public void notificarProductoActualizado(Producto producto) {
        ProductoActualizadoEvent evento = new ProductoActualizadoEvent(
                String.valueOf(producto.getId()),
                producto.getNombre(),
                producto.getDescripcion()
        );
        eventPublisher.publicar("producto-actualizado", evento);
        System.out.println(" Producto actualizado enviado a RabbitMQ ID: " + producto.getId());
    }

    public void notificarProductoEliminado(String productoId) {
        ProductoEliminadoEvent evento = new ProductoEliminadoEvent(productoId);
        eventPublisher.publicar("producto-eliminado", evento);
        System.out.println(" Producto eliminado enviado a RabbitMQ ID: " + productoId);
    }


}