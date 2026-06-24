package skyq.cliente;

import skyq.cliente.view.VentanaCliente;
import javax.swing.*;

/** Punto de entrada del Portal del Pasajero SkyQ (standalone). Reload. */
public final class MainCliente {

    private MainCliente() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        SwingUtilities.invokeLater(() -> new VentanaCliente().setVisible(true));
    }
}