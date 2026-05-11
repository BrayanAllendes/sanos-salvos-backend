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

    // Usar constructor en vez de @Autowired quita la línea amarilla
    public MascotaController(MascotaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> crearMascota(@RequestBody Mascota mascota) {
        try {
            Mascota nuevaMascota = service.registrarMascota(mascota);
            return ResponseEntity.ok(nuevaMascota);
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
}