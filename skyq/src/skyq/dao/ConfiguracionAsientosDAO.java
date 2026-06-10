package skyq.dao;

import skyq.database.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracionAsientosDAO {

    public boolean guardarConfiguracion(String matricula, int filas, int columnas, String pasillos) {
        String sql = "MERGE INTO configuracion_asientos WITH (HOLDLOCK) AS target " +
                "USING (SELECT ? AS matricula) AS source " +
                "ON (target.matricula = source.matricula) " +
                "WHEN MATCHED THEN UPDATE SET filas = ?, columnas = ?, pasillos = ? " +
                "WHEN NOT MATCHED THEN INSERT (matricula, filas, columnas, pasillos) VALUES (?, ?, ?, ?);";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, matricula);
            statement.setInt(2, filas);
            statement.setInt(3, columnas);
            statement.setString(4, pasillos);
            statement.setString(5, matricula);
            statement.setInt(6, filas);
            statement.setInt(7, columnas);
            statement.setString(8, pasillos);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean tieneConfiguracion(String matricula) {
        String sql = "SELECT COUNT(*) FROM configuracion_asientos WHERE matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
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