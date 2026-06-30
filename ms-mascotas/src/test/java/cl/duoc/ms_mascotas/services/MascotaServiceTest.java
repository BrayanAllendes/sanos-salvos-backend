package cl.duoc.ms_mascotas.services;

import cl.duoc.ms_mascotas.clients.UsuarioClient;
import cl.duoc.ms_mascotas.models.Mascota;
import cl.duoc.ms_mascotas.repositories.MascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository repository; // Fingimos MongoDB

    @Mock
    private UsuarioClient usuarioClient; // Fingimos la conexión a ms-usuarios

    @Mock
    private RabbitTemplate rabbitTemplate; // Fingimos el envío de colas

    @InjectMocks
    private MascotaService service;

    private Mascota mascotaBase;
    private Mascota mascotaCercana;
    private Mascota mascotaLejana;

    @BeforeEach
    void setUp() {
        // Arrange: Preparamos a la mascota principal (Ej: Perdida en el centro de Maipú)
        mascotaBase = new Mascota();
        mascotaBase.setId("M1");
        mascotaBase.setNombre("Firulais");
        mascotaBase.setTipo("Perro");
        mascotaBase.setEstado("Sano");
        mascotaBase.setIdDueno(100L);
        mascotaBase.setLatitud(-33.5100);
        mascotaBase.setLongitud(-70.7500);

        // Mascota a ~2 KM (Debería hacer Match)
        mascotaCercana = new Mascota();
        mascotaCercana.setId("M2");
        mascotaCercana.setTipo("Perro"); // Mismo tipo
        mascotaCercana.setLatitud(-33.5200);
        mascotaCercana.setLongitud(-70.7600);

        // Mascota a +60 KM (No debería hacer Match)
        mascotaLejana = new Mascota();
        mascotaLejana.setId("M3");
        mascotaLejana.setTipo("Perro");
        mascotaLejana.setLatitud(-34.0000);
        mascotaLejana.setLongitud(-71.0000);
    }

    @Test
    void testRegistrarMascota_Exitoso() {
        // Arrange
        when(repository.save(mascotaBase)).thenReturn(mascotaBase);

        // Act
        Mascota resultado = service.registrarMascota(mascotaBase);

        // Assert
        assertNotNull(resultado);
        verify(usuarioClient, times(1)).validarDueno(100L);
        verify(repository, times(1)).save(mascotaBase);
        // Validamos que se disparó el evento a RabbitMQ
        verify(rabbitTemplate, times(1)).convertAndSend(eq(MascotaService.EXCHANGE_NAME), eq(MascotaService.KEY_NUEVA), anyString());
    }

    @Test
    void testFallbackValidar() {
        // Arrange
        Throwable errorSimulado = new RuntimeException("500 Internal Server Error");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.fallbackValidar(mascotaBase, errorSimulado);
        });

        assertEquals("No se pudo validar al dueño. El servicio de usuarios no responde.", exception.getMessage());
    }

    @Test
    void testActualizarMascota_Silenciosa() {
        // Arrange (Actualización normal, no perdida)
        when(repository.save(mascotaBase)).thenReturn(mascotaBase);

        // Act
        Mascota resultado = service.actualizarMascota(mascotaBase);

        // Assert
        assertEquals("Sano", resultado.getEstado());
        verify(repository, times(1)).save(mascotaBase);
        // Validamos que NO se mandó nada a RabbitMQ
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void testActualizarMascota_GeneraAlertaBuscado() {
        // Arrange (Mascota reportada como Buscada)
        mascotaBase.setEstado(MascotaService.ESTADO_BUSCADO);
        mascotaBase.setSector("Plaza Maipú");
        when(repository.save(mascotaBase)).thenReturn(mascotaBase);

        // Act
        Mascota resultado = service.actualizarMascota(mascotaBase);

        // Assert
        assertEquals(MascotaService.ESTADO_BUSCADO, resultado.getEstado());
        // Verificamos que SÍ disparó la alerta roja por RabbitMQ
        verify(rabbitTemplate, times(1)).convertAndSend(eq(MascotaService.EXCHANGE_NAME), eq(MascotaService.KEY_BUSQUEDA), anyString());
    }

    // ==========================================================
    // PRUEBAS DEL MOTOR DE COINCIDENCIAS (GPS HAVERSINE)
    // ==========================================================

    @Test
    void testBuscarCoincidencias_EncuentraMatchCorrecto() {
        // Arrange
        // Simulamos una base de datos con: la misma mascota, una cerca, una lejos, una sin coordenadas, y un Gato.
        Mascota mascotaGato = new Mascota();
        mascotaGato.setId("M4");
        mascotaGato.setTipo("Gato");

        Mascota mascotaSinGPS = new Mascota();
        mascotaSinGPS.setId("M5");
        mascotaSinGPS.setTipo("Perro");

        List<Mascota> baseDeDatos = Arrays.asList(mascotaBase, mascotaCercana, mascotaLejana, mascotaGato, mascotaSinGPS);
        
        when(repository.findById("M1")).thenReturn(Optional.of(mascotaBase));
        when(repository.findAll()).thenReturn(baseDeDatos);

        // Act
        List<Mascota> coincidencias = service.buscarCoincidencias("M1");

        // Assert
        assertNotNull(coincidencias);
        assertEquals(1, coincidencias.size()); // Solo DEBE encontrar a 'mascotaCercana'
        assertEquals("M2", coincidencias.get(0).getId());
    }

    @Test
    void testBuscarCoincidencias_LanzaExceptionSiNoExiste() {
        // Arrange
        when(repository.findById("Fantasma")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.buscarCoincidencias("Fantasma");
        });
        assertEquals("Mascota no encontrada", exception.getMessage());
    }

    // ==========================================================
    // MÉTODOS SIMPLES DE BÚSQUEDA
    // ==========================================================

    @Test
    void testListarTodas() {
        when(repository.findAll()).thenReturn(Arrays.asList(mascotaBase));
        List<Mascota> resultado = service.listarTodas();
        assertEquals(1, resultado.size());
    }

    @Test
    void testBuscarPorId() {
        when(repository.findById("M1")).thenReturn(Optional.of(mascotaBase));
        assertTrue(service.buscarPorId("M1").isPresent());
    }

    @Test
    void testBuscarPorChip() {
        when(repository.findByChip("CHIP123")).thenReturn(Optional.of(mascotaBase));
        assertTrue(service.buscarPorChip("CHIP123").isPresent());
    }

    @Test
    void testBuscarPorEstado() {
        when(repository.findByEstado("Sano")).thenReturn(Arrays.asList(mascotaBase));
        assertEquals(1, service.buscarPorEstado("Sano").size());
    }

    @Test
    void testObtenerMascotaPorId_TiraUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            service.obtenerMascotaPorId("M1");
        });
    }
}