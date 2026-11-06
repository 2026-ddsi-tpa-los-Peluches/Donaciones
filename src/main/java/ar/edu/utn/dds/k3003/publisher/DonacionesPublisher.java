package ar.edu.utn.dds.k3003.publisher;


import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.events.donacionEvents.DonacionActualizadaEvent;
import ar.edu.utn.dds.k3003.events.donacionEvents.DonacionConQuejaRegistradaEvent;
import ar.edu.utn.dds.k3003.events.donacionEvents.DonacionCreadaEvent;
import ar.edu.utn.dds.k3003.events.donacionEvents.DonacionEstadoActualizadoEvent;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;
import ar.edu.utn.dds.k3003.model.donaciones.Producto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DonacionesPublisher {

    private final EventPublisher eventPublisher;

    // Inyección por constructor (recomendado en Spring)
    public DonacionesPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
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

    public void notificarDonacionCreada(Donacion donacion) {

        String nombreProd = donacion.getProducto() != null ? donacion.getProducto().getNombre() : null;

        String catProd = (donacion.getProducto() != null && donacion.getProducto().getCategoria() != null)

                ? donacion.getProducto().getCategoria().getNombre() : "General";
        String subCatProd = (donacion.getProducto() != null && donacion.getProducto().getSubcategoria() != null)
                ? donacion.getProducto().getSubcategoria().getNombre() : "General";

        Instant fechaIngresoInstant = donacion.getFecha() != null
                ? donacion.getFecha().atStartOfDay(ZoneId.systemDefault()).toInstant()
                : Instant.now();

        DonacionCreadaEvent evento = new DonacionCreadaEvent(
                String.valueOf(donacion.getId()),
                donacion.getDonadorID(),
                donacion.getProducto() != null ? String.valueOf(donacion.getProducto().getId()) : null,
                nombreProd,
                catProd,
                subCatProd,
                donacion.getCantidad(),
                donacion.getEstado() != null ? donacion.getEstado().name() : "INGRESADA",
                fechaIngresoInstant,
                Instant.now()
        );

        // Publicamos al exchange configurado
        eventPublisher.publicar( "donacion-creada", evento);
        System.out.println("Evento de donacion creada enviado a RabbitMQ para la donación ID:" + donacion.getId());
    }


    public void notificarEstadoCambiado(Donacion donacion) {
        DonacionEstadoActualizadoEvent evento = new DonacionEstadoActualizadoEvent(
                String.valueOf(donacion.getId()),
                donacion.getEstado() != null ? donacion.getEstado().name() : "INGRESADA",
                Instant.now()
        );

        // Publicamos al exchange configurado
        eventPublisher.publicar("donacion-estado-actualizado", evento);
        System.out.println("Evento de cambio de estado enviado a RabbitMQ para la donación ID: " + donacion.getId());
    }
}