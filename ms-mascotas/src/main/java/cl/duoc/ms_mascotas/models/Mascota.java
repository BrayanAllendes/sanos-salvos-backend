package cl.duoc.ms_mascotas.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mascotas")
public class Mascota {
    @Id
    private String id;
    private String nombre;
    private String tipo;
    private String estado;
    private String descripcion;
    private Long idDueno; // Usamos siempre 'n'
    private Double latitud;
    private Double longitud;

    public Mascota() {} // Constructor vacío fundamental

    public Mascota(String id, String nombre, String tipo, String estado, String descripcion, Long idDueno, Double latitud, Double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = estado;
        this.descripcion = descripcion;
        this.idDueno = idDueno;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y Setters limpios
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Long getIdDueno() { return idDueno; }
    public void setIdDueno(Long idDueno) { this.idDueno = idDueno; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}