package skyq.view;

import skyq.dao.AvionDAO;
import skyq.dao.EquipajeDAO;
import skyq.dao.PasajeroDAO;
import skyq.logic.ColaAbordaje;
import skyq.logic.DesembarqueManager;
import skyq.model.Avion;
import skyq.model.Equipaje;
import skyq.model.Pasajero;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class PanelCheckIn extends JPanel {

    private JComboBox<String> comboVuelosFlota;
    private JPanel panelCentralWorkspace;
    private CardLayout cardLayoutModos;

    // Campos del flujo Check-In
    private JTextField txtNombrePasajero, txtPesoEquipaje;
    private JComboBox<String> comboPrioridad;
    private JLabel lblAsientoAsignado;
    private JPanel contenedorDinamicoCabina;
    private String asientoElegido = "";

    // Componentes de las tablas de Abordaje/Desembarque
    private DefaultTableModel modeloTablaFlujo;
    private JTable tablaFlujoOperaciones;
    private JLabel lblTituloSeccionCambiante;

    private final AvionDAO avionDAO = new AvionDAO();
    private final PasajeroDAO pasajeroDAO = new PasajeroDAO();
    private final EquipajeDAO equipajeDAO = new EquipajeDAO();

    public PanelCheckIn() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(20, 20));
        initComponents();
        recargarVuelosDesdeFlota();
    }

    private void initComponents() {
        // ==========================================
        // 🛫 NORTE: BARRA SUPERIOR SELECCIÓN DE VUELO
        // ==========================================
        JPanel panelNorteSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panelNorteSeleccion.setBackground(EstiloUI.FONDO_TARJETA);
        panelNorteSeleccion.setBorder(EstiloUI.BORDE_COMPONENTE);

        JLabel lblSelector = new JLabel("OPERANDO FLOTA EN CONTEXTO:");
        lblSelector.setForeground(Color.CYAN);
        lblSelector.setFont(EstiloUI.FUENTE_SUBTITULO);

        comboVuelosFlota = new JComboBox<>();
        comboVuelosFlota.setPreferredSize(new Dimension(280, 32));
        comboVuelosFlota.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboVuelosFlota.setForeground(EstiloUI.TEXTO_BLANCO);

        panelNorteSeleccion.add(lblSelector);
        panelNorteSeleccion.add(comboVuelosFlota);
        add(panelNorteSeleccion, BorderLayout.NORTH);

        // ==========================================
        // 🎛️ OESTE: BARRA LATERAL SUB-NVEGACIÓN (FIGMA)
        // ==========================================
        JPanel sidebarModos = new JPanel(new GridLayout(3, 1, 0, 15));
        sidebarModos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        sidebarModos.setPreferredSize(new Dimension(180, 500));

        JButton btnModoCheckIn = crearBotonSidebar("1. Registro / Check-In", true);
        JButton btnModoAbordaje = crearBotonSidebar("2. Cola de Abordaje", false);
        JButton btnModoDesembarque = crearBotonSidebar("3. Desembarque", false);

        sidebarModos.add(btnModoCheckIn);
        sidebarModos.add(btnModoAbordaje);
        sidebarModos.add(btnModoDesembarque);
        add(sidebarModos, BorderLayout.WEST);

        // ==========================================
        // 🖥️ CENTRO: ESPACIO DE TRABAJO (CARDLAYOUT)
        // ==========================================
        cardLayoutModos = new CardLayout();
        panelCentralWorkspace = new JPanel(cardLayoutModos);
        panelCentralWorkspace.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        panelCentralWorkspace.add(construirFormularioCheckInVisual(), "VISTA_CHECKIN");
        panelCentralWorkspace.add(construirListadosTabularesDeVuelo(), "VISTA_TABLAS");

        add(panelCentralWorkspace, BorderLayout.CENTER);

        // Enlace interactivo de botones del menú lateral
        btnModoCheckIn.addActionListener(e -> {
            alternarEstiloMenu(btnModoCheckIn, btnModoAbordaje, btnModoDesembarque);
            cardLayoutModos.show(panelCentralWorkspace, "VISTA_CHECKIN");
            refrescarPlanoAvion();
        });

        btnModoAbordaje.addActionListener(e -> {
            alternarEstiloMenu(btnModoAbordaje, btnModoCheckIn, btnModoDesembarque);
            lblTituloSeccionCambiante.setText("COLA DE ABORDAJE ACTIVA - ENFOQUE: COMERCIAL + FIFO");
            cardLayoutModos.show(panelCentralWorkspace, "VISTA_TABLAS");
            calcularColaAbordajeDinamica();
        });

        btnModoDesembarque.addActionListener(e -> {
            alternarEstiloMenu(btnModoDesembarque, btnModoCheckIn, btnModoAbordaje);
            lblTituloSeccionCambiante.setText("SECUENCIA CRÍTICA DE DESEMBARQUE - PRIORIDAD: ADELANTE HACIA ATRÁS");
            cardLayoutModos.show(panelCentralWorkspace, "VISTA_TABLAS");
            calcularDesembarqueDinamico();
        });

        comboVuelosFlota.addActionListener(e -> refrescarPlanoAvion());
    }

    private JPanel construirFormularioCheckInVisual() {
        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // Tarjeta del Formulario
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(EstiloUI.FONDO_TARJETA);
        formCard.setPreferredSize(new Dimension(360, 500));
        formCard.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(20, 15, 20, 15)));

        txtNombrePasajero = crearFieldEstilizado();
        txtPesoEquipaje = crearFieldEstilizado();
        comboPrioridad = new JComboBox<>(new String[]{"1", "2", "3"});
        comboPrioridad.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); comboPrioridad.setForeground(EstiloUI.TEXTO_BLANCO);

        lblAsientoAsignado = new JLabel("[ Seleccione en el Mapa ]");
        lblAsientoAsignado.setForeground(Color.CYAN); lblAsientoAsignado.setFont(EstiloUI.FUENTE_SUBTITULO);

        JButton btnEnviarCheckIn = new JButton("EMITIR PASE DE ABORDAJE");
        btnEnviarCheckIn.setBackground(EstiloUI.AZUL_ACCENT); btnEnviarCheckIn.setForeground(EstiloUI.TEXTO_BLANCO); btnEnviarCheckIn.setBorderPainted(false); btnEnviarCheckIn.setFont(EstiloUI.FUENTE_COMPONENTE);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 5, 10, 5); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel lblTitle = new JLabel("REGISTRO DE PASAJEROS"); lblTitle.setForeground(EstiloUI.TEXTO_BLANCO); lblTitle.setFont(EstiloUI.FUENTE_TITULO); formCard.add(lblTitle, g);

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 1; formCard.add(new JLabel("Nombre Pasajero:"), g); g.gridx = 1; formCard.add(txtNombrePasajero, g);
        g.gridx = 0; g.gridy = 2; formCard.add(new JLabel("Categoría Vuelo:"), g); g.gridx = 1; formCard.add(comboPrioridad, g);
        g.gridx = 0; g.gridy = 3; formCard.add(new JLabel("Butaca Asignada:"), g); g.gridx = 1; formCard.add(lblAsientoAsignado, g);
        g.gridx = 0; g.gridy = 4; formCard.add(new JLabel("Peso Maleta (kg):"), g); g.gridx = 1; formCard.add(txtPesoEquipaje, g);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2; g.insets = new Insets(25, 5, 5, 5); formCard.add(btnEnviarCheckIn, g);

        for (Component comp : formCard.getComponents()) {
            if (comp instanceof JLabel && comp != lblTitle && comp != lblAsientoAsignado) {
                comp.setForeground(EstiloUI.TEXTO_MUTED); comp.setFont(EstiloUI.FUENTE_LABEL);
            }
        }
        splitPanel.add(formCard, BorderLayout.WEST);

        // Tarjeta del Mapa de Asientos
        contenedorDinamicoCabina = new JPanel(new BorderLayout());
        contenedorDinamicoCabina.setBackground(EstiloUI.FONDO_TARJETA);
        contenedorDinamicoCabina.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));
        splitPanel.add(contenedorDinamicoCabina, BorderLayout.CENTER);

        comboPrioridad.addActionListener(e -> refrescarPlanoAvion());
        btnEnviarCheckIn.addActionListener(e -> procesarGuardadoCheckIn());

        return splitPanel;
    }

    private JPanel construirListadosTabularesDeVuelo() {
        JPanel panelContenedorTabla = new JPanel(new BorderLayout(10, 12));
        panelContenedorTabla.setBackground(EstiloUI.FONDO_TARJETA);
        panelContenedorTabla.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        lblTituloSeccionCambiante = new JLabel("MONITOR OPERATIVO DE CONTROL");
        lblTituloSeccionCambiante.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTituloSeccionCambiante.setFont(EstiloUI.FUENTE_SUBTITULO);
        panelContenedorTabla.add(lblTituloSeccionCambiante, BorderLayout.NORTH);

        modeloTablaFlujo = new DefaultTableModel(new Object[]{"ID PASAJERO", "NOMBRE DEL PASAJERO", "ASIENTO", "PRIORIDAD", "TIMESTAMP REGISTRO"}, 0);
        tablaFlujoOperaciones = new JTable(modeloTablaFlujo);
        tablaFlujoOperaciones.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaFlujoOperaciones.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaFlujoOperaciones.setGridColor(new Color(48, 54, 61));

        JScrollPane scrollPane = new JScrollPane(tablaFlujoOperaciones);
        scrollPane.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelContenedorTabla.add(scrollPane, BorderLayout.CENTER);

        return panelContenedorTabla;
    }

    private void recargarVuelosDesdeFlota() {
        comboVuelosFlota.removeAllItems();
        List<Avion> flota = avionDAO.obtenerAvionesFlota();
        for (Avion a : flota) {
            comboVuelosFlota.addItem(a.getMatricula() + " - " + a.getModelo());
        }
        if (flota.isEmpty()) {
            comboVuelosFlota.addItem("HC-BXA - Configuración por Defecto");
        }
    }

    private String obtenerMatriculaActiva() {
        String item = (String) comboVuelosFlota.getSelectedItem();
        if (item == null || item.contains("Defecto")) return "HC-BXA";
        return item.split(" - ")[0];
    }

    private void refrescarPlanoAvion() {
        String matricula = obtenerMatriculaActiva();
        contenedorDinamicoCabina.removeAll();

        // Se inicializa el mapa enviando de forma contextual la matrícula actual
        MapaAsientosPanel planoRealTime = new MapaAsientosPanel(matricula, codigoAsiento -> {
            // 🔥 CORREGIDO: Llamada segura al método unificado por matrícula y vuelo
            if (pasajeroDAO.verificarAsientoOcupadoEnVuelo(codigoAsiento, matricula)) {
                JOptionPane.showMessageDialog(this, "Esta butaca ya está ocupada en este vuelo.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            asientoElegido = codigoAsiento;
            lblAsientoAsignado.setText("Asiento Seleccionado: " + codigoAsiento);
        });

        contenedorDinamicoCabina.add(planoRealTime, BorderLayout.CENTER);
        contenedorDinamicoCabina.revalidate();
        contenedorDinamicoCabina.repaint();
    }

    private void procesarGuardadoCheckIn() {
        String nombre = txtNombrePasajero.getText().trim();
        String pesoTexto = txtPesoEquipaje.getText().trim();
        String prioridadTexto = (String) comboPrioridad.getSelectedItem();
        String matricula = obtenerMatriculaActiva();

        if (nombre.isEmpty() || asientoElegido.isEmpty() || pesoTexto.isEmpty() || prioridadTexto == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos del pasajero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int prioridad = Integer.parseInt(prioridadTexto);
        double peso = Double.parseDouble(pesoTexto);

        double pesoMaximo = (prioridad == 1) ? 32.0 : (prioridad == 2) ? 23.0 : 15.0;
        if (peso > pesoMaximo) {
            JOptionPane.showMessageDialog(this, "El equipaje excede el peso permitido (" + pesoMaximo + "kg) para prioridad " + prioridad, "Sobrepeso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔥 CORREGIDO: Llamada limpia al constructor extendido de 6 parámetros
        Pasajero pasajero = new Pasajero(0, nombre, asientoElegido, prioridad, LocalDateTime.now(), matricula);
        int idPasajero = pasajeroDAO.insertarPasajeroYObtenerId(pasajero);

        if (idPasajero > 0) {
            Equipaje equipaje = new Equipaje(0, idPasajero, peso, "Aceptado");
            if (equipajeDAO.registrarEquipaje(equipaje)) {
                JOptionPane.showMessageDialog(this, "¡Pase de abordar emitido con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtNombrePasajero.setText(""); txtPesoEquipaje.setText(""); asientoElegido = "";
                lblAsientoAsignado.setText("[ Seleccione en el Mapa ]");
                refrescarPlanoAvion();
            }
        }
    }

    private void calcularColaAbordajeDinamica() {
        modeloTablaFlujo.setRowCount(0);
        // 🔥 CORREGIDO: Se filtra la cola consumiendo únicamente el contexto del vuelo seleccionado
        List<Pasajero> base = pasajeroDAO.obtenerPasajerosPorVuelo(obtenerMatriculaActiva());
        List<Pasajero> ordenados = new ColaAbordaje().organizarPasajeros(base);

        for (Pasajero p : ordenados) {
            modeloTablaFlujo.addRow(new Object[]{p.getIdPasajero(), p.getNombre(), p.getNumAsiento(), p.getNivelPrioridad(), p.getTimestampLlegada()});
        }
    }

    private void calcularDesembarqueDinamico() {
        modeloTablaFlujo.setRowCount(0);
        // 🔥 CORREGIDO: Se filtra el desembarque consumiendo únicamente el contexto del vuelo seleccionado
        List<Pasajero> base = pasajeroDAO.obtenerPasajerosPorVuelo(obtenerMatriculaActiva());
        List<Pasajero> ordenados = new DesembarqueManager().ordenarPorAsiento(base);

        for (Pasajero p : ordenados) {
            modeloTablaFlujo.addRow(new Object[]{p.getIdPasajero(), p.getNombre(), p.getNumAsiento(), p.getNivelPrioridad(), p.getTimestampLlegada()});
        }
    }

    private JTextField crearFieldEstilizado() {
        JTextField f = new JTextField(15);
        f.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); f.setForeground(EstiloUI.TEXTO_BLANCO); f.setCaretColor(EstiloUI.TEXTO_BLANCO); f.setBorder(EstiloUI.BORDE_COMPONENTE);
        return f;
    }

    private JButton crearBotonSidebar(String texto, boolean activo) {
        JButton b = new JButton(texto);
        b.setPreferredSize(new Dimension(165, 42)); b.setFont(EstiloUI.FUENTE_COMPONENTE); b.setFocusPainted(false); b.setBorderPainted(false);
        if (activo) {
            b.setBackground(EstiloUI.AZUL_ACCENT); b.setForeground(EstiloUI.TEXTO_BLANCO);
        } else {
            b.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b.setForeground(EstiloUI.TEXTO_MUTED);
        }
        return b;
    }

    private void alternarEstiloMenu(JButton sel, JButton b2, JButton b3) {
        sel.setBackground(EstiloUI.AZUL_ACCENT); sel.setForeground(EstiloUI.TEXTO_BLANCO);
        b2.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b2.setForeground(EstiloUI.TEXTO_MUTED);
        b3.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b3.setForeground(EstiloUI.TEXTO_MUTED);
    }
}