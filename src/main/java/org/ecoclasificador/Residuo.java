package org.ecoclasificador;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Representa un residuo que el jugador debe clasificar arrastrándolo
 * al contenedor correspondiente.
 * <p>
 * Cada residuo extiende {@link StackPane} y contiene:
 * <ul>
 *   <li>Una imagen representativa del residuo</li>
 *   <li>Una etiqueta con su nombre visible</li>
 *   <li>Categoría a la que pertenece (determina el contenedor correcto)</li>
 * </ul>
 * <p>
 * El residuo es arrastrable mediante eventos de ratón. Durante el arrastre
 * se utiliza {@link #setTranslateX(double)} y {@link #setTranslateY(double)}
 * para mantener la posición original ({@code layoutX/Y}) y poder
 * animar el retorno si la clasificación es incorrecta.
 */
public class Residuo extends StackPane {

    private final String nombre;
    private final String categoria;
    private final Color colorCategoria;
    private final String nombreArchivo;

    /** Referencia al juego principal para notificar eventos y obtener parámetros */
    private final JuegoReciclaje juego;

    /** Posición original dentro del área de juego (usada para animar retorno) */
    private double origenX;
    private double origenY;

    /** Offset del cursor respecto a la esquina del elemento al iniciar el arrastre */
    private double dx;
    private double dy;

    /** true cuando el residuo ya fue clasificado correctamente */
    private boolean clasificado;

    /**
     * Crea un nuevo residuo arrastrable.
     *
     * @param nombreArchivo Nombre del archivo de imagen dentro de /asset/
     * @param nombre        Nombre visible del residuo (ej: "Periódico")
     * @param categoria     Categoría de reciclaje (ej: "Papel")
     * @param juego         Referencia al juego para notificar eventos y obtener escalado
     */
    public Residuo(String nombreArchivo, String nombre, String categoria, JuegoReciclaje juego) {
        this.nombreArchivo  = nombreArchivo;
        this.nombre         = nombre;
        this.categoria      = categoria;
        this.colorCategoria = JuegoReciclaje.colorCategoria(categoria);
        this.juego          = juego;

        construirVisual();
        configurarDragDrop();
    }

    // =================================================================
    // GETTERS PÚBLICOS
    // =================================================================

    public String getNombre()               { return nombre; }
    public String getCategoria()            { return categoria; }
    public Color getColorCategoria()        { return colorCategoria; }
    public String getNombreArchivo()        { return nombreArchivo; }
    public double getOrigenX()              { return origenX; }
    public double getOrigenY()              { return origenY; }
    public boolean isClasificado()          { return clasificado; }

    /**
     * Establece la posición original del residuo dentro del área de juego.
     * Este valor se usa como referencia para animar el retorno cuando
     * el residuo se suelta en un lugar incorrecto.
     *
     * @param x Coordenada X original
     * @param y Coordenada Y original
     */
    public void setOrigen(double x, double y) {
        this.origenX = x;
        this.origenY = y;
    }

    // =================================================================
    // CONSTRUCCIÓN VISUAL
    // =================================================================

    /**
     * Construye la apariencia del residuo: fondo redondeado con sombra,
     * imagen representativa y nombre textual.
     */
    private void construirVisual() {
        double anchoItem = juego.getAnchoItem();
        double altoItem  = juego.getAltoItem();
        double escala    = juego.getEscala();

        setPrefSize(anchoItem, altoItem);

        Rectangle fondo = new Rectangle(anchoItem, altoItem);
        fondo.setArcWidth(16 * escala);
        fondo.setArcHeight(16 * escala);
        fondo.setFill(Color.WHITE);
        fondo.setStroke(Color.web("#DDDDDD"));
        fondo.setStrokeWidth(2 * escala);
        fondo.setEffect(new DropShadow(6 * escala, Color.web("#00000022")));

        ImageView imgView;
        try {
            Image img = new Image(getClass().getResource("/asset/" + nombreArchivo).toExternalForm());
            imgView = new ImageView(img);
            double tamImg = Math.min(anchoItem * 0.45, altoItem * 0.40);
            imgView.setFitWidth(tamImg);
            imgView.setFitHeight(tamImg);
            imgView.setPreserveRatio(true);
        } catch (Exception e) {
            imgView = new ImageView();
        }

        Text texto = new Text(nombre);
        texto.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, juego.getFuenteItem()));
        texto.setFill(Color.web("#333333"));

        VBox contenido = new VBox(4 * escala, imgView, texto);
        contenido.setAlignment(Pos.CENTER);

        getChildren().addAll(fondo, contenido);
    }

    // =================================================================
    // CONFIGURACIÓN DE ARRASTRE
    // =================================================================

    /**
     * Conecta los eventos de ratón necesarios para hacer el residuo arrastrable:
     * pulsar, arrastrar y soltar.
     */
    private void configurarDragDrop() {
        setOnMousePressed(this::alPresionar);
        setOnMouseDragged(this::alArrastrar);
        setOnMouseReleased(this::alSoltar);
        setOnMouseEntered(e -> setCursor(Cursor.HAND));
        setOnMouseExited(e  -> setCursor(Cursor.DEFAULT));
    }

    /**
     * Al presionar el ratón sobre el residuo: registra la posición original,
     * calcula el offset del cursor, escala el elemento y lo trae al frente.
     */
    private void alPresionar(MouseEvent e) {
        if (!juego.isJuegoActivo() || clasificado) return;

        juego.setItemArrastrado(this);

        origenX = getLayoutX();
        origenY = getLayoutY();

        dx = e.getSceneX() - (getLayoutX() + getTranslateX());
        dy = e.getSceneY() - (getLayoutY() + getTranslateY());

        toFront();

        setScaleX(1.08);
        setScaleY(1.08);
        setEffect(new DropShadow(18, Color.web("#00000044")));

        e.consume();
    }

    /**
     * Durante el arrastre: desplaza el residuo mediante {@link #setTranslateX/Y}
     * para mantener la posición {@code layout} como referencia de origen.
     * Se aplican límites para que el residuo no salga del área de juego.
     */
    private void alArrastrar(MouseEvent e) {
        if (juego.getItemArrastrado() != this || !juego.isJuegoActivo() || clasificado) return;

        double limiteX = juego.getAreaJuego().getWidth();
        if (limiteX <= 0) limiteX = juego.getAnchoVentana();
        double limiteY = juego.getAreaJuego().getHeight();
        if (limiteY <= 0) limiteY = juego.getAltoVentana() - 60;

        double nuevaTranslateX = Math.max(15 - origenX,
            Math.min(limiteX - juego.getAnchoItem() - 15 - origenX,
                e.getSceneX() - dx - origenX));
        double nuevaTranslateY = Math.max(15 - origenY,
            Math.min(limiteY - juego.getAltoItem() - 15 - origenY,
                e.getSceneY() - dy - origenY));

        setTranslateX(nuevaTranslateX);
        setTranslateY(nuevaTranslateY);

        for (Contenedor c : juego.getContenedores()) {
            c.setHoverActivo(UtilColision.colisionan(this, c));
        }

        e.consume();
    }

    /**
     * Al soltar el residuo: detecta si está sobre un contenedor válido
     * y notifica al juego para procesar acierto o fallo. Si no está sobre
     * ningún contenedor, anima el retorno a su posición original.
     */
    private void alSoltar(MouseEvent e) {
        if (juego.getItemArrastrado() != this || !juego.isJuegoActivo() || clasificado) return;

        juego.setItemArrastrado(null);

        setScaleX(1.0);
        setScaleY(1.0);
        setEffect(new DropShadow(5, Color.web("#00000022")));

        for (Contenedor c : juego.getContenedores()) {
            c.setHoverActivo(false);
        }

        for (Contenedor c : juego.getContenedores()) {
            if (UtilColision.colisionan(this, c)) {
                if (c.getCategoria().equals(this.categoria)) {
                    clasificado = true;
                    juego.setResiduosRestantes(juego.getResiduosRestantes() - 1);
                    juego.setPuntuacion(juego.getPuntuacion() + JuegoReciclaje.PUNTOS_ACIERTO);
                    juego.actualizarTextoMarcador();
                    juego.animarAcierto(this, c);

                    if (juego.getResiduosRestantes() <= 0) {
                        juego.detenerTemporizador();
                        juego.terminarJuego(true);
                    }
                } else {
                    juego.setPuntuacion(Math.max(0, juego.getPuntuacion() + JuegoReciclaje.PUNTOS_FALLO));
                    juego.actualizarTextoMarcador();
                    juego.animarFallo(this);
                }
                e.consume();
                return;
            }
        }

        juego.animarVuelta(this);
        e.consume();
    }
}
