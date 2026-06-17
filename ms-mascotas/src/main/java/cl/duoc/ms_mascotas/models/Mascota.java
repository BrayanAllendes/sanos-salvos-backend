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
    
    // Nuevos campos para trazabilidad completa
    private String chip;
    private String vacunas;
    private String sector;
    private String prioridad;
    private Boolean despachado;

    public Mascota() {} // Constructor vacío fundamental

    public Mascota(String id, String nombre, String tipo, String estado, String descripcion, Long idDueno, Double latitud, Double longitud, String chip, String vacunas, String sector, String prioridad, Boolean despachado) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = estado;
        this.descripcion = descripcion;
        this.idDueno = idDueno;
        this.latitud = latitud;
        this.longitud = longitud;
        this.chip = chip;
        this.vacunas = vacunas;
        this.sector = sector;
        this.prioridad = prioridad;
        this.despachado = despachado;
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
    public String getChip() { return chip; }
    public void setChip(String chip) { this.chip = chip; }
    public String getVacunas() { return vacunas; }
    public void setVacunas(String vacunas) { this.vacunas = vacunas; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public Boolean getDespachado() { return despachado; }
    public void setDespachado(Boolean despachado) { this.despachado = despachado; }
}