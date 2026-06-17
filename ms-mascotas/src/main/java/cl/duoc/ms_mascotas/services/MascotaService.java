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

    // Constantes centralizadas para el exchange y las routing keys.
    // Al usar TopicExchange, cada tipo de evento tiene su propia key semántica.
    // ms-notificaciones usará el wildcard "mascotas.#" para recibirlas todas.
    public static final String EXCHANGE_NAME  = "mascotas.exchange";
    public static final String KEY_NUEVA      = "mascotas.nueva";     // POST
    public static final String KEY_BUSQUEDA   = "mascotas.busqueda";  // PUT → estado perdido
    public static final String ESTADO_BUSCADO = "Buscado 🚨";

    private final MascotaRepository repository;
    private final UsuarioClient usuarioClient;
    private final RabbitTemplate rabbitTemplate;

    public MascotaService(MascotaRepository repository,
                          UsuarioClient usuarioClient,
                          RabbitTemplate rabbitTemplate) {
        this.repository    = repository;
        this.usuarioClient = usuarioClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @CircuitBreaker(name = "ms-usuarios", fallbackMethod = "fallbackValidar")
    public Mascota registrarMascota(Mascota mascota) {
        // 1. Validar que el dueño exista en ms-usuarios (con Circuit Breaker)
        usuarioClient.validarDueno(mascota.getIdDueno());

        // 2. Persistir en MongoDB
        Mascota guardada = repository.save(mascota);

        // 3. Publicar evento de nueva mascota (routing key: mascotas.nueva)
        String mensaje = String.format(
            "🐾 NUEVA MASCOTA: '%s' (%s) registrada para el dueño ID %d.",
            guardada.getNombre(),
            guardada.getTipo(),
            guardada.getIdDueno()
        );
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, KEY_NUEVA, mensaje);

        return guardada;
    }

    public Mascota fallbackValidar(Mascota mascota, Throwable t) {
        System.err.println("Circuit Breaker activado para ms-usuarios: " + t.getMessage());
        throw new RuntimeException("No se pudo validar al dueño. El servicio de usuarios no responde.");
    }

    public List<Mascota> listarTodas() {
        return repository.findAll();
    }

    public Optional<Mascota> buscarPorId(String id) {
        return repository.findById(id);
    }

    public Optional<Mascota> buscarPorChip(String chip) {
        return repository.findByChip(chip);
    }

    public List<Mascota> buscarPorEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public Mascota actualizarMascota(Mascota mascota) {
        Mascota actualizada = repository.save(mascota);

        // FIX PRINCIPAL: Disparar evento RabbitMQ cuando la mascota es
        // reportada como perdida. Antes, el PUT era completamente silencioso.
        // Ahora, el cambio de estado a "Buscado 🚨" genera una alerta que
        // ms-notificaciones recibirá por la routing key "mascotas.busqueda".
        if (ESTADO_BUSCADO.equals(actualizada.getEstado())) {
            String sector = actualizada.getSector() != null
                            ? actualizada.getSector()
                            : "No especificado";

            String alerta = String.format(
                "🚨 ALERTA DE BÚSQUEDA: La mascota '%s' (%s) fue reportada como PERDIDA. " +
                "Sector: %s. ID Dueño: %d.",
                actualizada.getNombre(),
                actualizada.getTipo(),
                sector,
                actualizada.getIdDueno()
            );
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, KEY_BUSQUEDA, alerta);
        }

        return actualizada;
    }

        // =================================================================
        // 🚀 MOTOR DE COINCIDENCIAS (ALGORITMO DE GEOLOCALIZACIÓN Y RASGOS)
        // =================================================================

    public List<Mascota> buscarCoincidencias(String idMascotaPerdida) {
        Mascota mascotaPerdida = repository.findById(idMascotaPerdida)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        List<Mascota> baseDeDatos = repository.findAll();

        return baseDeDatos.stream()
                .filter(m -> !m.getId().equals(mascotaPerdida.getId()))
                .filter(m -> m.getTipo() != null && m.getTipo().equalsIgnoreCase(mascotaPerdida.getTipo()))
                .filter(m -> {
                    if (m.getLatitud() == null || m.getLongitud() == null || 
                        mascotaPerdida.getLatitud() == null || mascotaPerdida.getLongitud() == null) {
                        return false; 
                    }
                    double distanciaKm = calcularDistancia(
                        mascotaPerdida.getLatitud(), mascotaPerdida.getLongitud(),
                        m.getLatitud(), m.getLongitud()
                    );
                    return distanciaKm <= 5.0; // Radio: 5 KM
                })
                .toList();
    }

    private double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; 
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }

    public Optional<Mascota> obtenerMascotaPorId(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerMascotaPorId'");
    }
}

