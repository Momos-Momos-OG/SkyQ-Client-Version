package skyq.view;

import skyq.database.ConexionBD;
import skyq.dao.PasajeroDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class MapaAsientosPanel extends JPanel {

    private final String matriculaAvion;
    private final AsientoSeleccionadoListener listener;
    private final PasajeroDAO pasajeroDAO;

    private int filas = 10;      // Caídas seguras por defecto
    private int columnas = 7;
    private final Set<Integer> indicesPasillos = new HashSet<>();
    private JToggleButton asientoSeleccionadoActual = null;

    public interface AsientoSeleccionadoListener {
        void onAsientoSeleccionado(String codigoAsiento);
    }

    public MapaAsientosPanel(String matriculaAvion, AsientoSeleccionadoListener listener) {
        this.matriculaAvion = matriculaAvion;
        this.listener = listener;
        this.pasajeroDAO = new PasajeroDAO();

        setBackground(EstiloUI.FONDO_TARJETA);
        cargarConfiguracionEspacial();
        initComponents();
    }

    private void cargarConfiguracionEspacial() {
        String sql = "SELECT filas, columnas, pasillos FROM configuracion_asientos WHERE matricula = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matriculaAvion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.filas = rs.getInt("filas");
                    this.columnas = rs.getInt("columnas");
                    String csvPasillos = rs.getString("pasillos");
                    if (csvPasillos != null && !csvPasillos.trim().isEmpty()) {
                        for (String p : csvPasillos.split(",")) {
                            indicesPasillos.add(Integer.parseInt(p.trim()) - 1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Si el avión no tiene mapa guardado, asume una configuración base estándar
            this.filas = 12;
            this.columnas = 7;
            indicesPasillos.add(3); // Pasillo en el medio por defecto
        }
    }

    private void initComponents() {
        setLayout(new GridLayout(filas, columnas, 6, 6));
        ButtonGroup grupoAsientos = new ButtonGroup();
        String[] letrasAsientos = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M"};

        for (int f = 1; f <= filas; f++) {
            int indiceLetra = 0;
            for (int c = 0; c < columnas; c++) {

                if (indicesPasillos.contains(c)) {
                    // Es un pasillo: No lleva nomenclatura, solo la fila indicadora en la mitad
                    JLabel lblPasillo = new JLabel(String.valueOf(f), SwingConstants.CENTER);
                    lblPasillo.setForeground(EstiloUI.TEXTO_MUTED);
                    lblPasillo.setFont(new Font("SansSerif", Font.BOLD, 10));
                    add(lblPasillo);
                    continue;
                }

                String codigoAsiento = f + letrasAsientos[indiceLetra % letrasAsientos.length];
                indiceLetra++;

                // 🔥 CORREGIDO: Se valida usando el método nativo existente 'verificarAsientoOcupado'
                boolean estaOcupado = pasajeroDAO.verificarAsientoOcupado(codigoAsiento);

                JToggleButton btnAsiento = new JToggleButton(codigoAsiento);
                btnAsiento.setOpaque(true);
                btnAsiento.setBorder(EstiloUI.BORDE_COMPONENTE);
                btnAsiento.setFont(new Font("SansSerif", Font.BOLD, 10));

                if (estaOcupado) {
                    btnAsiento.setBackground(EstiloUI.ASIENTO_OCUPADO);
                    btnAsiento.setForeground(EstiloUI.TEXTO_MUTED);
                    btnAsiento.setEnabled(false);
                } else {
                    btnAsiento.setBackground(EstiloUI.VERDE_NEON);
                    btnAsiento.setForeground(EstiloUI.TEXTO_BLANCO);
                    grupoAsientos.add(btnAsiento);

                    btnAsiento.addActionListener(e -> {
                        if (asientoSeleccionadoActual != null) {
                            asientoSeleccionadoActual.setBackground(EstiloUI.VERDE_NEON);
                        }
                        btnAsiento.setBackground(EstiloUI.AZUL_ACCENT);
                        asientoSeleccionadoActual = btnAsiento;
                        listener.onAsientoSeleccionado(codigoAsiento);
                    });
                }
                add(btnAsiento);
            }
        }
    }
}