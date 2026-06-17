package cl.duoc.sanosysalvos.ms_usuarios.controllers;

import cl.duoc.sanosysalvos.ms_usuarios.models.Usuario;
import cl.duoc.sanosysalvos.ms_usuarios.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {
        // Buscamos si ya existe el usuario base para no duplicarlo cada vez que inicias
        if (usuarioService.listarTodos().isEmpty()) {

            // 1. Crear Vecino de prueba
            // FIX: Se agrega RUT válido. Sin él, Hibernate Validator lanza
            //      ConstraintViolationException en repository.save() y el
            //      contenedor falla al arrancar.
            Usuario vecino = new Usuario();
            vecino.setNombre("Julian Vecino");
            vecino.setRut("11.111.111-1");
            vecino.setEmail("vecino@vecino.cl");
            vecino.setPassword("password123");
            vecino.setRol("VECINO");
            usuarioService.registrar(vecino);

            // 2. Crear Veterinario de prueba
            Usuario veterinario = new Usuario();
            veterinario.setNombre("Dr. Claudio Fuentes");
            veterinario.setRut("12.345.678-5");
            veterinario.setEmail("claudio@veterinaria.cl");
            veterinario.setPassword("password123");
            veterinario.setRol("VETERINARIO");
            usuarioService.registrar(veterinario);

            // 3. Crear Municipalidad de prueba
            Usuario muni = new Usuario();
            muni.setNombre("Central Maipú");
            muni.setRut("9.876.543-3");
            muni.setEmail("admin@maipu.cl");
            muni.setPassword("password123");
            muni.setRol("MUNICIPALIDAD");
            usuarioService.registrar(muni);

            System.out.println("======> ¡DATOS DE PRUEBA INYECTADOS CON ÉXITO EN POSTGRES! <======");
            System.out.println("  VECINO       → vecino@vecino.cl      / password123");
            System.out.println("  VETERINARIO  → claudio@veterinaria.cl / password123");
            System.out.println("  MUNICIPALIDAD → admin@maipu.cl        / password123");
        }
    }
}
