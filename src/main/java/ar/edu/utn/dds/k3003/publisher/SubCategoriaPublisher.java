package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.events.subcategoriaEvents.SubCategoriaAgregadaEvent;
import ar.edu.utn.dds.k3003.model.donaciones.SubCategoria;
import org.springframework.stereotype.Component;

@Component
public class SubCategoriaPublisher {

    private final EventPublisher eventPublisher;

    public SubCategoriaPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarSubCategoriaCreada(SubCategoria subCategoria) {
        SubCategoriaAgregadaEvent evento = new SubCategoriaAgregadaEvent(
                subCategoria.getId(),
                subCategoria.getNombre(),
                subCategoria.getDescripcion(),
                subCategoria.getCategoria() != null ? subCategoria.getCategoria().getId().toString() : null
        );
        eventPublisher.publicar("subcategoria-agregada", evento);
        System.out.println("Subcategoría creada enviada a RabbitMQ con ID: " + subCategoria.getId());
    }
}