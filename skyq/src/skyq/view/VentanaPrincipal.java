package skyq.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;

public class VentanaPrincipal extends JFrame {

    private JTabbedPane tabbedPane;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("SkyQ - Consola Aeroportuaria de Control Integrado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Inicia en Pantalla Completa automáticamente

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(EstiloUI.FONDO_TARJETA);
        tabbedPane.setForeground(EstiloUI.TEXTO_BLANCO);

        // 🔥 ARQUITECTURA GANADORA: Solo dos núcleos macro de control global
        tabbedPane.addTab("1. Centro de Comando (Gerente)", new PanelGerente());
        tabbedPane.addTab("2. Gestión Operativa de Pasajeros", new PanelCheckIn());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}