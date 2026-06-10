package skyq.model;

public class Piloto {
    private int idPiloto;
    private String nombre;
    private String rango;
    private String estado;

    public Piloto() {}

    public Piloto(int idPiloto, String nombre, String rango, String estado) {
        this.idPiloto = idPiloto;
        this.nombre = nombre;
        this.rango = rango;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdPiloto() { return idPiloto; }
    public void setIdPiloto(int idPiloto) { this.idPiloto = idPiloto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRango() { return rango; }
    public void setRango(String rango) { this.rango = rango; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}