package cl.duoc.sanosysalvos.ms_usuarios.services;

import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import cl.duoc.sanosysalvos.ms_usuarios.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository; // Fingimos la base de datos PostgreSQL

    @InjectMocks
    private UsuarioService service; // Inyectamos el mock al servicio real

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        // Arrange general: Preparamos un usuario falso antes de cada test
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombre("Brayan");
        usuarioPrueba.setEmail("brayan@test.cl");
        usuarioPrueba.setPassword("123456"); // Contraseña plana
        usuarioPrueba.setRol("VECINO");
    }

    @Test
    void testListarTodos() {
        // Arrange
        List<Usuario> listaFalsa = Arrays.asList(usuarioPrueba);
        when(repository.findAll()).thenReturn(listaFalsa);

        // Act
        List<Usuario> resultado = service.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Brayan", resultado.get(0).getNombre());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testRegistrar_Exitoso() {
        // Arrange
        when(repository.findByEmail(usuarioPrueba.getEmail())).thenReturn(Optional.empty()); // No existe
        when(repository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        // Act
        Usuario resultado = service.registrar(usuarioPrueba);

        // Assert
        assertNotNull(resultado);
        // Validamos que el servicio haya cambiado la clave plana por un Hash encriptado
        assertNotEquals("123456", resultado.getPassword());
        verify(repository, times(1)).findByEmail("brayan@test.cl");
        verify(repository, times(1)).save(usuarioPrueba);
    }

    @Test
    void testRegistrar_FallaPorqueEmailYaExiste() {
        // Arrange
        when(repository.findByEmail(usuarioPrueba.getEmail())).thenReturn(Optional.of(usuarioPrueba)); // Ya existe

        // Act & Assert
        // Verificamos que explote con RuntimeException tal como lo programaste
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.registrar(usuarioPrueba);
        });

        assertEquals("El correo ya está registrado", exception.getMessage());
        // Verificamos que NUNCA se haya llamado al método guardar
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void testBuscarPorEmail() {
        // Arrange
        when(repository.findByEmail(usuarioPrueba.getEmail())).thenReturn(Optional.of(usuarioPrueba));

        // Act
        Optional<Usuario> resultado = service.buscarPorEmail(usuarioPrueba.getEmail());

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("brayan@test.cl", resultado.get().getEmail());
    }

    @Test
    void testBuscarPorIdLong() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));

        // Act
        Optional<Usuario> resultado = service.buscarPorIdLong(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void testVerificarPassword() {
        // Arrange
        String passwordPlana = "secreta123";
        // Generamos un hash real manual para comparar
        String passwordHashed = new BCryptPasswordEncoder().encode(passwordPlana);

        // Act
        boolean coincide = service.verificarPassword(passwordPlana, passwordHashed);

        // Assert
        assertTrue(coincide);
    }
}