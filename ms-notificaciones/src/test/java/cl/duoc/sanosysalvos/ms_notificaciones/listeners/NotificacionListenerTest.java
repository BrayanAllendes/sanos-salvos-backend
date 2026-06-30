package cl.duoc.sanosysalvos.ms_notificaciones.listeners;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificacionListenerTest {

    private NotificacionListener listener;
    
    // Herramientas para "secuestrar" la consola y leer lo que imprime el método
    private final ByteArrayOutputStream capturadorConsola = new ByteArrayOutputStream();
    private final PrintStream consolaOriginal = System.out;

    @BeforeEach
    void setUp() {
        listener = new NotificacionListener();
        // Redirigimos System.out a nuestro capturador antes de cada prueba
        System.setOut(new PrintStream(capturadorConsola));
    }

    @AfterEach
    void tearDown() {
        // Restauramos la consola a la normalidad para no romper otras pruebas
        System.setOut(consolaOriginal);
    }

    @Test
    void testRecibirMensaje() {
        // Arrange: Simulamos que RabbitMQ nos manda un mensaje de alerta
        String mensajePrueba = "🚨 ALERTA: Mascota 'Firulais' perdida en el sector.";

        // Act: Llamamos al listener directamente
        listener.recibirMensaje(mensajePrueba);

        // Assert: Convertimos todo lo que se imprimió a un String y lo verificamos
        String salida = capturadorConsola.toString();

        // Comprobamos que el diseño y el mensaje exacto estén ahí
        assertTrue(salida.contains("SISTEMA DE NOTIFICACIONES — SANOS Y SALVOS"));
        assertTrue(salida.contains(mensajePrueba));
        assertTrue(salida.contains("[SIMULADO] ✅ Email enviado al dueño registrado."));
        assertTrue(salida.contains("Municipalidad de Maipú"));
    }
}