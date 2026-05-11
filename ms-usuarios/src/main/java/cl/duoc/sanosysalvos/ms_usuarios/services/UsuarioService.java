package cl.duoc.sanosysalvos.ms_usuarios.services;

import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import cl.duoc.sanosysalvos.ms_usuarios.repositories.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyectamos el repositorio y el encriptador
    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // REGISTRO DE USUARIO
    public Usuario registrar(Usuario usuario) {
        // 1. Verificar si el email ya existe
        if (repository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2. Encriptar la contraseña antes de guardar
        String passwordHashed = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordHashed);

        // 3. Guardar en PostgreSQL
        return repository.save(usuario);
    }

    // LISTAR TODOS LOS USUARIOS (Este es el que faltaba)
    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    // BUSCAR POR EMAIL (Para el Login)
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    // VALIDAR CREDENCIALES (Para el Login)
    public boolean verificarPassword(String passwordPlana, String passwordHashed) {
        return passwordEncoder.matches(passwordPlana, passwordHashed);
    }
}