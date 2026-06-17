package cl.duoc.sanosysalvos.ms_usuarios.Services;

import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import cl.duoc.sanosysalvos.ms_usuarios.repositories.UsuarioRepository;
import cl.duoc.sanosysalvos.ms_usuarios.services.UsuarioService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void testRegistrarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("vecino@vecino.cl");
        usuario.setPassword("123456");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.registrar(usuario); // Ajusta el método si es diferente

        assertNotNull(resultado);
        assertEquals("vecino@vecino.cl", resultado.getEmail());
    }

    @Test
    void testBuscarUsuarioPorEmail() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@maipu.cl");
        when(usuarioRepository.findByEmail("admin@maipu.cl")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("admin@maipu.cl"); // Ajusta el método

        assertTrue(resultado.isPresent());
        assertEquals("admin@maipu.cl", resultado.get().getEmail());
    }
}