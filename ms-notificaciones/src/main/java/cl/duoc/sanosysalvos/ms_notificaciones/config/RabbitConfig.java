package cl.duoc.sanosysalvos.ms_notificaciones.config;
 
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
@Configuration
public class RabbitConfig {
 
    public static final String QUEUE_NAME    = "notificaciones.queue";
    public static final String EXCHANGE_NAME = "mascotas.exchange";
 
    /**
     * FIX 1 — Routing key cambiada a wildcard "mascotas.#"
     *
     * Antes: "mascotas.routing.key" → binding literal, solo capturaba UNA key.
     * Ahora: "mascotas.#" → captura cualquier mensaje cuya routing key
     * empiece con "mascotas." (mascotas.nueva, mascotas.busqueda, y cualquier
     * evento futuro que agreguemos sin tocar este archivo).
     *
     * Así es como se aprovecha un TopicExchange correctamente.
     */
    public static final String ROUTING_KEY = "mascotas.#";
 
    /**
     * FIX 2 — Queue declarada como durable=true
     *
     * Antes era non-durable: si RabbitMQ se reiniciaba (o el container se
     * recreaba), la queue desaparecía y se perdían los mensajes en tránsito.
     * Con durable=true, la queue sobrevive reinicios del broker.
     *
     * FIX 3 — Declaración del exchange con durable=true
     *
     * El ms-mascotas ya declara el exchange como durable(true). Si este
     * servicio lo redeclaraba como non-durable, RabbitMQ lanzaba un error de
     * "inequivalent arg". Ahora ambas declaraciones son consistentes.
     */
    @Bean
    public Queue notificacionesQueue() {
        return new Queue(QUEUE_NAME, true);
    }
 
    @Bean
    public TopicExchange mascotasExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
 
    @Bean
    public Binding binding(Queue notificacionesQueue, TopicExchange mascotasExchange) {
        return BindingBuilder
                .bind(notificacionesQueue)
                .to(mascotasExchange)
                .with(ROUTING_KEY);
    }
}