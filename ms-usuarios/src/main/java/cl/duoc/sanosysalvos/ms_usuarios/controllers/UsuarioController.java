package cl.duoc.sanosysalvos.ms_usuarios.controllers;
 
import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import cl.duoc.sanosysalvos.ms_usuarios.services.UsuarioService;
import cl.duoc.sanosysalvos.ms_usuarios.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
 
    @Autowired
    private UsuarioService service;
 
    @Autowired
    private JwtUtil jwtUtil;
 
    @GetMapping
    public List<Usuario> listar() {
        return service.listarTodos();
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        java.util.Optional<Usuario> usuarioOpt = service.buscarPorIdLong(id);
        
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(usuarioOpt.get());
        } else {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
    }
 
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario nuevo = service.registrar(usuario);
            return ResponseEntity.status(201).body(nuevo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email    = credentials.get("email");
        String password = credentials.get("password");
 
        return service.buscarPorEmail(email)
            .map(user -> {
                if (service.verificarPassword(password, user.getPassword())) {
                    // FIX: Pasamos rol e id para que queden embebidos en el JWT.
                    // El API Gateway los leerá en cada request protegido sin
                    // tener que consultar ms-usuarios de nuevo.
                    String token = jwtUtil.generarToken(email, user.getRol(), user.getId());
                    return ResponseEntity.ok(Map.of(
                        "token", token,
                        "role",  user.getRol(),
                        "id",    String.valueOf(user.getId())
                    ));
                }
                return ResponseEntity.status(401).body("Contraseña incorrecta");
            })
            .orElse(ResponseEntity.status(404).body("Usuario no encontrado"));
    }
}