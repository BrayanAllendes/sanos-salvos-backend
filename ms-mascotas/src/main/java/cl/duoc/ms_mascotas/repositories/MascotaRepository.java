package cl.duoc.ms_mascotas.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import cl.duoc.ms_mascotas.models.Mascota;
import java.util.List;

@Repository
public interface MascotaRepository extends MongoRepository<Mascota, String> {
    
    // Este método te servirá para el filtro del mapa en el Front
    // Así puedes buscar solo "PERDIDOS" o "ENCONTRADOS"
    List<Mascota> findByEstado(String estado);
}