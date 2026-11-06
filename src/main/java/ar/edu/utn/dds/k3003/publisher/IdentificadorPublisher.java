package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.events.identificadorEvents.IdentificadorCreadoEvent;
import ar.edu.utn.dds.k3003.events.identificadorEvents.IdentificadorEliminadoEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class IdentificadorPublisher {

    private final EventPublisher eventPublisher;

    public IdentificadorPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarIdentificadorCreado(Long id, IdentificadorDTO identificadorDTO) {
        IdentificadorCreadoEvent evento = new IdentificadorCreadoEvent(
                id.toString(),
                identificadorDTO.tipo(),
                identificadorDTO.descripcion(),
                Instant.now()
        );

        eventPublisher.publicar("identificador-creado", evento);
        System.out.println("Identificador creado enviado a RabbitMQ para el ID: " + identificadorDTO.id());
    }

    public void notificarIdentificadorEliminado(String id) {
        IdentificadorEliminadoEvent evento = new IdentificadorEliminadoEvent(
                id,
                Instant.now()
        );

        eventPublisher.publicar("identificador-eliminado", evento);
        System.out.println("Identificador eliminado enviado a RabbitMQ para el ID: " + id);
    }
}