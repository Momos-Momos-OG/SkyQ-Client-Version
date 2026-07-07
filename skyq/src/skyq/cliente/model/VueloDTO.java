package skyq.cliente.model;

/**
 * Objeto de Transferencia de Datos (DTO) que representa un vuelo programado dentro del sistema SkyQ.
 *
 * Esta clase actúa como un contenedor inmutable que transporta la información esencial de un vuelo 
 * desde la capa de acceso a datos hacia las capas de lógica de negocio y presentación. Su propósito 
 * es desacoplar la arquitectura de la aplicación, permitiendo que la interfaz de usuario y los 
 * controladores accedan a los detalles del vuelo sin interactuar directamente con la estructura de la base de datos.
 *
 * La información clave que encapsula es crítica para las operaciones de reserva y logística aeroportuaria:
 * - El código único del vuelo (ej. SQ123), utilizado para la identificación oficial y seguimiento de operaciones.
 * - El destino final del vuelo, crucial para la selección del itinerario por parte del cliente.
 * - La fecha y hora programada de salida, fundamental para la gestión de tiempos de embarque y puntualidad.
 * - La matrícula de la aeronave asignada, que vincula este vuelo con la infraestructura física operativa.
 */
public class VueloDTO {

    private final String codigoVuelo;
    private final String destino;
    private final String fechaSalida;
    private final String matricula;

    /**
     * Construye un nuevo VueloDTO con todos sus atributos requeridos para la cartelera.
     *
     * @param codigoVuelo el identificador alfanumérico del vuelo
     * @param destino el destino final de llegada del vuelo
     * @param fechaSalida la fecha y hora programadas para el despegue
     * @param matricula el identificador único de la aeronave asignada
     */
    public VueloDTO(String codigoVuelo, String destino, String fechaSalida, String matricula) {
        this.codigoVuelo = codigoVuelo;
        this.destino = destino;
        this.fechaSalida = fechaSalida;
        this.matricula = matricula;
    }

    /**
     * Obtiene el código del vuelo.
     *
     * @return el código alfanumérico único del vuelo
     */
    public String getCodigoVuelo() {
        return codigoVuelo;
    }

    /**
     * Obtiene el destino.
     *
     * @return la ciudad o aeropuerto de destino final
     */
    public String getDestino() {
        return destino;
    }

    /**
     * Obtiene la fecha y hora de salida programada.
     *
     * @return la fecha y hora de salida en formato de cadena
     */
    public String getFechaSalida() {
        return fechaSalida;
    }

    /**
     * Obtiene la matrícula de la aeronave.
     *
     * @return el código de registro único de la aeronave asignada
     */
    public String getMatricula() {
        return matricula;
    }
}
