package cl.duoc.ms_clientes.service;

import cl.duoc.ms_clientes.dto.DireccionDespacho;
import java.util.regex.Pattern;

import cl.duoc.ms_clientes.dto.UsuarioRequest;
import cl.duoc.ms_clientes.entity.RolUsuario;
import cl.duoc.ms_clientes.entity.Usuario;
import cl.duoc.ms_clientes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    // Celular chileno: +56 9 seguido de 8 dígitos
    private static final Pattern TELEFONO_CHILE = Pattern.compile("^\\+569\\d{8}$");

    private final UsuarioRepository usuarioRepository;
    private final String adminOid;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          @Value("${app.admin-oid:32a7ca6e-5a16-4dd0-9cc5-7ad24350eca3}") String adminOid) {
        this.usuarioRepository = usuarioRepository;
        this.adminOid = adminOid;
    }

    public Usuario buscarPorOid(String oid) {
        return usuarioRepository.findByEntraIdOid(oid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario buscarOCrearUsuario(UsuarioRequest request) {
        return usuarioRepository.findByEntraIdOid(request.oid())
                .map(usuarioExistente -> {
                    usuarioExistente.setNombre(request.nombre());
                    usuarioExistente.setEmail(request.email());
                    usuarioExistente.setRol(resolverRolUsuario(request.oid()));
                    return usuarioRepository.save(usuarioExistente);
                })
                .orElseGet(() -> {
                    Usuario nuevoUsuario = new Usuario(request.oid(), request.nombre(), request.email(), resolverRolUsuario(request.oid()));
                    return usuarioRepository.save(nuevoUsuario);
                });
    }

    public Usuario guardarDireccion(String oid, DireccionDespacho datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan los datos de despacho");
        }
        String direccion = limpiar(datos.direccion());
        String comuna = limpiar(datos.comuna());
        String region = limpiar(datos.region());
        String telefono = datos.telefono() == null ? "" : datos.telefono().replaceAll("[\\s-]", "");

        if (direccion.length() < 5 || direccion.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La dirección debe tener entre 5 y 120 caracteres");
        }
        if (comuna.isEmpty() || region.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica la región y la comuna");
        }
        if (!TELEFONO_CHILE.matcher(telefono).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El teléfono debe ser un celular chileno: +56 9 y 8 dígitos");
        }

        Usuario usuario = buscarPorOid(oid);
        usuario.setDireccion(direccion);
        usuario.setComuna(comuna);
        usuario.setRegion(region);
        usuario.setTelefono(telefono);
        return usuarioRepository.save(usuario);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private RolUsuario resolverRolUsuario(String oid) {
        return adminOid.equalsIgnoreCase(oid) ? RolUsuario.ADMIN : RolUsuario.USER;
    }
}
