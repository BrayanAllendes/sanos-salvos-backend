package cl.duoc.sanosysalvos.ms_usuarios.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor // Genera constructor con todos los campos
@NoArgsConstructor  // Genera constructor vacío (Obligatorio para JPA)
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Column(unique = true)
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;

    @Column(unique = true)
    @Email(message = "Debe ingresar un email válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") // Validación de seguridad extra
    private String password; 

    @NotBlank(message = "El rol es obligatorio")
    private String rol; // ADMIN, USER, VET
}