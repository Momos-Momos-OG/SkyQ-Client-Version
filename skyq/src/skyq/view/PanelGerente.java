package skyq.view;

import skyq.dao.AvionDAO;
import skyq.dao.PilotoDAO;
import skyq.model.Avion;
import skyq.model.Piloto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PanelGerente extends JPanel {

    private CardLayout cardNavigator;
    private JPanel mainDynamicContainer;
    private PanelRadarView panelRadarView;

    private JButton btnRadarView, btnRegistroView;
    private JTextField txtMatricula, txtModelo, txtCapacidad;
    private JComboBox<String> comboEstado;
    private JTextArea txtDescripcion;
    private JButton btnRegistrarAvion;

    // Elementos del CRUD de Pilotos
    private JTextArea areaPilotosUI;
    private final PilotoDAO pilotoDAO = new PilotoDAO();

    public PanelGerente() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // --- COLUMNA DE BARRA LATERAL (FIGMA) ---
        JPanel sidebarMenu = new JPanel();
        sidebarMenu.setBackground(EstiloUI.FONDO_TARJETA);
        sidebarMenu.setPreferredSize(new Dimension(180, 800));
        sidebarMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(48, 54, 61)));
        sidebarMenu.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 25));

        btnRadarView = new JButton("Radar");
        btnRegistroView = new JButton("Registro");

        aplicarEstiloBotonMenu(btnRadarView, true);
        aplicarEstiloBotonMenu(btnRegistroView, false);

        sidebarMenu.add(btnRadarView);
        sidebarMenu.add(btnRegistroView);
        add(sidebarMenu, BorderLayout.WEST);

        // --- CONTENEDOR DINÁMICO ---
        cardNavigator = new CardLayout();
        mainDynamicContainer = new JPanel(cardNavigator);
        mainDynamicContainer.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        panelRadarView = new PanelRadarView();
        mainDynamicContainer.add(panelRadarView, "PANTALLA_RADAR");
        mainDynamicContainer.add(construirDashboardGerente(), "PANTALLA_DASHBOARD");

        add(mainDynamicContainer, BorderLayout.CENTER);

        btnRadarView.addActionListener(e -> {
            aplicarEstiloBotonMenu(btnRadarView, true);
            aplicarEstiloBotonMenu(btnRegistroView, false);
            panelRadarView.recargarDatosAviones(); // Sincroniza el radar al abrirlo
            cardNavigator.show(mainDynamicContainer, "PANTALLA_RADAR");
        });

        btnRegistroView.addActionListener(e -> {
            aplicarEstiloBotonMenu(btnRadarView, false);
            aplicarEstiloBotonMenu(btnRegistroView, true);
            cardNavigator.show(mainDynamicContainer, "PANTALLA_DASHBOARD");
        });
    }

    private JPanel construirDashboardGerente() {
        JPanel dashboard = new JPanel(new GridLayout(1, 3, 20, 0));
        dashboard.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        dashboard.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 🏛️ CARD 1: ENTRADA DE DATOS DE AERONAVE
        JPanel cardAeronave = new JPanel(new GridBagLayout());
        cardAeronave.setBackground(EstiloUI.FONDO_TARJETA);
        cardAeronave.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        JPanel fotoPlaceholder = new JPanel(new BorderLayout());
        fotoPlaceholder.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        fotoPlaceholder.setPreferredSize(new Dimension(110, 110));
        fotoPlaceholder.setBorder(EstiloUI.BORDE_COMPONENTE);
        JLabel lblPlus = new JLabel("+", SwingConstants.CENTER);
        lblPlus.setForeground(EstiloUI.TEXTO_MUTED); lblPlus.setFont(new Font("SansSerif", Font.BOLD, 26));
        fotoPlaceholder.add(lblPlus, BorderLayout.CENTER);

        txtMatricula = crearCampoTexto();
        txtModelo = crearCampoTexto();
        txtCapacidad = crearCampoTexto();
        txtDescripcion = new JTextArea(3, 10);
        txtDescripcion.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); txtDescripcion.setForeground(EstiloUI.TEXTO_BLANCO); txtDescripcion.setBorder(EstiloUI.BORDE_COMPONENTE); txtDescripcion.setLineWrap(true);

        comboEstado = new JComboBox<>(new String[]{"Disponible", "En mantenimiento", "Fuera de servicio"});
        comboEstado.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); comboEstado.setForeground(EstiloUI.TEXTO_BLANCO); comboEstado.setBorder(EstiloUI.BORDE_COMPONENTE);

        btnRegistrarAvion = new JButton("REGISTRAR EN FLOTA");
        btnRegistrarAvion.setBackground(EstiloUI.AZUL_ACCENT); btnRegistrarAvion.setForeground(EstiloUI.TEXTO_BLANCO); btnRegistrarAvion.setFont(EstiloUI.FUENTE_COMPONENTE); btnRegistrarAvion.setBorderPainted(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.fill = GridBagConstraints.NONE; cardAeronave.add(fotoPlaceholder, g);
        g.fill = GridBagConstraints.HORIZONTAL; g.gridwidth = 1;
        g.gridx = 0; g.gridy = 1; cardAeronave.add(crearLabelMuted("Matrícula:"), g); g.gridx = 1; cardAeronave.add(txtMatricula, g);
        g.gridx = 0; g.gridy = 2; cardAeronave.add(crearLabelMuted("Modelo:"), g); g.gridx = 1; cardAeronave.add(txtModelo, g);
        g.gridx = 0; g.gridy = 3; cardAeronave.add(crearLabelMuted("Capacidad:"), g); g.gridx = 1; cardAeronave.add(txtCapacidad, g);
        g.gridx = 0; g.gridy = 4; cardAeronave.add(crearLabelMuted("Estado:"), g); g.gridx = 1; cardAeronave.add(comboEstado, g);
        g.gridx = 0; g.gridy = 5; cardAeronave.add(crearLabelMuted("Descripción:"), g); g.gridx = 1; cardAeronave.add(new JScrollPane(txtDescripcion), g);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.insets = new Insets(15, 5, 5, 5); cardAeronave.add(btnRegistrarAvion, g);

        dashboard.add(cardAeronave);

        // 🗺️ CARD 2: MAPEO DE CUADRÍCULA DE CABINA
        JPanel cardMapeo = new JPanel(new BorderLayout(10, 10));
        cardMapeo.setBackground(EstiloUI.FONDO_TARJETA);
        cardMapeo.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(20, 15, 20, 15)));
        JLabel lblTm = new JLabel("MAPEO DE ASIENTOS", SwingConstants.CENTER);
        lblTm.setForeground(EstiloUI.TEXTO_BLANCO); lblTm.setFont(EstiloUI.FUENTE_SUBTITULO);
        cardMapeo.add(lblTm, BorderLayout.NORTH);

        JPanel mockGrid = new JPanel(new GridLayout(7, 3, 6, 6));
        mockGrid.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        for (int i = 1; i <= 21; i++) {
            JButton bSeat = new JButton("A" + i);
            bSeat.setBackground(EstiloUI.ASIENTO_OCUPADO); bSeat.setForeground(EstiloUI.TEXTO_MUTED); bSeat.setBorder(EstiloUI.BORDE_COMPONENTE); bSeat.setEnabled(false);
            mockGrid.add(bSeat);
        }
        cardMapeo.add(mockGrid, BorderLayout.CENTER);
        JButton btnMc = new JButton("MAPEO COMPLETO");
        btnMc.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnMc.setForeground(EstiloUI.TEXTO_BLANCO);
        btnMc.setBorderPainted(false);
        cardMapeo.add(btnMc, BorderLayout.SOUTH);

        // 🔥 LOGICA REACTIVA DE INTEGRACIÓN EN TIEMPO REAL:
        btnMc.addActionListener(e -> {
            String matriculaActual = txtMatricula.getText().trim();
            String capacidadTexto = txtCapacidad.getText().trim();

            if (matriculaActual.isEmpty() || capacidadTexto.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, ingrese la Matrícula y Capacidad del avión en el formulario izquierdo para poder diseñar su mapa.",
                        "Datos Faltantes", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int capacidad;
            try {
                capacidad = Integer.parseInt(capacidadTexto);
                if (capacidad <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "La capacidad debe ser un número entero mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 🔥 VALIDACIÓN CRÍTICA: Verificar si el avión ya existe en SQL Server antes de abrir el diálogo
            AvionDAO avionDAO = new AvionDAO();
            if (!avionDAO.verificarMatriculaRegistrada(matriculaActual)) {
                JOptionPane.showMessageDialog(this,
                        "El avión con matrícula '" + matriculaActual + "' aún no ha sido registrado en la flota.\n" +
                                "Por favor, haga clic primero en 'REGISTRAR EN FLOTA' antes de configurar sus asientos.",
                        "Aeronave No Encontrada", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Si pasa la validación, abre el diseñador de forma totalmente segura
            DialogoMapeoCompleto diseñadorInteligente = new DialogoMapeoCompleto(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    matriculaActual,
                    capacidad
            );
            diseñadorInteligente.setVisible(true);
        });

        dashboard.add(cardMapeo);

        // 👥 CARD 3: RECURSOS INTERNOS - CRUD DE PILOTOS CONECTADO A BASE DE DATOS
        JPanel cardRecursos = new JPanel(new GridLayout(3, 1, 0, 15));
        cardRecursos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // Sub-Card Pilotos Interactiva
        JPanel subPilotos = new JPanel(new BorderLayout(5, 5));
        subPilotos.setBackground(EstiloUI.FONDO_TARJETA);
        subPilotos.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(10, 10, 10, 10)));

        JPanel cabeceraPilotos = new JPanel(new BorderLayout());
        cabeceraPilotos.setBackground(EstiloUI.FONDO_TARJETA);
        JLabel lblP = new JLabel("Pilotos Disponibles");
        lblP.setForeground(EstiloUI.TEXTO_BLANCO); lblP.setFont(EstiloUI.FUENTE_SUBTITULO);
        JButton btnGestionarPilotos = new JButton("⚙ Manage");
        btnGestionarPilotos.setFont(new Font("SansSerif", Font.BOLD, 10)); btnGestionarPilotos.setBackground(EstiloUI.AZUL_ACCENT); btnGestionarPilotos.setForeground(EstiloUI.TEXTO_BLANCO); btnGestionarPilotos.setBorderPainted(false);
        cabeceraPilotos.add(lblP, BorderLayout.WEST);
        cabeceraPilotos.add(btnGestionarPilotos, BorderLayout.EAST);
        subPilotos.add(cabeceraPilotos, BorderLayout.NORTH);

        areaPilotosUI = new JTextArea();
        areaPilotosUI.setBackground(EstiloUI.FONDO_TARJETA);
        areaPilotosUI.setForeground(EstiloUI.TEXTO_MUTED); areaPilotosUI.setFont(EstiloUI.FUENTE_LABEL); areaPilotosUI.setEditable(false);
        subPilotos.add(new JScrollPane(areaPilotosUI), BorderLayout.CENTER);
        cardRecursos.add(subPilotos);

        recargarListaPilotosUI(); // Carga los pilotos de la BD al iniciar

        // Accion del administrador para lanzar el gestor CRUD de Pilotos
        btnGestionarPilotos.addActionListener(e -> abrirDialogoCrudPilotos());

        // Sub-Tarjeta B: Mantenimientos
        JPanel subMantenimiento = new JPanel(new BorderLayout(5, 5));
        subMantenimiento.setBackground(EstiloUI.FONDO_TARJETA);
        subMantenimiento.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(10, 10, 10, 10)));
        JLabel lblMant = new JLabel("Mantenimientos");
        lblMant.setForeground(EstiloUI.TEXTO_BLANCO); lblMant.setFont(EstiloUI.FUENTE_SUBTITULO);
        subMantenimiento.add(lblMant, BorderLayout.NORTH);

        JPanel pBotones = new JPanel(new GridLayout(3, 1, 0, 5));
        pBotones.setBackground(EstiloUI.FONDO_TARJETA);
        String[] acciones = {"Registrar", "Editar", "Ver"};
        for (String ac : acciones) {
            JButton b = new JButton(ac);
            b.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b.setForeground(EstiloUI.TEXTO_BLANCO); b.setFont(EstiloUI.FUENTE_COMPONENTE); b.setBorderPainted(false);
            pBotones.add(b);
        }
        subMantenimiento.add(pBotones, BorderLayout.CENTER);
        cardRecursos.add(subMantenimiento);

        // Sub-Tarjeta C: Bloque vacío de Figma
        JPanel subAdicional = new JPanel(new GridBagLayout());
        subAdicional.setBackground(EstiloUI.FONDO_TARJETA);
        subAdicional.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(10, 10, 10, 10)));
        JLabel lblPlusBig = new JLabel("+");
        lblPlusBig.setForeground(EstiloUI.TEXTO_MUTED); lblPlusBig.setFont(new Font("SansSerif", Font.BOLD, 42));
        subAdicional.add(lblPlusBig);
        cardRecursos.add(subAdicional);

        dashboard.add(cardRecursos);

        btnRegistrarAvion.addActionListener(e -> guardarAvionBaseDatos());

        return dashboard;
    }

    private void recargarListaPilotosUI() {
        List<Piloto> pilotos = pilotoDAO.obtenerPilotos();
        StringBuilder sb = new StringBuilder();
        for (Piloto p : pilotos) {
            sb.append(" • ").append(p.getNombre()).append(" (").append(p.getRango()).append(") - ").append(p.getEstado()).append("\n");
        }
        if(pilotos.isEmpty()) {
            sb.append(" No hay pilotos en el sistema.");
        }
        areaPilotosUI.setText(sb.toString());
    }

    private void abrirDialogoCrudPilotos() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Panel de Control de Pilotos", true);
        dialog.getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        dialog.setSize(420, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        JTextField txtNom = new JTextField(12);
        JTextField txtRan = new JTextField(12); // Ej: Comandante, Co-Piloto
        JComboBox<String> cbEst = new JComboBox<>(new String[]{"Disponible", "En Vuelo", "Licencia"});

        txtNom.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); txtNom.setForeground(EstiloUI.TEXTO_BLANCO); txtNom.setBorder(EstiloUI.BORDE_COMPONENTE);
        txtRan.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); txtRan.setForeground(EstiloUI.TEXTO_BLANCO); txtRan.setBorder(EstiloUI.BORDE_COMPONENTE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Nombre Piloto:"), gbc); gbc.gridx = 1; dialog.add(txtNom, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Rango/Título:"), gbc); gbc.gridx = 1; dialog.add(txtRan, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Estado:"), gbc); gbc.gridx = 1; dialog.add(cbEst, gbc);

        JButton btnAdd = new JButton("Agregar");
        JButton btnDel = new JButton("Eliminar por Nombre");
        btnAdd.setBackground(EstiloUI.VERDE_NEON); btnAdd.setForeground(EstiloUI.TEXTO_BLANCO); btnAdd.setBorderPainted(false);
        btnDel.setBackground(EstiloUI.ROJO_ALERTA); btnDel.setForeground(EstiloUI.TEXTO_BLANCO); btnDel.setBorderPainted(false);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(btnAdd, gbc);
        gbc.gridx = 1; gbc.gridy = 3; dialog.add(btnDel, gbc);

        for (Component comp : dialog.getContentPane().getComponents()) {
            if (comp instanceof JLabel) comp.setForeground(EstiloUI.TEXTO_MUTED);
        }

        // Lógica de Alta de Piloto
        btnAdd.addActionListener(e -> {
            String nom = txtNom.getText().trim();
            String ran = txtRan.getText().trim();
            if(!nom.isEmpty() && !ran.isEmpty()){
                if(pilotoDAO.insertarPiloto(new Piloto(0, nom, ran, (String)cbEst.getSelectedItem()))) {
                    JOptionPane.showMessageDialog(dialog, "Piloto contratado y registrado.");
                    recargarListaPilotosUI();
                    dialog.dispose();
                }
            }
        });

        // Lógica de Baja de Piloto simplificada por nombre para no sobrecomplicar con IDs
        btnDel.addActionListener(e -> {
            String nom = txtNom.getText().trim();
            List<Piloto> actual = pilotoDAO.obtenerPilotos();
            for(Piloto p : actual) {
                if(p.getNombre().equalsIgnoreCase(nom)) {
                    if(pilotoDAO.eliminarPiloto(p.getIdPiloto())) {
                        JOptionPane.showMessageDialog(dialog, "Registro de piloto eliminado.");
                        recargarListaPilotosUI();
                        dialog.dispose();
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(dialog, "Piloto no encontrado.");
        });

        dialog.setVisible(true);
    }

    private JTextField crearCampoTexto() {
        JTextField f = new JTextField(10);
        f.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); f.setForeground(EstiloUI.TEXTO_BLANCO); f.setCaretColor(EstiloUI.TEXTO_BLANCO); f.setBorder(EstiloUI.BORDE_COMPONENTE);
        return f;
    }

    private JLabel crearLabelMuted(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_MUTED); l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }

    private void aplicarEstiloBotonMenu(JButton b, boolean activo) {
        b.setPreferredSize(new Dimension(150, 42)); b.setFont(EstiloUI.FUENTE_SUBTITULO); b.setFocusPainted(false); b.setBorderPainted(false);
        if (activo) {
            b.setBackground(EstiloUI.AZUL_ACCENT);
            b.setForeground(EstiloUI.TEXTO_BLANCO);
        } else {
            b.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
            b.setForeground(EstiloUI.TEXTO_MUTED);
        }
    }

    private void guardarAvionBaseDatos() {
        String matricula = txtMatricula.getText().trim();
        String modelo = txtModelo.getText().trim();
        String capacidadTexto = txtCapacidad.getText().trim();
        String estado = (String) comboEstado.getSelectedItem();

        if (matricula.isEmpty() || modelo.isEmpty() || capacidadTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int capacidad;
        try { capacidad = Integer.parseInt(capacidadTexto); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La capacidad debe ser un número.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AvionDAO avionDAO = new AvionDAO();
        if (avionDAO.verificarMatriculaRegistrada(matricula)) {
            JOptionPane.showMessageDialog(this, "Esta matrícula ya está registrada.", "Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (avionDAO.guardarAvion(new Avion(matricula, modelo, capacidad, estado))) {
            JOptionPane.showMessageDialog(this, "Aeronave dada de alta exitosamente.");
            txtMatricula.setText(""); txtModelo.setText(""); txtCapacidad.setText(""); txtDescripcion.setText("");
            panelRadarView.recargarDatosAviones(); // Fuerza al radar a dibujar el nuevo avión al instante
        }
    }
}