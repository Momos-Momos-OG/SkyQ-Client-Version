package skyq.cliente.service;

import skyq.cliente.db.ConexionBD;
import skyq.cliente.model.PasajeroDTO;
import skyq.cliente.model.VueloDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Clase de servicio que maneja la lógica de negocio y las interacciones con la base de datos
 * para la aplicación cliente standalone SkyQ B2C.
 * Centraliza las acciones JDBC y asegura que las transacciones de base de datos se ejecuten limpiamente.
 */
public class ClienteService {

    private final Random random = new Random();

    /**
     * Recupera todos los vuelos que están actualmente programados.
     */
    public List<VueloDTO> obtenerVuelosProgramados() throws Exception {
        final String sql = "SELECT matricula, codigoVuelo, fechaSalida, destino FROM vuelos WHERE estado = 'Programado'";
        List<VueloDTO> vuelos = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vuelos.add(new VueloDTO(
                        rs.getString("codigoVuelo"),
                        rs.getString("destino"),
                        rs.getString("fechaSalida"),
                        rs.getString("matricula")
                ));
            }
        }
        return vuelos;
    }

    /**
     * Recupera la configuración de distribución de asientos de la aeronave con la matrícula dada.
     */
    public String obtenerDistribucionAvion(String matricula) throws Exception {
        final String sql = "SELECT distribucion_clases FROM configuracion_asientos WHERE matricula = ?";
        String distribucion = "VIP:2-2:4|ECON:3-3:20"; // Valor por defecto

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dist = rs.getString("distribucion_clases");
                    if (dist != null && !dist.trim().isEmpty()) {
                        distribucion = dist;
                    }
                }
            }
        }
        return distribucion;
    }

    /**
     * Recupera el conjunto de asientos que están actualmente ocupados para la aeronave con la matrícula dada.
     */
    public Set<String> obtenerAsientosOcupados(String matricula) throws Exception {
        final String sql = "SELECT numAsiento FROM pasajero WHERE matricula = ?";
        Set<String> occupiedSeats = new HashSet<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    occupiedSeats.add(rs.getString("numAsiento"));
                }
            }
        }
        return occupiedSeats;
    }

    /**
     * Procesa una reserva para una lista de pasajeros en una única transacción.
     * Genera un PNR único, realiza la inserción por lotes de los registros de pasajeros y
     * confirma la transacción. Si ocurre algún error de base de datos, revierte los cambios.
     */
    public String procesarReservaTransaccional(String matricula, List<PasajeroDTO> pasajeros) throws Exception {
        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new IllegalArgumentException("El grupo de pasajeros no puede estar vacio.");
        }

        final String pnr = generarPNR();
        final String sql =
                "INSERT INTO pasajero " +
                "  (nombre, numAsiento, nivelPrioridad, timestampLlegada, " +
                "   matricula, pnr, sillaRuedas, upgrade) " +
                "VALUES (?, ?, ?, NULL, ?, ?, ?, 0)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sql);
            for (PasajeroDTO p : pasajeros) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getAsiento());
                ps.setInt(3, extraerPrioridad(p.getClase()));
                ps.setString(4, matricula);
                ps.setString(5, pnr);
                ps.setBoolean(6, p.isSillaRuedas());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            return pnr;

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Ignorado
                }
            }
            throw ex;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignored) {}
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Genera un registro de nombre de pasajero (PNR) alfanumérico aleatorio que comienza con 'SQ-'.
     */
    private String generarPNR() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("SQ-");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Determina el nivel de prioridad basado en la clase de cabina seleccionada.
     * VIP corresponde a 1, Ejecutiva a 2, y otros (por ejemplo, Económica) a 3.
     */
    private int extraerPrioridad(String claseTexto) {
        if (claseTexto != null) {
            if (claseTexto.startsWith("VIP")) {
                return 1;
            }
            if (claseTexto.startsWith("Ejecutiva")) {
                return 2;
            }
        }
        return 3;
    }
}
