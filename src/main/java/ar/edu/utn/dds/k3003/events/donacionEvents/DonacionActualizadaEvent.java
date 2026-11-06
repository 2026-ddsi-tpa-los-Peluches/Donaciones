package ar.edu.utn.dds.k3003.events.donacionEvents;


import java.time.Instant;
import java.time.LocalDate;

public record DonacionActualizadaEvent(
        String donacionId, String donadorId, String productoId, String nombreProducto,
        String categoriaProducto, String subcategoriaProducto, int cantidad, String estado,
        Instant fechaIngreso, Queja queja, Instant fechaEvento
) {
    public record Queja(String quejaId, LocalDate fecha, String descripcion) {}
}