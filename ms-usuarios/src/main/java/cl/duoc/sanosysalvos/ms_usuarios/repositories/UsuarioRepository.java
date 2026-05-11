package cl.duoc.sanosysalvos.ms_usuarios.repositories;

import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Esto nos servirá para el Login después
    Optional<Usuario> findByEmail(String email);
}