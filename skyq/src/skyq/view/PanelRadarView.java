package skyq.view;

import skyq.dao.AvionDAO;
import skyq.model.Avion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PanelRadarView extends JPanel {

    private final AvionDAO avionDAO = new AvionDAO();
    private List<Avion> avionesFlota = new ArrayList<>();
    private final List<Rectangle> hitboxes = new ArrayList<>();

    public PanelRadarView() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        recargarDatosAviones();

        // Escucha de clics sobre los elementos del Radar
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < hitboxes.size(); i++) {
                    if (hitboxes.get(i).contains(e.getPoint())) {
                        abrirEditorAvionFlotante(avionesFlota.get(i));
                        break;
                    }
                }
            }
        });
    }

    public void recargarDatosAviones() {
        avionesFlota = avionDAO.obtenerAvionesFlota();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        // Anillos del radar de Figma
        g2.setColor(new Color(48, 54, 61));
        for (int radio = 100; radio <= 350; radio += 80) {
            g2.drawOval(cx - radio, cy - radio, radio * 2, radio * 2);
        }
        g2.drawLine(0, cy, getWidth(), cy);
        g2.drawLine(cx, 0, cx, getHeight());

        hitboxes.clear();
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Renderizar aviones de la Base de Datos con posiciones estables matemáticas
        for (int i = 0; i < avionesFlota.size(); i++) {
            Avion av = avionesFlota.get(i);

            // Generamos posiciones fijas distribuidas usando el hashCode único de la matrícula
            int semilla = Math.abs(av.getMatricula().hashCode());
            int x = 150 + (semilla % Math.max(200, getWidth() - 300));
            int y = 100 + ((semilla / 3) % Math.max(150, getHeight() - 200));

            // Guardamos el área de clic (hitbox) de 25x25 píxeles alrededor del avión
            hitboxes.add(new Rectangle(x - 5, y - 15, 25, 25));

            // Dibujar el icono y la información del avión
            g2.setColor(av.getEstado().equalsIgnoreCase("Disponible") ? EstiloUI.VERDE_NEON : EstiloUI.ROJO_ALERTA);
            g2.drawString("✈", x, y);

            g2.setColor(EstiloUI.TEXTO_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(av.getMatricula() + " (" + av.getEstado() + ")", x - 20, y + 14);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        }

        g2.setColor(EstiloUI.TEXTO_BLANCO);
        g2.setFont(EstiloUI.FUENTE_SUBTITULO);
        g2.drawString("PANTALLA DE RADAR OPERATIVO (Haga clic en un avión para editar)", 25, 30);
    }

    private void abrirEditorAvionFlotante(Avion avion) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Información de Aeronave", true);
        dialog.getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        JTextField txtMod = new JTextField(avion.getModelo(), 12);
        JTextField txtCap = new JTextField(String.valueOf(avion.getCapacidad()), 12);
        JComboBox<String> cbEst = new JComboBox<>(new String[]{"Disponible", "En mantenimiento", "Fuera de servicio"});
        cbEst.setSelectedItem(avion.getEstado());

        txtMod.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); txtMod.setForeground(EstiloUI.TEXTO_BLANCO); txtMod.setBorder(EstiloUI.BORDE_COMPONENTE);
        txtCap.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); txtCap.setForeground(EstiloUI.TEXTO_BLANCO); txtCap.setBorder(EstiloUI.BORDE_COMPONENTE);
        cbEst.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); cbEst.setForeground(EstiloUI.TEXTO_BLANCO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblT = new JLabel("Matrícula: " + avion.getMatricula()); lblT.setForeground(Color.CYAN); dialog.add(lblT, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Modelo:"), gbc); gbc.gridx = 1; dialog.add(txtMod, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Capacidad:"), gbc); gbc.gridx = 1; dialog.add(txtCap, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Estado:"), gbc); gbc.gridx = 1; dialog.add(cbEst, gbc);

        JButton btnAct = new JButton("Actualizar Datos");
        btnAct.setBackground(EstiloUI.AZUL_ACCENT); btnAct.setForeground(EstiloUI.TEXTO_BLANCO); btnAct.setBorderPainted(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; dialog.add(btnAct, gbc);

        // Forzar estilos de etiquetas internas del diálogo
        for (Component comp : dialog.getContentPane().getComponents()) {
            if (comp instanceof JLabel && comp != lblT) comp.setForeground(EstiloUI.TEXTO_MUTED);
        }

        btnAct.addActionListener(e -> {
            try {
                avion.setModelo(txtMod.getText().trim());
                avion.setCapacidad(Integer.parseInt(txtCap.getText().trim()));
                avion.setEstado((String) cbEst.getSelectedItem());

                if (avionDAO.actualizarAvion(avion)) {
                    JOptionPane.showMessageDialog(dialog, "Datos de aeronave actualizados en Docker.");
                    dialog.dispose();
                    recargarDatosAviones();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Campos inválidos.", "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}