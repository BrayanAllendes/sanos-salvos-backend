package cl.duoc.ms_mascotas.controllers;

import cl.duoc.ms_mascotas.models.Mascota;
import cl.duoc.ms_mascotas.services.MascotaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MascotaController.class)
@AutoConfigureMockMvc(addFilters = false)
class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MascotaService service;

    private Mascota mascotaPrueba;

    @BeforeEach
    void setUp() {
        mascotaPrueba = new Mascota();
        mascotaPrueba.setId("M1");
        mascotaPrueba.setNombre("Firulais");
    }

    @Test
    void testCrearMascota_Exitoso() throws Exception {
        when(service.registrarMascota(any(Mascota.class))).thenReturn(mascotaPrueba);

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mascotaPrueba)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Firulais"));
    }

    @Test
    void testListarTodas() throws Exception {
        when(service.listarTodas()).thenReturn(Arrays.asList(mascotaPrueba));

        mockMvc.perform(get("/api/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId("M1")).thenReturn(Optional.of(mascotaPrueba));

        mockMvc.perform(get("/api/mascotas/M1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("M1"));
    }

    @Test
    void testActualizarMascota() throws Exception {
        when(service.buscarPorId("M1")).thenReturn(Optional.of(mascotaPrueba));
        when(service.actualizarMascota(any(Mascota.class))).thenReturn(mascotaPrueba);

        mockMvc.perform(put("/api/mascotas/M1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mascotaPrueba)))
                .andExpect(status().isOk());
    }

    @Test
    void testDespacharMascota() throws Exception {
        when(service.buscarPorId("M1")).thenReturn(Optional.of(mascotaPrueba));
        when(service.actualizarMascota(any(Mascota.class))).thenReturn(mascotaPrueba);

        mockMvc.perform(put("/api/mascotas/M1/despachar"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerCoincidencias() throws Exception {
        when(service.buscarCoincidencias("M1")).thenReturn(Arrays.asList(mascotaPrueba));

        mockMvc.perform(get("/api/mascotas/M1/coincidencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
}