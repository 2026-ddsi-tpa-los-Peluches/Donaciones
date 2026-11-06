package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.CategoriaDTO;
import ar.edu.utn.dds.k3003.events.categoriaEvents.CategoriaCreadaEvent;
import ar.edu.utn.dds.k3003.events.categoriaEvents.CategoriaEliminadaEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CategoriaPublisher {

    private final EventPublisher eventPublisher;

    public CategoriaPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarCategoriaCreada(CategoriaDTO categoriaDTO) {
        CategoriaCreadaEvent evento = new CategoriaCreadaEvent(
                categoriaDTO.id(),
                categoriaDTO.nombre(),
                categoriaDTO.descripcion(),
                Instant.now()
        );

        eventPublisher.publicar("categoria-creada", evento);
        System.out.println("Categoría creada enviada a RabbitMQ para el ID: " + categoriaDTO.id());
    }

    public void notificarCategoriaEliminada(String id) {
        CategoriaEliminadaEvent evento = new CategoriaEliminadaEvent(
                id,
                Instant.now()
        );

        eventPublisher.publicar("categoria-eliminada", evento);
        System.out.println("Categoría eliminada enviada a RabbitMQ para el ID: " + id);
    }
}