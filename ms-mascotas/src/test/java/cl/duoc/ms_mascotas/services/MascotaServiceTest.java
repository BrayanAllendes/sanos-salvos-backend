package cl.duoc.ms_mascotas.services;

import cl.duoc.ms_mascotas.models.Mascota;
import cl.duoc.ms_mascotas.repositories.MascotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @InjectMocks
    private MascotaService mascotaService;

    @Test
    void testGuardarMascota() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Bobby");
        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mascota);

        Mascota resultado = mascotaService.actualizarMascota(mascota); // Ajusta el nombre del método si es diferente

        assertNotNull(resultado);
        assertEquals("Bobby", resultado.getNombre());
        verify(mascotaRepository, times(1)).save(mascota);
    }

    @Test
    void testBuscarMascotaPorId() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Luna");
        when(mascotaRepository.findById("1")).thenReturn(Optional.of(mascota));

        Optional<Mascota> resultado = mascotaService.buscarPorId("1"); // Ajusta el nombre del método

        assertTrue(resultado.isPresent());
        assertEquals("Luna", resultado.get().getNombre());
    }

    @Test
    void testListarMascotas() {
        when(mascotaRepository.findAll()).thenReturn(Arrays.asList(new Mascota(), new Mascota()));

        List<Mascota> resultado = mascotaService.listarTodas(); // Ajusta el nombre del método

        assertEquals(2, resultado.size());
        verify(mascotaRepository, times(1)).findAll();
    }
}