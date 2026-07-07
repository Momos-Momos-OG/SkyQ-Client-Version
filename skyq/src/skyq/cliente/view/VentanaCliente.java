package skyq.cliente.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.cliente.model.VueloDTO;
import skyq.cliente.service.ClienteService;

/**
 * VentanaCliente - Interfaz del portal del pasajero B2C (SkyQ Standalone).
 * Actúa como la ventana principal que muestra la cartelera de vuelos programados.
 * Se adhiere estrictamente a MVC al delegar la carga de datos a ClienteService
 * y utilizar VueloDTO para el intercambio de datos.
 */
public final class VentanaCliente extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(VentanaCliente.class.getName());

    // Paleta de colores interna (sin EstiloUI)
    static final Color C_FONDO        = new Color(13,  17,  23);
    static final Color C_TARJETA      = new Color(22,  27,  34);
    static final Color C_AZUL         = new Color(31, 111, 235);
    static final Color C_TEXTO        = new Color(240, 246, 252);
    static final Color C_MUTED        = new Color(139, 148, 158);
    static final Color C_BORDE        = new Color(48,   54,  61);

    private final Timer             timerRecarga;
    private final CardLayout        cardLayout;
    private final JPanel            cardPanel;

    // Contenedor vertical para las tarjetas de vuelo
    private final JPanel            panelContenedorVuelos;
    private final transient List<VueloDTO> listaVuelos = new ArrayList<>();
    private String                  codigoVueloExpandido = null;

    private final transient ClienteService clienteService = new ClienteService();

    /**
     * Construye la ventana principal del cliente, inicializa componentes y configura
     * el temporizador para recargar automáticamente la cartelera.
     */
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

    /**
     * Sobrescribe addNotify para iniciar el temporizador de actualización de vuelos
     * bajo las condiciones adecuadas.
     */
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

    /**
     * Sobrescribe removeNotify para detener el temporizador de actualización automática.
     */
    @Override
    public void removeNotify() {
        if (timerRecarga.isRunning()) timerRecarga.stop();
        super.removeNotify();
    }

    /**
     * Navega de regreso a la pantalla de la cartelera de vuelos ("CARTELERA"),
     * limpia cualquier panel de reservas anterior y reinicia el temporizador.
     */
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

    /**
     * Crea la barra de título personalizada en la parte superior.
     */
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

    /**
     * Crea los encabezados de título y subtítulo encima de la lista de la cartelera.
     */
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

    /**
     * Carga los vuelos desde la base de datos a través de la capa de servicio.
     * Vuelve a llenar la lista de vuelos y activa el renderizado de la UI.
     */
    private void cargarVuelos() {
        listaVuelos.clear();
        try {
            List<VueloDTO> vuelos = clienteService.obtenerVuelosProgramados();
            listaVuelos.addAll(vuelos);
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error de conexion o consulta de BD", ex);
            JOptionPane.showMessageDialog(this,
                    "Error de conexión o consulta de BD:\n" + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }
        renderizarVuelos();
    }

    /**
     * Itera sobre los vuelos cargados y los renderiza como componentes de tarjetas expandibles.
     */
    private void renderizarVuelos() {
        panelContenedorVuelos.removeAll();

        for (VueloDTO v : listaVuelos) {
            boolean isExpanded = v.getCodigoVuelo().equals(codigoVueloExpandido);
            JPanel card = crearTarjetaVuelo(v, isExpanded);
            panelContenedorVuelos.add(card);
            panelContenedorVuelos.add(Box.createVerticalStrut(10));
        }

        panelContenedorVuelos.revalidate();
        panelContenedorVuelos.repaint();
    }

    /**
     * Construye el componente de tarjeta expandible visual que representa un solo vuelo.
     */
    private JPanel crearTarjetaVuelo(VueloDTO v, boolean isExpanded) {
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

        JLabel lblDestino = new JLabel("Destino: " + v.getDestino());
        lblDestino.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDestino.setForeground(C_TEXTO);
        pInfoBasica.add(lblDestino);

        JLabel lblFecha = new JLabel("Salida: " + v.getFechaSalida());
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

            JLabel lblCodigo = new JLabel("Vuelo: " + v.getCodigoVuelo());
            lblCodigo.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblCodigo.setForeground(C_TEXTO);
            pInfoExtra.add(lblCodigo);

            JLabel lblMatricula = new JLabel("Aeronave: " + v.getMatricula());
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
                PanelReserva panelReserva = new PanelReserva(VentanaCliente.this, v.getMatricula(), v.getCodigoVuelo());
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
                    codigoVueloExpandido = v.getCodigoVuelo();
                    timerRecarga.stop();
                }
                renderizarVuelos();
            }
        };

        header.addMouseListener(toggleListener);
        lblToggle.addMouseListener(toggleListener);

        return card;
    }

    /**
     * Método principal para lanzar la aplicación standalone del Portal del Pasajero SkyQ.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        SwingUtilities.invokeLater(() -> new VentanaCliente().setVisible(true));
    }
}