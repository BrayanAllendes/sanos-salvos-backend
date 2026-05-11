package cl.duoc.ms_mascotas.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import cl.duoc.ms_mascotas.repositories.MascotaRepository;
import cl.duoc.ms_mascotas.models.Mascota;
import cl.duoc.ms_mascotas.clients.UsuarioClient;
import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    private final MascotaRepository repository;
    private final UsuarioClient usuarioClient;
    private final RabbitTemplate rabbitTemplate;

    // Constructor manual para asegurar la inyección (sin Lombok)
    public MascotaService(MascotaRepository repository, UsuarioClient usuarioClient, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.usuarioClient = usuarioClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @CircuitBreaker(name = "ms-usuarios", fallbackMethod = "fallbackValidar")
    public Mascota registrarMascota(Mascota mascota) {
        // Validamos al dueño llamando al microservicio de usuarios
        usuarioClient.validarDueno(mascota.getIdDueno()); 
        
        // Si la validación pasa, guardamos en MongoDB
        Mascota guardada = repository.save(mascota);
        
        // Notificamos a RabbitMQ
        rabbitTemplate.convertAndSend("mascotas.exchange", "mascotas.routing.key", 
            "Nueva mascota: " + guardada.getNombre());

        return guardada;
    }

    // El fallback para el Circuit Breaker
    public Mascota fallbackValidar(Mascota mascota, Throwable t) {
        System.err.println("Error en validación: " + t.getMessage());
        throw new RuntimeException("No se pudo validar al dueño. El servicio de usuarios no responde.");
    }

    // --- AQUÍ ESTABA LO QUE FALTABA PARA QUE EL CONTROLADOR NO ESTUVIERA EN ROJO ---

    public List<Mascota> listarTodas() {
        return repository.findAll();
    }

    public Optional<Mascota> buscarPorId(String id) {
        return repository.findById(id);
    }
}