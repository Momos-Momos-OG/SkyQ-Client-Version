package skyq.view;

import skyq.dao.PasajeroDAO;
import skyq.logic.DesembarqueManager;
import skyq.model.Pasajero;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelDesembarque extends JPanel {

    private JTable tablaDesembarque;
    private DefaultTableModel modeloTabla;
    private JButton btnIniciarDesembarque;

    public PanelDesembarque() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        btnIniciarDesembarque = new JButton("Iniciar Desembarque");
        btnIniciarDesembarque.setBackground(EstiloUI.AZUL_ACCENT);
        btnIniciarDesembarque.setForeground(EstiloUI.TEXTO_BLANCO);
        btnIniciarDesembarque.setFont(EstiloUI.FUENTE_SUBTITULO);
        btnIniciarDesembarque.setBorderPainted(false);
        add(btnIniciarDesembarque, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Asiento", "Prioridad", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaDesembarque = new JTable(modeloTabla);
        tablaDesembarque.setBackground(EstiloUI.FONDO_TARJETA);
        tablaDesembarque.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaDesembarque.setGridColor(new Color(51, 65, 85));

        JScrollPane scroll = new JScrollPane(tablaDesembarque);
        scroll.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        add(scroll, BorderLayout.CENTER);

        btnIniciarDesembarque.addActionListener(e -> iniciarDesembarque());
    }

    private void iniciarDesembarque() {
        PasajeroDAO pasajeroDAO = new PasajeroDAO();
        DesembarqueManager manager = new DesembarqueManager();

        List<Pasajero> pasajeros = pasajeroDAO.obtenerPasajerosVuelo();
        List<Pasajero> pasajerosOrdenados = manager.ordenarPorAsiento(pasajeros);

        modeloTabla.setRowCount(0);
        for (Pasajero p : pasajerosOrdenados) {
            modeloTabla.addRow(new Object[]{p.getIdPasajero(), p.getNombre(), p.getNumAsiento(), p.getNivelPrioridad(), p.getTimestampLlegada()});
        }
    }
}