package cl.duoc.ms_clientes.dto;

import cl.duoc.ms_clientes.entity.Usuario;

/** Dirección de despacho del usuario. El teléfono va en formato +569XXXXXXXX. */
public record DireccionDespacho(
        String direccion,
        String comuna,
        String region,
        String telefono) {

    /** null si el usuario todavía no guarda una dirección. */
    public static DireccionDespacho from(Usuario usuario) {
        if (usuario.getDireccion() == null || usuario.getDireccion().isBlank()) {
            return null;
        }
        return new DireccionDespacho(
                usuario.getDireccion(),
                usuario.getComuna(),
                usuario.getRegion(),
                usuario.getTelefono());
    }
}
