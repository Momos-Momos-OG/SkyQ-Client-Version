package skyq.dao;

import skyq.database.ConexionBD;
import skyq.model.Pasajero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PasajeroDAO {

    public boolean insertarPasajero(Pasajero pasajero) {
        String sql = "INSERT INTO pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada, matricula) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pasajero.getNombre());
            statement.setString(2, pasajero.getNumAsiento());
            statement.setInt(3, pasajero.getNivelPrioridad());
            LocalDateTime ts = pasajero.getTimestampLlegada();
            statement.setTimestamp(4, ts != null ? Timestamp.valueOf(ts) : null);
            statement.setString(5, pasajero.getMatricula() != null ? pasajero.getMatricula() : "");
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insertarPasajeroYObtenerId(Pasajero pasajero) {
        String sql = "INSERT INTO pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada, matricula) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pasajero.getNombre());
            statement.setString(2, pasajero.getNumAsiento());
            statement.setInt(3, pasajero.getNivelPrioridad());
            LocalDateTime ts = pasajero.getTimestampLlegada();
            statement.setTimestamp(4, ts != null ? Timestamp.valueOf(ts) : null);
            statement.setString(5, pasajero.getMatricula() != null ? pasajero.getMatricula() : "");

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Método original global para evitar errores de compilación
    public List<Pasajero> obtenerPasajerosVuelo() {
        List<Pasajero> pasajeros = new ArrayList<>();
        String sql = "SELECT idPasajero, nombre, numAsiento, nivelPrioridad, timestampLlegada, matricula FROM pasajero ORDER BY idPasajero";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Pasajero p = new Pasajero();
                p.setIdPasajero(resultSet.getInt("idPasajero"));
                p.setNombre(resultSet.getString("nombre"));
                p.setNumAsiento(resultSet.getString("numAsiento"));
                p.setNivelPrioridad(resultSet.getInt("nivelPrioridad"));
                p.setMatricula(resultSet.getString("matricula"));
                Timestamp ts = resultSet.getTimestamp("timestampLlegada");
                if (ts != null) p.setTimestampLlegada(ts.toLocalDateTime());
                pasajeros.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pasajeros;
    }

    // Método contextual para filtrar exclusivamente por la matrícula elegida
    public List<Pasajero> obtenerPasajerosPorVuelo(String matricula) {
        List<Pasajero> pasajeros = new ArrayList<>();
        String sql = "SELECT idPasajero, nombre, numAsiento, nivelPrioridad, timestampLlegada FROM pasajero WHERE matricula = ? ORDER BY idPasajero";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Pasajero p = new Pasajero();
                    p.setIdPasajero(resultSet.getInt("idPasajero"));
                    p.setNombre(resultSet.getString("nombre"));
                    p.setNumAsiento(resultSet.getString("numAsiento"));
                    p.setNivelPrioridad(resultSet.getInt("nivelPrioridad"));
                    p.setMatricula(matricula);
                    Timestamp ts = resultSet.getTimestamp("timestampLlegada");
                    if (ts != null) p.setTimestampLlegada(ts.toLocalDateTime());
                    pasajeros.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pasajeros;
    }

    public boolean verificarAsientoOcupado(String asiento) {
        String sql = "SELECT COUNT(*) FROM pasajero WHERE numAsiento = ?";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, asiento);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verificarAsientoOcupadoEnVuelo(String asiento, String matricula) {
        String sql = "SELECT COUNT(*) FROM pasajero WHERE numAsiento = ? AND matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, asiento);
            statement.setString(2, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}