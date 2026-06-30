package cl.duoc.sanosysalvos.ms_usuarios.controllers;

import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import cl.duoc.sanosysalvos.ms_usuarios.security.JwtUtil;
import cl.duoc.sanosysalvos.ms_usuarios.services.UsuarioService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false) // APAGAMOS LA SEGURIDAD SOLO PARA EL TEST
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc; // Nuestro Postman de mentira

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    @MockBean
    private UsuarioService service; // Fingimos el servicio

    @MockBean
    private JwtUtil jwtUtil; // Fingimos el creador de tokens

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setRut("19123456-9"); 
        usuarioPrueba.setNombre("Brayan");
        usuarioPrueba.setEmail("vecino@test.cl");
        usuarioPrueba.setPassword("hashed123");
        usuarioPrueba.setRol("VECINO");
    }

    @Test
    void testListar() throws Exception {
        // Arrange
        when(service.listarTodos()).thenReturn(Arrays.asList(usuarioPrueba));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Brayan"));
    }

    @Test
    void testBuscarPorId_Existe() throws Exception {
        // Arrange
        when(service.buscarPorIdLong(1L)).thenReturn(Optional.of(usuarioPrueba));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("vecino@test.cl"));
    }

    @Test
    void testBuscarPorId_NoExiste() throws Exception {
        // Arrange
        when(service.buscarPorIdLong(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuario no encontrado"));
    }

    @Test
    void testRegistrar_Exitoso() throws Exception {
        // Arrange
        when(service.registrar(any(Usuario.class))).thenReturn(usuarioPrueba);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioPrueba)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Brayan"));
    }

    @Test
    void testRegistrar_Falla() throws Exception {
        // Arrange
        when(service.registrar(any(Usuario.class))).thenThrow(new RuntimeException("El correo ya está registrado"));

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioPrueba)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El correo ya está registrado"));
    }

    @Test
    void testLogin_Exitoso() throws Exception {
        // Arrange
        Map<String, String> credenciales = new HashMap<>();
        credenciales.put("email", "vecino@test.cl");
        credenciales.put("password", "plana123");

        when(service.buscarPorEmail("vecino@test.cl")).thenReturn(Optional.of(usuarioPrueba));
        when(service.verificarPassword("plana123", "hashed123")).thenReturn(true);
        when(jwtUtil.generarToken(anyString(), anyString(), any(Long.class))).thenReturn("token-falso-123");

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-falso-123"))
                .andExpect(jsonPath("$.role").value("VECINO"))
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void testLogin_ContrasenaIncorrecta() throws Exception {
        // Arrange
        Map<String, String> credenciales = new HashMap<>();
        credenciales.put("email", "vecino@test.cl");
        credenciales.put("password", "mala123");

        when(service.buscarPorEmail("vecino@test.cl")).thenReturn(Optional.of(usuarioPrueba));
        when(service.verificarPassword("mala123", "hashed123")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Contraseña incorrecta"));
    }

    @Test
    void testLogin_UsuarioNoEncontrado() throws Exception {
        // Arrange
        Map<String, String> credenciales = new HashMap<>();
        credenciales.put("email", "fantasma@test.cl");
        credenciales.put("password", "123456");

        when(service.buscarPorEmail("fantasma@test.cl")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuario no encontrado"));
    }
}