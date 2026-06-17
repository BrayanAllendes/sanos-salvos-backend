package cl.duoc.sanosysalvos.ms_notificaciones.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.sanosysalvos.ms_notificaciones.listeners.NotificacionListener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class NotificacionListenerTest {

    @InjectMocks
    private NotificacionListener notificacionListener;

    @Test
    void testRecibirMensajeNuevaMascota() {
        String mensajePrueba = "{\"id\":\"1\", \"nombre\":\"Bobby\", \"estado\":\"Registrado\"}";
        
        // Verifica que el método no lance excepciones al recibir un mensaje
        // Ajusta el nombre del método si el tuyo no se llama recibirMensaje
        assertDoesNotThrow(() -> notificacionListener.recibirMensaje(mensajePrueba)); 
    }

    @Test
    void testRecibirMensajeMascotaPerdida() {
        String mensajeAlerta = "{\"id\":\"2\", \"nombre\":\"Luna\", \"estado\":\"Buscado 🚨\"}";
        
        assertDoesNotThrow(() -> notificacionListener.recibirMensaje(mensajeAlerta));
    }
}