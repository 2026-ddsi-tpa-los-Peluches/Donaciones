package ar.edu.utn.dds.k3003.publisher;


import ar.edu.utn.dds.k3003.events.DonacionActualizadaEvent;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;
import ar.edu.utn.dds.k3003.model.donaciones.Producto;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DonacionesPublisher {

    private final EventPublisher eventPublisher;

    // Inyección por constructor (recomendado en Spring)
    public DonacionesPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarDonacion(Donacion donacion) {

        Producto producto = donacion.getProducto();
        DonacionActualizadaEvent evento = new DonacionActualizadaEvent(
                String.valueOf(donacion.getId()),
                donacion.getDonadorID(),
                producto != null && producto.getId() != null ? String.valueOf(producto.getId()) : null,
                producto != null ? producto.getNombre() : null,
                producto != null ? producto.getCategoriaID() : null,
                producto != null && producto.getSubcategoria() != null ? producto.getSubcategoria().getNombre() : null,
                donacion.getCantidad(),
                donacion.getEstado() != null ? donacion.getEstado().toString() : null,
                Instant.now(),
                null, //
                Instant.now()
        );

        // Publicamos el evento al Exchange y RoutingKey correspondientes (reemplaza por tus constantes)
        eventPublisher.publicar("donaciones", evento);
        System.out.println("Donación enviada a RabbitMQ con ID: " + donacion.getId());
    }
}