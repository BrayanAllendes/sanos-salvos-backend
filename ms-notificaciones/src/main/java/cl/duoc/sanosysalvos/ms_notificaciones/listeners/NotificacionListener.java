package cl.duoc.sanosysalvos.ms_notificaciones.listeners;

import cl.duoc.sanosysalvos.ms_notificaciones.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionListener {

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void recibirMensaje(String mensaje) {
        System.out.println("========================================");
        System.out.println("ALERTA DE NOTIFICACIÓN: " + mensaje);
        System.out.println("========================================");
    }
}