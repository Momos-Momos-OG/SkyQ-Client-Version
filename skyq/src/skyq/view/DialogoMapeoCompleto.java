package skyq.view;

import skyq.dao.ConfiguracionAsientosDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DialogoMapeoCompleto extends JDialog {

    private final String matriculaAvion;
    private final int capacidadAvion;
    private final ConfiguracionAsientosDAO configDAO = new ConfiguracionAsientosDAO();

    // Componentes de control
    private JComboBox<Integer> comboPasillos;
    private JSpinner spinnerColumnas;
    private JLabel lblInfoCalculo;

    private JPanel panelTogglesColumnas;
    private JPanel panelMatrizCabina;
    private JButton btnGuardar;

    // Estado interno del mapa adaptativo
    private int totalColumnas;
    private boolean[] esPasillo;

    public DialogoMapeoCompleto(Frame padre, String matriculaAvion, int capacidadAvion) {
        super(padre, "Diseñador de Cabina Avanzado - " + matriculaAvion, true);
        this.matriculaAvion = matriculaAvion;
        this.capacidadAvion = capacidadAvion;

        getContentPane().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setSize(1000, 700);
        setLocationRelativeTo(padre);
        initComponents();

        // Ejecuta la generación automática por defecto basada en la capacidad inicial
        generarDisposicionPredeterminada();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        // ==========================================
        // 🛠️ PANEL SUPERIOR: CONTROLES AUTOMÁTICOS
        // ==========================================
        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBackground(EstiloUI.FONDO_TARJETA);
        panelControles.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(10, 15, 10, 15)));

        JLabel lblCapacidad = new JLabel("Aeronave: " + matriculaAvion + " | Capacidad Registrada: " + capacidadAvion + " asientos");
        lblCapacidad.setForeground(Color.CYAN);
        lblCapacidad.setFont(EstiloUI.FUENTE_SUBTITULO);

        comboPasillos = new JComboBox<>(new Integer[]{1, 2});
        comboPasillos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboPasillos.setForeground(EstiloUI.TEXTO_BLANCO);

        // Spinner para definir el ancho total de columnas (asientos + pasillos virtuales)
        spinnerColumnas = new JSpinner(new SpinnerNumberModel(7, 3, 15, 1));
        JComponent editor = spinnerColumnas.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
            ((JSpinner.DefaultEditor) editor).getTextField().setForeground(EstiloUI.TEXTO_BLANCO);
        }
        spinnerColumnas.setBorder(EstiloUI.BORDE_COMPONENTE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        panelControles.add(lblCapacidad, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; panelControles.add(crearLabelVisual("Número de Pasillos:"), gbc);
        gbc.gridx = 1; panelControles.add(comboPasillos, gbc);
        gbc.gridx = 2; panelControles.add(crearLabelVisual("Ancho de Columnas Totales:"), gbc);
        gbc.gridx = 3; panelControles.add(spinnerColumnas, gbc);

        add(panelControles, BorderLayout.NORTH);

        // ==========================================
        // 🎛️ PANEL CENTRAL: SELECTOR DE COLUMNAS + RENDER
        // ==========================================
        JPanel panelCentroEstructura = new JPanel(new BorderLayout(10, 10));
        panelCentroEstructura.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // Sub-Panel A: Fila de Botones para editar la disposición manualmente
        panelTogglesColumnas = new JPanel();
        panelTogglesColumnas.setBackground(EstiloUI.FONDO_TARJETA);
        panelTogglesColumnas.setBorder(EstiloUI.BORDE_COMPONENTE);
        panelTogglesColumnas.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
        panelCentroEstructura.add(panelTogglesColumnas, BorderLayout.NORTH);

        // Sub-Panel B: Matriz del Avión que se dibuja en tiempo real
        panelMatrizCabina = new JPanel();
        panelMatrizCabina.setBackground(EstiloUI.FONDO_TARJETA);
        panelMatrizCabina.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollCabina = new JScrollPane(panelMatrizCabina);
        scrollCabina.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollCabina.setBorder(BorderFactory.createEmptyBorder());
        panelCentroEstructura.add(scrollCabina, BorderLayout.CENTER);

        add(panelCentroEstructura, BorderLayout.CENTER);

        // ==========================================
        // 💾 PANEL INFERIOR: ACCIONES Y RESUMEN
        // ==========================================
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(EstiloUI.FONDO_TARJETA);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(10, 15, 10, 15)));

        lblInfoCalculo = new JLabel("Cargando mapeo...");
        lblInfoCalculo.setForeground(EstiloUI.TEXTO_MUTED);
        lblInfoCalculo.setFont(EstiloUI.FUENTE_LABEL);

        btnGuardar = new JButton("Confirmar y Guardar en Servidor");
        btnGuardar.setBackground(EstiloUI.VERDE_NEON);
        btnGuardar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnGuardar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnGuardar.setBorderPainted(false);

        panelInferior.add(lblInfoCalculo, BorderLayout.WEST);
        panelInferior.add(btnGuardar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        // ==========================================
        // 🔄 LISTENERS REACTIVOS EN TIEMPO REAL
        // ==========================================
        comboPasillos.addActionListener(e -> {
            // Reajusta el ancho predeterminado según el número de pasillos elegidos
            int pasillos = (int) comboPasillos.getSelectedItem();
            spinnerColumnas.setValue(pasillos == 1 ? 7 : 11);
            generarDisposicionPredeterminada();
        });

        spinnerColumnas.addChangeListener(e -> generarDisposicionPredeterminada());
        btnGuardar.addActionListener(e -> guardarConfiguracionFinal());
    }

    /**
     * Aplica el algoritmo automático para posicionar simétricamente los pasillos
     * según las reglas de negocio establecidas.
     */
    private void generarDisposicionPredeterminada() {
        totalColumnas = (int) spinnerColumnas.getValue();
        int numPasillos = (int) comboPasillos.getSelectedItem();
        esPasillo = new boolean[totalColumnas];

        if (numPasillos == 1) {
            // Regla 1 Pasillo: Siempre colocado exactamente en la mitad de la cabina
            int mitad = totalColumnas / 2;
            esPasillo[mitad] = true;
        } else {
            // Regla 2 Pasillos: Divide los asientos en 3 bloques. Symmetrical a los lados, desigual en el centro
            int totalAsientos = totalColumnas - 2;
            if (totalAsientos >= 3) {
                int base = totalAsientos / 3;
                int residuo = totalAsientos % 3;

                int secA = base;
                int secCentro = base + residuo; // El bloque desigual queda en el centro siempre

                int indicePasillo1 = secA;
                int indicePasillo2 = secA + 1 + secCentro;

                if (indicePasillo1 < totalColumnas) esPasillo[indicePasillo1] = true;
                if (indicePasillo2 < totalColumnas) esPasillo[indicePasillo2] = true;
            } else {
                // Caída segura en caso de que pongan un número de columnas muy pequeño
                if (totalColumnas > 2) esPasillo[1] = true;
                if (totalColumnas > 3) esPasillo[totalColumnas - 2] = true;
            }
        }

        actualizarFilaDeEdicionManual();
    }

    /**
     * Genera la fila de botones superiores para que el gerente edite la disposición
     * de cualquier columna manualmente con un solo clic.
     */
    private void actualizarFilaDeEdicionManual() {
        panelTogglesColumnas.removeAll();

        for (int i = 0; i < totalColumnas; i++) {
            final int indiceColumna = i;
            JToggleButton btnToggle = new JToggleButton();
            btnToggle.setFont(new Font("SansSerif", Font.BOLD, 10));
            btnToggle.setFocusPainted(false);

            // Estilo visual según su estado actual
            if (esPasillo[i]) {
                btnToggle.setText("🚶 Pasillo");
                btnToggle.setSelected(true);
                btnToggle.setBackground(EstiloUI.AZUL_ACCENT);
                btnToggle.setForeground(EstiloUI.TEXTO_BLANCO);
            } else {
                btnToggle.setText("💺 Asiento");
                btnToggle.setSelected(false);
                btnToggle.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
                btnToggle.setForeground(EstiloUI.TEXTO_MUTED);
            }

            // Evento interactivo: Modifica el layout al dar clic en la columna
            btnToggle.addActionListener(e -> {
                esPasillo[indiceColumna] = btnToggle.isSelected();
                recalcularYRenderizarMatrizRealTime();

                // Actualiza el aspecto del botón presionado
                if (esPasillo[indiceColumna]) {
                    btnToggle.setText("🚶 Pasillo");
                    btnToggle.setBackground(EstiloUI.AZUL_ACCENT);
                    btnToggle.setForeground(EstiloUI.TEXTO_BLANCO);
                } else {
                    btnToggle.setText("💺 Asiento");
                    btnToggle.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
                    btnToggle.setForeground(EstiloUI.TEXTO_MUTED);
                }
            });

            panelTogglesColumnas.add(btnToggle);
        }

        panelTogglesColumnas.revalidate();
        panelTogglesColumnas.repaint();

        // Dispara el renderizado de la matriz física
        recalcularYRenderizarMatrizRealTime();
    }

    /**
     * Recalcula la cantidad de filas necesarias según los asientos útiles por fila
     * y dibuja la cuadrícula en tiempo real sin numerar las columnas.
     */
    private void recalcularYRenderizarMatrizRealTime() {
        panelMatrizCabina.removeAll();

        int asientosPorFila = 0;
        for (boolean pasillo : esPasillo) {
            if (!pasillo) asientosPorFila++;
        }

        // Validación de seguridad para evitar divisiones para cero si el gerente borra todos los asientos
        if (asientosPorFila == 0) {
            lblInfoCalculo.setText("⚠️ ERROR: Debe existir al menos una columna de asientos.");
            lblInfoCalculo.setForeground(EstiloUI.ROJO_ALERTA);
            panelMatrizCabina.setLayout(new BorderLayout());
            panelMatrizCabina.add(new JLabel("Disposición de cabina no válida", SwingConstants.CENTER), BorderLayout.CENTER);
            btnGuardar.setEnabled(false);
            panelMatrizCabina.revalidate();
            panelMatrizCabina.repaint();
            return;
        }

        // Calcula automáticamente cuántas filas se necesitan según la capacidad del avión
        int totalFilas = (int) Math.ceil((double) capacidadAvion / asientosPorFila);

        lblInfoCalculo.setText("Estructura Dinámica: " + totalFilas + " Filas calculadas | " + asientosPorFila + " Asientos por fila");
        lblInfoCalculo.setForeground(EstiloUI.VERDE_NEON);
        btnGuardar.setEnabled(true);

        panelMatrizCabina.setLayout(new GridLayout(totalFilas, totalColumnas, 6, 6));
        String[] abecedarioNomenclatura = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N"};

        // Dibuja la cabina física del avión
        for (int f = 1; f <= totalFilas; f++) {
            int indiceLetra = 0;
            for (int c = 0; c < totalColumnas; c++) {
                if (esPasillo[c]) {
                    // Renderizado de Corredor (No se numera la columna, muestra solo el número de fila indicador en el medio)
                    JLabel lblCorredor = new JLabel(String.valueOf(f), SwingConstants.CENTER);
                    lblCorredor.setForeground(EstiloUI.TEXTO_MUTED);
                    lblCorredor.setFont(new Font("SansSerif", Font.BOLD, 10));
                    panelMatrizCabina.add(lblCorredor);
                } else {
                    // Renderizado de Asiento Puro usando Letra correlativa (ej: 1A, 1B, 1C)
                    String codigoButaca = f + abecedarioNomenclatura[indiceLetra % abecedarioNomenclatura.length];
                    indiceLetra++;

                    JButton btnSeatPreview = new JButton(codigoButaca);
                    btnSeatPreview.setBackground(EstiloUI.VERDE_NEON);
                    btnSeatPreview.setForeground(EstiloUI.TEXTO_BLANCO);
                    btnSeatPreview.setFont(new Font("SansSerif", Font.BOLD, 9));
                    btnSeatPreview.setBorder(EstiloUI.BORDE_COMPONENTE);
                    btnSeatPreview.setEnabled(false); // Es una previsualización de administración
                    panelMatrizCabina.add(btnSeatPreview);
                }
            }
        }

        panelMatrizCabina.revalidate();
        panelMatrizCabina.repaint();
    }

    /**
     * Serializa los índices de pasillos a formato CSV y guarda la configuración en SQL Server.
     */
    private void guardarConfiguracionFinal() {
        int asientosPorFila = 0;
        StringBuilder sbPasillos = new StringBuilder();

        for (int i = 0; i < esPasillo.length; i++) {
            if (esPasillo[i]) {
                if (sbPasillos.length() > 0) sbPasillos.append(",");
                sbPasillos.append(i + 1); // Almacenamiento base 1 para legibilidad en DB
            } else {
                asientosPorFila++;
            }
        }

        int totalFilas = (int) Math.ceil((double) capacidadAvion / asientosPorFila);

        if (configDAO.guardarConfiguracion(matriculaAvion, totalFilas, totalColumnas, sbPasillos.toString())) {
            JOptionPane.showMessageDialog(this, "¡Distribución espacial guardada en Docker con éxito para la matrícula: " + matriculaAvion);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error crítico de conexión con el servidor de Base de Datos.", "Error de persistencia", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel crearLabelVisual(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_BLANCO);
        l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }
}