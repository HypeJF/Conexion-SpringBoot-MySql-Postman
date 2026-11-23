package com.taller.taller_empleados.controller;

import com.taller.taller_empleados.entity.Rol;
import com.taller.taller_empleados.entity.Usuario;
import com.taller.taller_empleados.repository.RolRepository;
import com.taller.taller_empleados.repository.UsuarioRepository;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class RegistroUsuarioRequest {
    public String username;
    public String password;
    public String rol;
}

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody RegistroUsuarioRequest request) {
        if (request == null || request.username == null || request.password == null || request.rol == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debes enviar username, password y rol"));
        }

        if (usuarioRepository.findByUsername(request.username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El username ya existe"));
        }

        Rol rolEntidad = rolRepository.findByNombre(request.rol)
                .orElse(null);

        if (rolEntidad == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rol no encontrado: " + request.rol));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.username);
        nuevoUsuario.setPassword(passwordEncoder.encode(request.password));
        nuevoUsuario.setEnabled(true);
        nuevoUsuario.setRoles(Set.of(rolEntidad));

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", guardado.getId(),
                "username", guardado.getUsername(),
                "rol", rolEntidad.getNombre()
        ));
    }

}
