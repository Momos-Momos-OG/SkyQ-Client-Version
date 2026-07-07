package skyq.cliente.model;

/**
 * Objeto de Transferencia de Datos (DTO) que representa a un pasajero dentro del sistema SkyQ.
 *
 * Esta clase actúa como un contenedor inmutable de la información del pasajero durante el proceso 
 * de reserva e interacción con la interfaz gráfica. Su propósito es desacoplar los datos ingresados 
 * en la UI de las entidades de persistencia complejas, permitiendo mover la información de forma limpia.
 *
 * En el contexto del negocio aeroportuario, la información contenida define:
 * - El nombre del pasajero para el boleto y el manifiesto oficial.
 * - El tipo de pasajero (ej. Adulto, Niño) para controles operacionales y tarifas.
 * - La clase de cabina (ej. VIP, Ejecutiva, Económica) para prioridades y franquicias de equipaje.
 * - El requerimiento de asistencia especial (silla de ruedas), fundamental para la logística de abordaje.
 * - El número de asiento asignado físicamente en la aeronave.
 */
public class PasajeroDTO {

    private final String nombre;
    private final String tipo;
    private final String clase;
    private final boolean sillaRuedas;
    private String asiento;

    /**
     * Construye un nuevo PasajeroDTO sin asiento asignado para comenzar el proceso de reserva.
     *
     * @param nombre el nombre completo del pasajero
     * @param tipo el tipo de pasajero (ej. Adulto, Niño)
     * @param clase la clase de cabina elegida por el pasajero
     * @param sillaRuedas el indicador de si el pasajero requiere asistencia de silla de ruedas
     */
    public PasajeroDTO(String nombre, String tipo, String clase, boolean sillaRuedas) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.clase = clase;
        this.sillaRuedas = sillaRuedas;
        this.asiento = null;
    }

    /**
     * Obtiene el nombre del pasajero.
     *
     * @return el nombre del pasajero
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el tipo de pasajero.
     *
     * @return el tipo del pasajero
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Obtiene la clase de cabina.
     *
     * @return la clase del pasajero en el vuelo
     */
    public String getClase() {
        return clase;
    }

    /**
     * Verifica si se requiere asistencia en silla de ruedas.
     *
     * @return true si requiere silla de ruedas, false en caso contrario
     */
    public boolean isSillaRuedas() {
        return sillaRuedas;
    }

    /**
     * Obtiene el número de asiento asignado.
     *
     * @return el identificador del asiento, o null si aún no se ha asignado
     */
    public String getAsiento() {
        return asiento;
    }

    /**
     * Asigna un asiento al pasajero.
     *
     * @param asiento el identificador del asiento a asignar
     */
    public void setAsiento(String asiento) {
        this.asiento = asiento;
    }
}
