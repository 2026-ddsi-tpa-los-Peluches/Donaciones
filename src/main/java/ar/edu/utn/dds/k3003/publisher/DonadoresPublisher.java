package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.events.donadorEvents.ActualizarCantidadDonacionesEstadoEvent;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DonadoresPublisher {

    private final EventPublisher eventPublisher;

    // Inyección por constructor (recomendado en Spring)
    public DonadoresPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarActualizacionEstadoDonador(Donacion donacion, String estadoAnterior){
        ActualizarCantidadDonacionesEstadoEvent evento = new ActualizarCantidadDonacionesEstadoEvent(
                donacion.getDonadorID(),
                estadoAnterior,
                donacion.getEstado().toString(),
                Instant.now()
        );
        eventPublisher.publicar("donador-donacion-estado", evento);
        System.out.println("Donador " + donacion.getDonadorID() + " realizo una donacion " + donacion.getEstado().toString());
    }

}
