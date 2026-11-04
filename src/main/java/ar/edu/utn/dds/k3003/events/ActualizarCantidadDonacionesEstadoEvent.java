package ar.edu.utn.dds.k3003.events;

public record ActualizarCantidadDonacionesEstadoEvent(
    String donadorID,
    String estado
) {}