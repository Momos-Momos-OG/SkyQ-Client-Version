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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * PanelReserva - Pantalla de reservas móvil con CardLayout interno para la cabina gráfica.
 * Implementación 100% libre de JTable, usando una lista de tarjetas dinámicas táctiles.
 */
public final class PanelReserva extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PanelReserva.class.getName());

    // Paleta de colores local (Tema Oscuro)
    private static final Color C_FONDO        = new Color(13,  17,  23);
    private static final Color C_TARJETA      = new Color(22,  27,  34);
    private static final Color C_AZUL         = new Color(31, 111, 235);
    private static final Color C_GRIS         = new Color(33,  38,  45);
    private static final Color C_TEXTO        = new Color(240, 246, 252);
    private static final Color C_MUTED        = new Color(139, 148, 158);
    private static final Color C_VERDE        = new Color(46,  160,  67);
    private static final Color C_BORDE        = new Color(48,   54,  61);
    private static final Color C_ROJO         = new Color(220,  53,  69);

    private final VentanaCliente parent;
    private final String matricula;
    private final String codigoVuelo;

    // Distribución física de la cabina del avión extraída de la BD
    private String distribucionAvion = "VIP:2-2:4|ECON:3-3:20";

    // CardLayout interno para navegar entre Vista A (Formulario) y Vista B (Mapa Asientos)
    private final CardLayout cardLayoutInterno;
    private JPanel panelVistaA;
    private JPanel panelVistaB;

    // Componentes del Formulario (Vista A)
    private JTextField        txtNombre;
    private JComboBox<String> comboTipo;
    private JComboBox<String> comboClase;
    private JCheckBox         chkSilla;
    private JPanel            panelListaPasajeros;

    // Contenedor del mapa de asientos (Vista B)
    private JPanel panelContenedorMapa;

    // Estructura de datos en memoria para el grupo familiar
    private final transient List<PasajeroTemporal> grupoFamiliar = new ArrayList<>();
    private int pasajeroEditandoIndex = -1;

    // Ocupación en BD del avión y clase seleccionada válida anterior
    private final transient Set<String> occupiedSeatsDb = new HashSet<>();
    private String            ultimoClaseValida = null;

    /**
     * Representación temporal en memoria de un pasajero del grupo.
     */
    public static class PasajeroTemporal {
        String nombre;
        String tipo; // "Adulto" | "Niño"
        String clase; // "VIP (1)" | "Ejecutiva (2)" | "Economica (3)"
        boolean sillaRuedas;
        String asiento; // nulo al inicio

        public PasajeroTemporal(String nombre, String tipo, String clase, boolean sillaRuedas) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.clase = clase;
            this.sillaRuedas = sillaRuedas;
            this.asiento = null;
        }
    }

    @SuppressWarnings({"this-escape", "LeakingThisInConstructor"})
    public PanelReserva(VentanaCliente parent, String matricula, String codigoVuelo) {
        this.parent      = parent;
        this.matricula   = matricula;
        this.codigoVuelo = codigoVuelo;

        // Cargar configuración de cabina del avión y su ocupación antes de construir la interfaz
        cargarConfiguracionAvion();

        cardLayoutInterno = new CardLayout();
        setLayout(cardLayoutInterno);
        setBackground(C_FONDO);

        inicializarVistaA();
        inicializarVistaB();

        add(panelVistaA, "VISTA_A");
        add(panelVistaB, "VISTA_B");

        cardLayoutInterno.show(this, "VISTA_A");
    }

    // Consulta la BD, obtiene la distribución de cabina y carga los asientos ocupados
    private void cargarConfiguracionAvion() {
        String sql = "SELECT distribucion_clases FROM configuracion_asientos WHERE matricula = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dist = rs.getString("distribucion_clases");
                    if (dist != null && !dist.trim().isEmpty()) {
                        distribucionAvion = dist;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error al cargar configuracion de avion", e);
        }

        // Cargar asientos ocupados
        occupiedSeatsDb.clear();
        String sqlOccupied = "SELECT numAsiento FROM pasajero WHERE matricula = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOccupied)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    occupiedSeatsDb.add(rs.getString("numAsiento"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error al cargar asientos ocupados", e);
        }
    }

    // Calcula si una clase de cabina del avión está AGOTADA (Capacidad == Ocupados)
    private boolean isClaseAgotada(String claseComboName) {
        String key = "ECON";
        if (claseComboName.startsWith("VIP")) {
            key = "VIP";
        } else if (claseComboName.startsWith("Ejecutiva")) {
            key = "EJEC";
        }

        int totalCap = 0;
        int occupiedCount = 0;

        String[] sections = distribucionAvion.split("\\|");
        int currentRow = 1;

        for (String section : sections) {
            String[] parts = section.split(":");
            if (parts.length < 3) continue;
            String sectionName = parts[0];
            String layout = parts[1];
            int numRows = Integer.parseInt(parts[2]);

            String[] layoutParts = layout.split("-");
            int leftCount = Integer.parseInt(layoutParts[0]);
            int rightCount = Integer.parseInt(layoutParts[1]);
            int seatsPerRow = leftCount + rightCount;

            boolean isMatch = false;
            String sNameLower = sectionName.toLowerCase();
            if (key.equals("VIP") && sNameLower.contains("vip")) {
                isMatch = true;
            } else if (key.equals("EJEC") && (sNameLower.contains("ejec") || sNameLower.contains("exec"))) {
                isMatch = true;
            } else if (key.equals("ECON") && (sNameLower.contains("econ") || sNameLower.contains("turista") || sNameLower.contains("standard") || sNameLower.contains("economica"))) {
                isMatch = true;
            }

            if (isMatch) {
                totalCap += seatsPerRow * numRows;
                int startRow = currentRow;
                int endRow = currentRow + numRows - 1;

                for (String seat : occupiedSeatsDb) {
                    int seatRow = extractRowFromSeat(seat);
                    if (seatRow >= startRow && seatRow <= endRow) {
                        occupiedCount++;
                    }
                }
            }
            currentRow += numRows;
        }

        return totalCap > 0 && occupiedCount >= totalCap;
    }

    private static int extractRowFromSeat(String seat) {
        if (seat == null || seat.isEmpty()) return -1;
        StringBuilder sb = new StringBuilder();
        for (char c : seat.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Inicializa la Vista A (Formulario e Historial en Tarjetas)
    private void inicializarVistaA() {
        panelVistaA = new JPanel(new BorderLayout());
        panelVistaA.setBackground(C_FONDO);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_TARJETA);
        header.setPreferredSize(new Dimension(450, 44));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(6, 8, 6, 8)));

        JButton btnVolver = new JButton("← Volver a Cartelera");
        btnVolver.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnVolver.setForeground(C_MUTED);
        btnVolver.setBackground(C_TARJETA);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setOpaque(true);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnVolver.setForeground(C_TEXTO);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnVolver.setForeground(C_MUTED);
            }
        });
        btnVolver.addActionListener(e -> parent.showCartelera());
        header.add(btnVolver, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("Reserva Vuelo " + codigoVuelo);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitle.setForeground(C_TEXTO);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(lblTitle, BorderLayout.CENTER);

        panelVistaA.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_FONDO);
        body.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Form Card
        JPanel cardForm = new JPanel(new GridBagLayout());
        cardForm.setBackground(C_TARJETA);
        cardForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                new EmptyBorder(12, 12, 12, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        cardForm.add(mkLabel("Nombre:"), gbc);
        txtNombre = mkTextField();
        gbc.gridx = 1; gbc.weightx = 1;
        cardForm.add(txtNombre, gbc);

        // Tipo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        cardForm.add(mkLabel("Tipo:"), gbc);
        comboTipo = new JComboBox<>(new String[]{"Adulto", "Niño"});
        comboTipo.setBackground(C_GRIS);
        comboTipo.setForeground(C_TEXTO);
        comboTipo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.weightx = 1;
        cardForm.add(comboTipo, gbc);

        // Clase (ComboBox dinámico según la distribución del avión con validación AGOTADO)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        cardForm.add(mkLabel("Clase:"), gbc);
        
        comboClase = new JComboBox<>();
        String distLower = distribucionAvion.toLowerCase();
        if (distLower.contains("vip")) {
            String item = "VIP (1)";
            if (isClaseAgotada(item)) item += " - AGOTADO";
            comboClase.addItem(item);
        }
        if (distLower.contains("ejec") || distLower.contains("exec")) {
            String item = "Ejecutiva (2)";
            if (isClaseAgotada(item)) item += " - AGOTADO";
            comboClase.addItem(item);
        }
        if (distLower.contains("econ") || distLower.contains("turista") || comboClase.getItemCount() == 0) {
            String item = "Economica (3)";
            if (isClaseAgotada(item)) item += " - AGOTADO";
            comboClase.addItem(item);
        }

        // Configurar ultimoClaseValida al primer elemento no agotado
        for (int i = 0; i < comboClase.getItemCount(); i++) {
            String val = comboClase.getItemAt(i);
            if (!val.contains("AGOTADO")) {
                ultimoClaseValida = val;
                comboClase.setSelectedIndex(i);
                break;
            }
        }
        
        comboClase.setBackground(C_GRIS);
        comboClase.setForeground(C_TEXTO);
        comboClase.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Custom list cell renderer para clases agotadas
        comboClase.setRenderer(new ListCellRenderer<String>() {
            private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    renderer.setText(value);
                    if (value.contains("AGOTADO")) {
                        renderer.setForeground(C_ROJO);
                        Font font = renderer.getFont();
                        java.util.Map<java.awt.font.TextAttribute, Object> attributes = new java.util.HashMap<>(font.getAttributes());
                        attributes.put(java.awt.font.TextAttribute.STRIKETHROUGH, java.awt.font.TextAttribute.STRIKETHROUGH_ON);
                        attributes.put(java.awt.font.TextAttribute.WEIGHT, java.awt.font.TextAttribute.WEIGHT_BOLD);
                        renderer.setFont(font.deriveFont(attributes));
                    } else {
                        renderer.setForeground(C_TEXTO);
                    }
                }
                if (isSelected) {
                    renderer.setBackground(C_AZUL);
                } else {
                    renderer.setBackground(C_GRIS);
                }
                return renderer;
            }
        });

        // Bloqueador de selección agotada
        comboClase.addItemListener(new java.awt.event.ItemListener() {
            private boolean reentrancyGuard = false;

            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                if (reentrancyGuard) return;
                if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    String selected = (String) comboClase.getSelectedItem();
                    if (selected != null) {
                        if (selected.contains("AGOTADO")) {
                            reentrancyGuard = true;
                            JOptionPane.showMessageDialog(PanelReserva.this,
                                    "No quedan asientos disponibles en esta clase.",
                                    "Clase Agotada", JOptionPane.WARNING_MESSAGE);
                            if (ultimoClaseValida != null) {
                                comboClase.setSelectedItem(ultimoClaseValida);
                            } else {
                                for (int i = 0; i < comboClase.getItemCount(); i++) {
                                    String item = comboClase.getItemAt(i);
                                    if (!item.contains("AGOTADO")) {
                                        comboClase.setSelectedIndex(i);
                                        ultimoClaseValida = item;
                                        break;
                                    }
                                }
                            }
                            reentrancyGuard = false;
                        } else {
                            ultimoClaseValida = selected;
                        }
                    }
                }
            }
        });

        gbc.gridx = 1; gbc.weightx = 1;
        cardForm.add(comboClase, gbc);

        // Silla de Ruedas
        chkSilla = new JCheckBox("Asistencia Especial (Silla)");
        chkSilla.setBackground(C_TARJETA);
        chkSilla.setForeground(C_TEXTO);
        chkSilla.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1;
        cardForm.add(chkSilla, gbc);

        // Botón agregar pasajero
        JButton btnAgregar = new JButton("+ Agregar Pasajero");
        aplicarEstiloBoton(btnAgregar, C_AZUL, 12);
        btnAgregar.setPreferredSize(new Dimension(200, 32));
        btnAgregar.addActionListener(e -> agregarPasajero());

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 6, 2, 6);
        cardForm.add(btnAgregar, gbc);

        body.add(cardForm);
        body.add(Box.createVerticalStrut(10));

        // Título de la lista
        JPanel pTitleList = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pTitleList.setBackground(C_FONDO);
        JLabel lblList = new JLabel("Grupo de Pasajeros");
        lblList.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblList.setForeground(C_TEXTO);
        pTitleList.add(lblList);
        body.add(pTitleList);
        body.add(Box.createVerticalStrut(6));

        // Contenedor de Tarjetas (Lista vertical)
        panelListaPasajeros = new JPanel();
        panelListaPasajeros.setLayout(new BoxLayout(panelListaPasajeros, BoxLayout.Y_AXIS));
        panelListaPasajeros.setBackground(C_FONDO);

        JScrollPane scroll = new JScrollPane(panelListaPasajeros);
        scroll.getViewport().setBackground(C_FONDO);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(418, 380));
        body.add(scroll);

        panelVistaA.add(body, BorderLayout.CENTER);

        // Botón Confirmar Final
        JPanel pConfirm = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        pConfirm.setBackground(C_FONDO);
        pConfirm.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDE));

        JButton btnConfirmar = new JButton("CONFIRMAR RESERVA");
        aplicarEstiloBoton(btnConfirmar, C_VERDE, 13);
        btnConfirmar.setPreferredSize(new Dimension(320, 42));
        btnConfirmar.addActionListener(e -> confirmarReserva());
        pConfirm.add(btnConfirmar);

        panelVistaA.add(pConfirm, BorderLayout.SOUTH);
    }

    // Inicializa la Vista B (Selección gráfica de asientos)
    private void inicializarVistaB() {
        panelVistaB = new JPanel(new BorderLayout());
        panelVistaB.setBackground(C_FONDO);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_TARJETA);
        header.setPreferredSize(new Dimension(450, 44));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(6, 8, 6, 8)));

        JButton btnVolver = new JButton("← Cancelar");
        btnVolver.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnVolver.setForeground(C_MUTED);
        btnVolver.setBackground(C_TARJETA);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setOpaque(true);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnVolver.setForeground(C_TEXTO);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnVolver.setForeground(C_MUTED);
            }
        });
        btnVolver.addActionListener(e -> cardLayoutInterno.show(this, "VISTA_A"));
        header.add(btnVolver, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("Elegir Asiento");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitle.setForeground(C_TEXTO);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(lblTitle, BorderLayout.CENTER);

        panelVistaB.add(header, BorderLayout.NORTH);

        // Contenedor del mapa de cabina
        panelContenedorMapa = new JPanel();
        panelContenedorMapa.setLayout(new BoxLayout(panelContenedorMapa, BoxLayout.Y_AXIS));
        panelContenedorMapa.setBackground(C_TARJETA);
        panelContenedorMapa.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollMap = new JScrollPane(panelContenedorMapa);
        scrollMap.getViewport().setBackground(C_TARJETA);
        scrollMap.setBorder(BorderFactory.createEmptyBorder());

        panelVistaB.add(scrollMap, BorderLayout.CENTER);

        // Footer Informativo
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        footer.setBackground(C_FONDO);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDE));

        JLabel lblHint = new JLabel("Rojo: Ocupado  |  Verde: Disponible  |  Gris: Clase No Permitida");
        lblHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblHint.setForeground(C_MUTED);
        footer.add(lblHint);

        panelVistaB.add(footer, BorderLayout.SOUTH);
    }

    // Actualiza la visualización de las tarjetas en la Vista A
    private void actualizarListaPasajeros() {
        panelListaPasajeros.removeAll();

        for (int i = 0; i < grupoFamiliar.size(); i++) {
            final int index = i;
            PasajeroTemporal p = grupoFamiliar.get(i);

            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBackground(C_TARJETA);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_BORDE, 1),
                    new EmptyBorder(8, 12, 8, 12)));
            card.setMaximumSize(new Dimension(418, 60));

            // Información del pasajero
            JPanel pDetails = new JPanel();
            pDetails.setLayout(new BoxLayout(pDetails, BoxLayout.Y_AXIS));
            pDetails.setBackground(C_TARJETA);

            String claseLimpia = obtenerNombreLimpioClase(p.clase);
            String tituloPasajero = String.format("%s (%s - %s)", p.nombre, p.tipo, claseLimpia);
            
            JLabel lblNombre = new JLabel(tituloPasajero);
            lblNombre.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblNombre.setForeground(C_TEXTO);
            pDetails.add(lblNombre);

            String desc = p.sillaRuedas ? "Requerimiento: Silla de Ruedas" : "Sin requerimientos especiales";
            JLabel lblDesc = new JLabel(desc);
            lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblDesc.setForeground(C_MUTED);
            pDetails.add(lblDesc);

            card.add(pDetails, BorderLayout.CENTER);

            // Botones de acción en la tarjeta
            JPanel pActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            pActions.setBackground(C_TARJETA);

            JButton btnSeat = new JButton();
            btnSeat.setFont(new Font("SansSerif", Font.BOLD, 11));
            btnSeat.setFocusPainted(false);
            btnSeat.setBorderPainted(false);
            btnSeat.setOpaque(true);
            btnSeat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSeat.setPreferredSize(new Dimension(140, 28));

            if (p.asiento == null) {
                btnSeat.setText("🪑 Seleccionar");
                btnSeat.setBackground(C_AZUL);
                btnSeat.setForeground(C_TEXTO);
            } else {
                btnSeat.setText("✅ Asiento: " + p.asiento);
                btnSeat.setBackground(C_VERDE);
                btnSeat.setForeground(C_TEXTO);
            }
            btnSeat.addActionListener(e -> {
                pasajeroEditandoIndex = index;
                irAElegirAsiento();
            });
            pActions.add(btnSeat);

            JButton btnDel = new JButton("X");
            btnDel.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnDel.setBackground(C_GRIS);
            btnDel.setForeground(C_MUTED);
            btnDel.setFocusPainted(false);
            btnDel.setBorderPainted(false);
            btnDel.setOpaque(true);
            btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDel.setPreferredSize(new Dimension(30, 28));
            btnDel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    btnDel.setBackground(C_ROJO);
                    btnDel.setForeground(C_TEXTO);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    btnDel.setBackground(C_GRIS);
                    btnDel.setForeground(C_MUTED);
                }
            });
            btnDel.addActionListener(e -> {
                grupoFamiliar.remove(index);
                actualizarListaPasajeros();
            });
            pActions.add(btnDel);

            card.add(pActions, BorderLayout.EAST);

            panelListaPasajeros.add(card);
            panelListaPasajeros.add(Box.createVerticalStrut(8));
        }

        panelListaPasajeros.add(Box.createVerticalGlue());
        panelListaPasajeros.revalidate();
        panelListaPasajeros.repaint();
    }

    private String obtenerNombreLimpioClase(String claseCombo) {
        if (claseCombo == null) return "Económica";
        if (claseCombo.startsWith("VIP")) return "VIP";
        if (claseCombo.startsWith("Ejecutiva")) return "Ejecutiva";
        return "Económica";
    }

    private void irAElegirAsiento() {
        inicializarMapaAsientos();
        cardLayoutInterno.show(this, "VISTA_B");
    }

    // Genera dinámicamente los botones del mapa de cabina
    private void inicializarMapaAsientos() {
        panelContenedorMapa.removeAll();

        Set<String> occupiedSeats = new HashSet<>();
        String distribucion = distribucionAvion; 

        // Consulta de asientos ocupados
        try (Connection conn = ConexionBD.getConnection()) {
            String sqlOccupied = "SELECT numAsiento FROM pasajero WHERE matricula = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlOccupied)) {
                ps.setString(1, matricula);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        occupiedSeats.add(rs.getString("numAsiento"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error al cargar la ocupacion de cabina", e);
            JOptionPane.showMessageDialog(this,
                    "Error al cargar la ocupación de cabina:\n" + e.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Agregar también los asientos ocupados por el grupo familiar en memoria
        for (int i = 0; i < grupoFamiliar.size(); i++) {
            if (i == pasajeroEditandoIndex) continue;
            String seat = grupoFamiliar.get(i).asiento;
            if (seat != null) {
                occupiedSeats.add(seat);
            }
        }

        // Resolver sección permitida del avión según la clase del pasajero actual
        String pClass = grupoFamiliar.get(pasajeroEditandoIndex).clase;
        boolean planeHasVip = distribucion.toLowerCase().contains("vip");
        boolean planeHasEjec = distribucion.toLowerCase().contains("ejec") || distribucion.toLowerCase().contains("exec");
        boolean planeHasEcon = distribucion.toLowerCase().contains("econ") || distribucion.toLowerCase().contains("turista") || distribucion.toLowerCase().contains("standard");

        String allowedSection = "ECON";
        if (pClass.toLowerCase().contains("vip") && planeHasVip) {
            allowedSection = "VIP";
        } else if (pClass.toLowerCase().contains("ejecutiva") && planeHasEjec) {
            allowedSection = "EJEC";
        } else if (pClass.toLowerCase().contains("ejecutiva") && !planeHasEjec && planeHasEcon) {
            allowedSection = "ECON"; 
        } else if (pClass.toLowerCase().contains("economica") && planeHasEcon) {
            allowedSection = "ECON";
        }

        // Renderizado del layout de cabina
        String[] classes = distribucion.split("\\|");
        int currentRow = 1;

        for (String cls : classes) {
            String[] parts = cls.split(":");
            if (parts.length < 3) continue;
            String className = parts[0];
            String layout = parts[1];
            int numRows = Integer.parseInt(parts[2]);

            String[] layoutParts = layout.split("-");
            int leftCount = Integer.parseInt(layoutParts[0]);
            int rightCount = Integer.parseInt(layoutParts[1]);

            boolean sectionAllowed = false;
            String lowerName = className.toLowerCase();
            if (allowedSection.equals("VIP") && lowerName.contains("vip")) {
                sectionAllowed = true;
            } else if (allowedSection.equals("EJEC") && (lowerName.contains("ejec") || lowerName.contains("exec"))) {
                sectionAllowed = true;
            } else if (allowedSection.equals("ECON") && (lowerName.contains("econ") || lowerName.contains("turista") || lowerName.contains("standard"))) {
                sectionAllowed = true;
            }

            // Cabecera de la sección
            JPanel headerClase = new JPanel(new FlowLayout(FlowLayout.CENTER));
            headerClase.setBackground(C_TARJETA);
            JLabel lblClaseName = new JLabel("--- Sección " + className + " ---");
            lblClaseName.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblClaseName.setForeground(sectionAllowed ? C_AZUL : C_MUTED);
            headerClase.add(lblClaseName);
            panelContenedorMapa.add(headerClase);

            for (int r = 0; r < numRows; r++) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
                rowPanel.setBackground(C_TARJETA);

                JLabel lblRow = new JLabel(String.format("%02d", currentRow));
                lblRow.setFont(new Font("SansSerif", Font.BOLD, 11));
                lblRow.setForeground(C_MUTED);
                rowPanel.add(lblRow);

                char colLetter = 'A';

                for (int c = 0; c < leftCount; c++) {
                    String seatName = currentRow + "" + colLetter;
                    rowPanel.add(crearBotonAsiento(seatName, sectionAllowed, occupiedSeats));
                    colLetter++;
                }

                rowPanel.add(Box.createHorizontalStrut(28));

                for (int c = 0; c < rightCount; c++) {
                    String seatName = currentRow + "" + colLetter;
                    rowPanel.add(crearBotonAsiento(seatName, sectionAllowed, occupiedSeats));
                    colLetter++;
                }

                panelContenedorMapa.add(rowPanel);
                currentRow++;
            }
        }

        panelContenedorMapa.revalidate();
        panelContenedorMapa.repaint();
    }

    private JToggleButton crearBotonAsiento(String seatName, boolean sectionAllowed, Set<String> occupiedSeats) {
        JToggleButton btn = new JToggleButton(seatName);
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.setPreferredSize(new Dimension(48, 32));
        btn.setFocusPainted(false);

        if (!sectionAllowed) {
            btn.setBackground(C_GRIS);
            btn.setForeground(C_MUTED);
            btn.setBorder(BorderFactory.createLineBorder(C_BORDE, 1));
            btn.setEnabled(false);
        } else if (occupiedSeats.contains(seatName)) {
            btn.setBackground(C_ROJO);
            btn.setForeground(C_TEXTO);
            btn.setBorder(BorderFactory.createLineBorder(C_BORDE, 1));
            btn.setSelected(true);
            btn.setEnabled(false);
        } else {
            btn.setBackground(C_VERDE);
            btn.setForeground(C_TEXTO);
            btn.setBorder(BorderFactory.createLineBorder(C_AZUL, 1));
            btn.setEnabled(true);
            btn.addActionListener(e -> seleccionarAsiento(seatName));
        }
        return btn;
    }

    private void seleccionarAsiento(String seatName) {
        if (pasajeroEditandoIndex >= 0 && pasajeroEditandoIndex < grupoFamiliar.size()) {
            grupoFamiliar.get(pasajeroEditandoIndex).asiento = seatName;
        }
        actualizarListaPasajeros();
        cardLayoutInterno.show(this, "VISTA_A");
    }

    private void agregarPasajero() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa el nombre del pasajero.", "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipo       = (String) comboTipo.getSelectedItem();
        String clase      = (String) comboClase.getSelectedItem();
        boolean sillaVal  = chkSilla.isSelected();

        grupoFamiliar.add(new PasajeroTemporal(nombre, tipo, clase, sillaVal));
        actualizarListaPasajeros();

        // Limpiar campos formulario
        txtNombre.setText("");
        comboTipo.setSelectedIndex(0);
        
        // Seleccionar primer elemento válido en el combo
        if (ultimoClaseValida != null) {
            comboClase.setSelectedItem(ultimoClaseValida);
        }
        chkSilla.setSelected(false);
        txtNombre.requestFocusInWindow();
    }

    // Metodo eliminarPasajero removido por no ser utilizado.

    private void confirmarReserva() {
        if (grupoFamiliar.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Agrega al menos un pasajero antes de confirmar.",
                    "Grupo vacio", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (PasajeroTemporal p : grupoFamiliar) {
            if (p.asiento == null) {
                JOptionPane.showMessageDialog(this,
                        "Por favor selecciona un asiento para cada pasajero del grupo.",
                        "Asiento pendiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String pnr = generarPNR();

        final String sql =
            "INSERT INTO pasajero " +
            "  (nombre, numAsiento, nivelPrioridad, timestampLlegada, " +
            "   matricula, pnr, sillaRuedas, upgrade) " +
            "VALUES (?, ?, ?, NULL, ?, ?, ?, 0)";

        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PasajeroTemporal p : grupoFamiliar) {
                    ps.setString(1, p.nombre);
                    ps.setString(2, p.asiento);
                    ps.setInt   (3, extraerPrioridad(p.clase));
                    ps.setString(4, matricula);
                    ps.setString(5, pnr);
                    ps.setBoolean(6, p.sillaRuedas);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }

            String mensaje = """
                    Reserva completada!
                    
                    Vuelo:     %s
                    Aeronave:  %s
                    Pasajeros: %d
                    
                    Codigo PNR:  %s
                    
                    Presenta este codigo en el mostrador de abordaje.""".formatted(
                            codigoVuelo, matricula, grupoFamiliar.size(), pnr);

            JOptionPane.showMessageDialog(this,
                    mensaje,
                    "Reserva Confirmada",
                    JOptionPane.INFORMATION_MESSAGE);

            parent.showCartelera();

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException r) {
                    LOGGER.log(java.util.logging.Level.WARNING, "Error al hacer rollback de la transaccion", r);
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Error al guardar la reserva:\n\n" + ex.getMessage(),
                    "Error de BD", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(java.util.logging.Level.WARNING, "Error al cerrar la conexion", e);
                }
            }
        }
    }

    private static String generarPNR() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("SQ-");
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private static int extraerPrioridad(String claseTexto) {
        if (claseTexto != null) {
            if (claseTexto.startsWith("VIP"))        return 1;
            if (claseTexto.startsWith("Ejecutiva"))  return 2;
        }
        return 3;
    }

    private JLabel mkLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(C_MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JTextField mkTextField() {
        JTextField f = new JTextField();
        f.setBackground(C_GRIS);
        f.setForeground(C_TEXTO);
        f.setCaretColor(C_TEXTO);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                new EmptyBorder(2, 6, 2, 6)));
        return f;
    }

    private void aplicarEstiloBoton(JButton btn, Color bg, int fontSize) {
        btn.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        btn.setBackground(bg);
        btn.setForeground(C_TEXTO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
