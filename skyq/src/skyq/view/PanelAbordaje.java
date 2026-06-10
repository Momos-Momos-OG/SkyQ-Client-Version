package skyq.view;

import skyq.dao.PasajeroDAO;
import skyq.logic.ColaAbordaje;
import skyq.model.Pasajero;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelAbordaje extends JPanel {

    private JTable tablaPasajeros;
    private DefaultTableModel modeloTabla;
    private JButton btnCargarCola;

    public PanelAbordaje() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        btnCargarCola = new JButton("Cargar Cola de Abordaje");
        btnCargarCola.setBackground(EstiloUI.AZUL_ACCENT);
        btnCargarCola.setForeground(EstiloUI.TEXTO_BLANCO);
        btnCargarCola.setFont(EstiloUI.FUENTE_SUBTITULO);
        btnCargarCola.setBorderPainted(false);
        add(btnCargarCola, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Asiento", "Prioridad", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaPasajeros = new JTable(modeloTabla);
        tablaPasajeros.setBackground(EstiloUI.FONDO_TARJETA);
        tablaPasajeros.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaPasajeros.setGridColor(new Color(51, 65, 85));

        JScrollPane scroll = new JScrollPane(tablaPasajeros);
        scroll.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        add(scroll, BorderLayout.CENTER);

        btnCargarCola.addActionListener(e -> cargarColaAbordaje());
    }

    private void cargarColaAbordaje() {
        PasajeroDAO pasajeroDAO = new PasajeroDAO();
        ColaAbordaje colaAbordaje = new ColaAbordaje();

        List<Pasajero> pasajeros = pasajeroDAO.obtenerPasajerosVuelo();
        List<Pasajero> pasajerosOrdenados = colaAbordaje.organizarPasajeros(pasajeros);

        modeloTabla.setRowCount(0);
        for (Pasajero p : pasajerosOrdenados) {
            modeloTabla.addRow(new Object[]{p.getIdPasajero(), p.getNombre(), p.getNumAsiento(), p.getNivelPrioridad(), p.getTimestampLlegada()});
        }
    }
}