package cl.duoc.sanosysalvos.ms_notificaciones.listeners;
 
import cl.duoc.sanosysalvos.ms_notificaciones.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 
@Component
public class NotificacionListener {
 
    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
 
    /**
     * Escucha todos los mensajes del queue "notificaciones.queue".
     * Gracias al binding con wildcard "mascotas.#", recibe tanto los eventos
     * de nueva mascota (mascotas.nueva) como las alertas de pérdida
     * (mascotas.busqueda) sin necesidad de múltiples listeners.
     *
     * En producción, aquí se integraría JavaMailSender (SMTP), Twilio (SMS)
     * o Firebase (push). Para la entrega universitaria, se simula el envío
     * logueando en consola de forma descriptiva.
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void recibirMensaje(String mensaje) {
        String timestamp = LocalDateTime.now().format(FORMATO);
 
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          📧  SISTEMA DE NOTIFICACIONES — SANOS Y SALVOS    ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  ⏰ Recibido : %s                            ║%n", timestamp);
        System.out.printf( "║  📨 Evento   : %s%n", mensaje);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  [SIMULADO] ✅ Email enviado al dueño registrado.            ║");
        System.out.println("║  [SIMULADO] ✅ Alerta SMS enviada a Municipalidad de Maipú.  ║");
        System.out.println("║  [SIMULADO] ✅ Notificación push enviada a vecinos del sector.║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
}