package skyq.model;

import java.time.LocalDateTime;

public class Pasajero {

    private int idPasajero;
    private String nombre;
    private String numAsiento;
    private int nivelPrioridad;
    private LocalDateTime timestampLlegada;
    private String matricula; // Relación contextual con el avión

    public Pasajero() {
    }

    // Constructor de 5 parámetros para compatibilidad con lógica previa
    public Pasajero(int idPasajero, String nombre, String numAsiento, int nivelPrioridad, LocalDateTime timestampLlegada) {
        this.idPasajero = idPasajero;
        this.nombre = nombre;
        this.numAsiento = numAsiento;
        this.nivelPrioridad = nivelPrioridad;
        this.timestampLlegada = timestampLlegada;
        this.matricula = "";
    }

    // Constructor completo de 6 parámetros para el Dashboard Operativo
    public Pasajero(int idPasajero, String nombre, String numAsiento, int nivelPrioridad, LocalDateTime timestampLlegada, String matricula) {
        this.idPasajero = idPasajero;
        this.nombre = nombre;
        this.numAsiento = numAsiento;
        this.nivelPrioridad = nivelPrioridad;
        this.timestampLlegada = timestampLlegada;
        this.matricula = matricula;
    }

    public int getIdPasajero() { return idPasajero; }
    public void setIdPasajero(int idPasajero) { this.idPasajero = idPasajero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNumAsiento() { return numAsiento; }
    public void setNumAsiento(String numAsiento) { this.numAsiento = numAsiento; }

    public int getNivelPrioridad() { return nivelPrioridad; }
    public void setNivelPrioridad(int nivelPrioridad) { this.nivelPrioridad = nivelPrioridad; }

    public LocalDateTime getTimestampLlegada() { return timestampLlegada; }
    public void setTimestampLlegada(LocalDateTime timestampLlegada) { this.timestampLlegada = timestampLlegada; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
}