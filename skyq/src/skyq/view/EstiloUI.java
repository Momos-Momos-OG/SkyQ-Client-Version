package skyq.view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.border.LineBorder;

public class EstiloUI {
    // Paleta Dark Mode Aeroespacial (Figma)
    public static final Color FONDO_DARK_PRINCIPAL = new Color(13, 17, 23);
    public static final Color FONDO_TARJETA = new Color(22, 27, 34);
    public static final Color AZUL_ACCENT = new Color(31, 111, 235);
    public static final Color GRIS_BOTON_PASIVO = new Color(33, 38, 45);
    public static final Color VERDE_NEON = new Color(46, 160, 67);
    public static final Color ROJO_ALERTA = new Color(239, 68, 68);
    public static final Color ASIENTO_OCUPADO = new Color(51, 65, 85);

    // Colores de Texto
    public static final Color TEXTO_BLANCO = new Color(240, 246, 252);
    public static final Color TEXTO_MUTED = new Color(139, 148, 158);

    // Fuentes Limpias
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_LABEL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_COMPONENTE = new Font("SansSerif", Font.BOLD, 12);

    // Sistema de Bordes Unificados para evitar caídas en otros componentes
    public static final LineBorder BORDE_COMPONENTE = new LineBorder(new Color(48, 54, 61), 1);
    public static final LineBorder BORDE_TARJETA = new LineBorder(new Color(48, 54, 61), 1);
}