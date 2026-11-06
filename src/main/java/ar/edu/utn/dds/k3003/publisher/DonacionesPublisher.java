package ar.edu.utn.dds.k3003.publisher;


import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.events.DonacionActualizadaEvent;
import ar.edu.utn.dds.k3003.events.donacionEvents.DonacionConQuejaRegistradaEvent;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;
import ar.edu.utn.dds.k3003.model.donaciones.Producto;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

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


    public void notificarDonacionConQueja(Donacion donacion, QuejaDTO quejaDTO) {
        DonacionConQuejaRegistradaEvent evento = new DonacionConQuejaRegistradaEvent(
                String.valueOf(donacion.getId()),
                quejaDTO.id(),
                LocalDate.now(),
                quejaDTO.descripcion(), // O el texto de la queja correspondiente
                donacion.getEstado() != null ? donacion.getEstado().toString() : "CONQUEJA",
                Instant.now()
        );

        eventPublisher.publicar( "donacion-queja", evento);
        System.out.println("Evento de queja enviado a RabbitMQ para la donación ID:" + donacion.getId());
    }
}