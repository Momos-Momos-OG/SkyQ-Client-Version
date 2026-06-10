package skyq.dao;

import skyq.database.ConexionBD;
import skyq.model.Avion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AvionDAO {

    public boolean guardarAvion(Avion avion) {
        String sql = "INSERT INTO aviones (matricula, modelo, capacidad, estado) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, avion.getMatricula());
            statement.setString(2, avion.getModelo());
            statement.setInt(3, avion.getCapacidad());
            statement.setString(4, avion.getEstado());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verificarMatriculaRegistrada(String matricula) {
        String sql = "SELECT COUNT(*) FROM aviones WHERE matricula = ?";
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

    public List<Avion> obtenerAvionesFlota() {
        List<Avion> lista = new ArrayList<>();
        String sql = "SELECT matricula, modelo, capacidad, estado FROM aviones";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                lista.add(new Avion(
                        resultSet.getString("matricula"),
                        resultSet.getString("modelo"),
                        resultSet.getInt("capacidad"),
                        resultSet.getString("estado")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizarAvion(Avion avion) {
        String sql = "UPDATE aviones SET modelo = ?, capacidad = ?, estado = ? WHERE matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, avion.getModelo());
            statement.setInt(2, avion.getCapacidad());
            statement.setString(3, avion.getEstado());
            statement.setString(4, avion.getMatricula());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}