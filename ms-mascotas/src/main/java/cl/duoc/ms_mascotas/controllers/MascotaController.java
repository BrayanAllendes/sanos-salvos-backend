package cl.duoc.ms_mascotas.controllers;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cl.duoc.ms_mascotas.models.Mascota;
import cl.duoc.ms_mascotas.services.MascotaService;
import java.util.List;
 
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {
 
    private final MascotaService service;
 
    public MascotaController(MascotaService service) {
        this.service = service;
    }
 
    // ── Endpoints generales (accesibles por todos los roles autenticados) ──────
 
    @PostMapping
    public ResponseEntity<?> crearMascota(@RequestBody Mascota mascota) {
        try {
            return ResponseEntity.ok(service.registrarMascota(mascota));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
 
    @GetMapping
    public List<Mascota> listarTodas() {
        return service.listarTodas();
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @GetMapping("/chip/{chip}")
    public ResponseEntity<Mascota> buscarPorChip(@PathVariable String chip) {
        return service.buscarPorChip(chip)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @GetMapping("/estado/{estado}")
    public List<Mascota> buscarPorEstado(@PathVariable String estado) {
        return service.buscarPorEstado(estado);
    }
 
    /**
     * Actualización general: VECINO reporta mascota perdida (estado = "Buscado 🚨")
     * o VETERINARIO carga datos clínicos.
     * El Gateway NO restringe este endpoint por rol.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(@PathVariable String id,
                                               @RequestBody Mascota mascota) {
        return service.buscarPorId(id).map(m -> {
            mascota.setId(id);
            return ResponseEntity.ok(service.actualizarMascota(mascota));
        }).orElse(ResponseEntity.notFound().build());
    }
 
    /**
     * FIX: Endpoint exclusivo para MUNICIPALIDAD → despachar patrulla.
     *
     * Al separar la acción en su propia URL (/despachar), el API Gateway puede
     * aplicar la regla de rol de forma declarativa, sin analizar el cuerpo del
     * request ni el estado de la mascota. Cualquier intento de un VECINO o
     * VETERINARIO de llamar a esta ruta recibirá un 403 Forbidden desde el Gateway,
     * antes de que el request llegue siquiera a este microservicio.
     *
     * El frontend (MunicipioView) debe llamar a PUT /api/mascotas/{id}/despachar
     * en lugar del PUT /api/mascotas/{id} genérico.
     */
    @PutMapping("/{id}/despachar")
    public ResponseEntity<?> despacharMascota(@PathVariable String id) {
        return service.buscarPorId(id).map(m -> {
            m.setDespachado(true);
            return ResponseEntity.ok(service.actualizarMascota(m));
        }).orElse(ResponseEntity.notFound().build());
    }
    // =================================================================
    // 🚀 ENDPOINT DEL MOTOR DE COINCIDENCIAS
    // =================================================================
    @GetMapping("/{id}/coincidencias")
    public ResponseEntity<?> obtenerCoincidencias(@PathVariable String id) {
        try {
            List<Mascota> coincidencias = service.buscarCoincidencias(id);
            return ResponseEntity.ok(coincidencias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}