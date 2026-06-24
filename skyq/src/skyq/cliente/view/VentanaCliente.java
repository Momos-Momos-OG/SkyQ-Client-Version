package skyq.cliente.view;

import skyq.cliente.db.ConexionBD;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * VentanaCliente - Portal B2C del Pasajero (SkyQ Standalone). Reload.
 * Dependencias externas: NINGUNA. Solo JDK 17 + JDBC SQL Server driver.
 * Sincronizacion: javax.swing.Timer cada 3 s. Ciclo de vida via addNotify/removeNotify.
 */
public final class VentanaCliente extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(VentanaCliente.class.getName());

    // Paleta de colores interna (sin EstiloUI)
    static final Color C_FONDO        = new Color(13,  17,  23);
    static final Color C_TARJETA      = new Color(22,  27,  34);
    static final Color C_AZUL         = new Color(31, 111, 235);
    static final Color C_GRIS         = new Color(33,  38,  45);
    static final Color C_TEXTO        = new Color(240, 246, 252);
    static final Color C_MUTED        = new Color(139, 148, 158);
    static final Color C_BORDE        = new Color(48,   54,  61);
    static final Color C_FILA_ALTERNA = new Color(27,   32,  40);

    private final Timer             timerRecarga;
    private final CardLayout        cardLayout;
    private final JPanel            cardPanel;

    // Contenedor vertical para las tarjetas de vuelo
    private final JPanel            panelContenedorVuelos;
    private final transient List<VueloTemporal> listaVuelos = new ArrayList<>();
    private String                  codigoVueloExpandido = null;

    private static class VueloTemporal {
        final String codigoVuelo;
        final String destino;
        final String fechaSalida;
        final String matricula;

        VueloTemporal(String codigoVuelo, String destino, String fechaSalida, String matricula) {
            this.codigoVuelo = codigoVuelo;
            this.destino = destino;
            this.fechaSalida = fechaSalida;
            this.matricula = matricula;
        }
    }

    public VentanaCliente() {
        setTitle("SkyQ - Portal del Pasajero");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 800);
        setResizable(false);
        setUndecorated(true); // Sin bordes nativos para emulación móvil
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_FONDO);
        setLayout(new BorderLayout());

        // Barra de título simulada
        add(crearBarraTitulo(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(C_FONDO);

        // Pantalla 1: Cartelera de Vuelos
        JPanel pantallaCartelera = new JPanel(new BorderLayout());
        pantallaCartelera.setBackground(C_FONDO);

        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.setBackground(C_FONDO);
        centro.setBorder(new EmptyBorder(20, 16, 12, 16));
        centro.add(crearEncabezado(), BorderLayout.NORTH);

        // Contenedor vertical con BoxLayout para las tarjetas
        panelContenedorVuelos = new JPanel();
        panelContenedorVuelos.setLayout(new BoxLayout(panelContenedorVuelos, BoxLayout.Y_AXIS));
        panelContenedorVuelos.setBackground(C_FONDO);

        JScrollPane scroll = new JScrollPane(panelContenedorVuelos);
        scroll.setBackground(C_FONDO);
        scroll.getViewport().setBackground(C_FONDO);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        centro.add(scroll, BorderLayout.CENTER);
        
        pantallaCartelera.add(centro, BorderLayout.CENTER);

        cardPanel.add(pantallaCartelera, "CARTELERA");
        add(cardPanel, BorderLayout.CENTER);

        timerRecarga = new Timer(3_000, e -> cargarVuelos());
        timerRecarga.setInitialDelay(0);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        boolean hasReserva = false;
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof PanelReserva) {
                hasReserva = true;
                break;
            }
        }
        // Solo inicia el timer si no estamos en la pantalla de reserva y no hay ninguna tarjeta expandida
        if (!hasReserva && codigoVueloExpandido == null && !timerRecarga.isRunning()) {
            timerRecarga.start();
        }
    }

    @Override
    public void removeNotify() {
        if (timerRecarga.isRunning()) timerRecarga.stop();
        super.removeNotify();
    }

    // Regresa a la cartelera y limpia el PanelReserva anterior
    public void showCartelera() {
        codigoVueloExpandido = null;
        cardLayout.show(cardPanel, "CARTELERA");
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof PanelReserva) {
                cardPanel.remove(c);
            }
        }
        cardPanel.revalidate();
        cardPanel.repaint();
        if (!timerRecarga.isRunning()) {
            timerRecarga.start();
        }
        cargarVuelos();
    }

    // Barra superior oscura con titulo y boton de cierre
    private JPanel crearBarraTitulo() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(C_TARJETA);
        barra.setPreferredSize(new Dimension(getWidth(), 36));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE));

        JLabel lbl = new JLabel("  Cartelera de Vuelos - SkyQ Portal");
        lbl.setForeground(C_TEXTO);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        barra.add(lbl, BorderLayout.WEST);

        JButton btnX = new JButton("X");
        btnX.setPreferredSize(new Dimension(45, 35));
        btnX.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnX.setForeground(C_MUTED);
        btnX.setBackground(C_TARJETA);
        btnX.setBorderPainted(false);
        btnX.setFocusPainted(false);
        btnX.setContentAreaFilled(false);
        btnX.setOpaque(true);
        btnX.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnX.setBackground(new Color(220, 53, 69));
                btnX.setForeground(C_TEXTO);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnX.setBackground(C_TARJETA);
                btnX.setForeground(C_MUTED);
            }
        });
        btnX.addActionListener(e -> System.exit(0));
        barra.add(btnX, BorderLayout.EAST);
        return barra;
    }

    // Titulo y subtitulo encima de la cartelera
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FONDO);
        p.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel titulo = new JLabel("Vuelos Disponibles - Salidas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(C_TEXTO);

        JLabel sub = new JLabel("Actualización cada 3s - Toca para detalles");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(C_MUTED);

        p.add(titulo, BorderLayout.NORTH);
        p.add(sub,    BorderLayout.SOUTH);
        return p;
    }

    // Consulta la BD y repobla la lista de vuelos
    private void cargarVuelos() {
        final String sql = "SELECT matricula, codigoVuelo, fechaSalida, destino FROM vuelos WHERE estado = 'Programado'";
        listaVuelos.clear();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaVuelos.add(new VueloTemporal(
                        rs.getString("codigoVuelo"),
                        rs.getString("destino"),
                        rs.getString("fechaSalida"),
                        rs.getString("matricula")
                ));
            }
        } catch (SQLException ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error de conexion o consulta de BD", ex);
            JOptionPane.showMessageDialog(this,
                    "Error de conexión o consulta de BD:\n" + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }

        renderizarVuelos();
    }

    // Renderiza la lista de vuelos en tarjetas
    private void renderizarVuelos() {
        panelContenedorVuelos.removeAll();

        for (VueloTemporal v : listaVuelos) {
            boolean isExpanded = v.codigoVuelo.equals(codigoVueloExpandido);
            JPanel card = crearTarjetaVuelo(v, isExpanded);
            panelContenedorVuelos.add(card);
            panelContenedorVuelos.add(Box.createVerticalStrut(10));
        }

        panelContenedorVuelos.revalidate();
        panelContenedorVuelos.repaint();
    }

    // Crea el diseño de tarjeta expandible para cada vuelo
    private JPanel crearTarjetaVuelo(VueloTemporal v, boolean isExpanded) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(418, isExpanded ? 130 : 64));
        card.setPreferredSize(new Dimension(418, isExpanded ? 130 : 64));

        // Cabecera: Destino, Fecha, y Botón de Toggle
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_TARJETA);

        JPanel pInfoBasica = new JPanel();
        pInfoBasica.setLayout(new BoxLayout(pInfoBasica, BoxLayout.Y_AXIS));
        pInfoBasica.setBackground(C_TARJETA);

        JLabel lblDestino = new JLabel("Destino: " + v.destino);
        lblDestino.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDestino.setForeground(C_TEXTO);
        pInfoBasica.add(lblDestino);

        JLabel lblFecha = new JLabel("Salida: " + v.fechaSalida);
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblFecha.setForeground(C_MUTED);
        pInfoBasica.add(lblFecha);

        header.add(pInfoBasica, BorderLayout.CENTER);

        JLabel lblToggle = new JLabel(isExpanded ? "▲ Colapsar" : "▼ Detalles");
        lblToggle.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblToggle.setForeground(C_AZUL);
        lblToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(lblToggle, BorderLayout.EAST);

        card.add(header);

        if (isExpanded) {
            card.add(Box.createVerticalStrut(10));

            JPanel pDetalles = new JPanel(new BorderLayout());
            pDetalles.setBackground(C_TARJETA);

            JPanel pInfoExtra = new JPanel();
            pInfoExtra.setLayout(new BoxLayout(pInfoExtra, BoxLayout.Y_AXIS));
            pInfoExtra.setBackground(C_TARJETA);

            JLabel lblCodigo = new JLabel("Vuelo: " + v.codigoVuelo);
            lblCodigo.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblCodigo.setForeground(C_TEXTO);
            pInfoExtra.add(lblCodigo);

            JLabel lblMatricula = new JLabel("Aeronave: " + v.matricula);
            lblMatricula.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblMatricula.setForeground(C_MUTED);
            pInfoExtra.add(lblMatricula);

            pDetalles.add(pInfoExtra, BorderLayout.CENTER);

            JButton btnComprar = new JButton("COMPRAR BOLETOS");
            btnComprar.setFont(new Font("SansSerif", Font.BOLD, 11));
            btnComprar.setBackground(C_AZUL);
            btnComprar.setForeground(C_TEXTO);
            btnComprar.setFocusPainted(false);
            btnComprar.setBorderPainted(false);
            btnComprar.setOpaque(true);
            btnComprar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnComprar.setPreferredSize(new Dimension(150, 32));
            btnComprar.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    btnComprar.setBackground(C_AZUL.brighter());
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    btnComprar.setBackground(C_AZUL);
                }
            });
            btnComprar.addActionListener(e -> {
                timerRecarga.stop();
                PanelReserva panelReserva = new PanelReserva(VentanaCliente.this, v.matricula, v.codigoVuelo);
                cardPanel.add(panelReserva, "RESERVA");
                cardLayout.show(cardPanel, "RESERVA");
            });
            pDetalles.add(btnComprar, BorderLayout.EAST);

            card.add(pDetalles);
        }

        // Comportamiento de Toggle
        java.awt.event.MouseAdapter toggleListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isExpanded) {
                    codigoVueloExpandido = null;
                    timerRecarga.start();
                } else {
                    codigoVueloExpandido = v.codigoVuelo;
                    timerRecarga.stop();
                }
                renderizarVuelos();
            }
        };

        header.addMouseListener(toggleListener);
        lblToggle.addMouseListener(toggleListener);

        return card;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        SwingUtilities.invokeLater(() -> new VentanaCliente().setVisible(true));
    }
}