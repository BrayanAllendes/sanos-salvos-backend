package cl.duoc.ms_mascotas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-usuarios")
public interface UsuarioClient {
    // Cambiamos validarDueño -> validarDueno
    @GetMapping("/api/usuarios/{id}")
    Object validarDueno(@PathVariable("id") Long id); 
}