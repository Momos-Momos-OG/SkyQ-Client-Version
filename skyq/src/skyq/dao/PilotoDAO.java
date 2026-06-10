package skyq.dao;

import skyq.database.ConexionBD;
import skyq.model.Piloto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PilotoDAO {

    public List<Piloto> obtenerPilotos() {
        List<Piloto> lista = new ArrayList<>();
        String sql = "SELECT idPiloto, nombre, rango, estado FROM pilotos";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Piloto(rs.getInt("idPiloto"), rs.getString("nombre"), rs.getString("rango"), rs.getString("estado")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean insertarPiloto(Piloto p) {
        String sql = "INSERT INTO pilotos (nombre, rango, estado) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getRango());
            stmt.setString(3, p.getEstado());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean actualizarPiloto(Piloto p) {
        String sql = "UPDATE pilotos SET nombre = ?, rango = ?, estado = ? WHERE idPiloto = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getRango());
            stmt.setString(3, p.getEstado());
            stmt.setInt(4, p.getIdPiloto());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean eliminarPiloto(int idPiloto) {
        String sql = "DELETE FROM pilotos WHERE idPiloto = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPiloto);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}