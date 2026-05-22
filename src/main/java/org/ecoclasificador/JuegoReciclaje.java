package org.ecoclasificador;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controlador principal del juego de reciclaje. Extiende {@link BorderPane}
 * para organizar la interfaz en barra superior (puntuación, tiempo, progreso)
 * y área central (residuos arrastrables + contenedores).
 * <p>
 * Gestiona:
 * <ul>
 *   <li>Generación y mezcla de residuos aleatorios por partida</li>
 *   <li>Temporizador con cuenta atrás</li>
 *   <li>Cálculo de puntuación (aciertos +10, fallos -5)</li>
 *   <li>Animaciones de acierto, fallo y retorno</li>
 *   <li>Pantalla de fin de juego con opciones de reinicio y ranking</li>
 * </ul>
 * <p>
 * Se comunica con {@link Residuo} y {@link Contenedor} exclusivamente
 * mediante getters públicos y callbacks, manteniendo el encapsulamiento.
 */
public class JuegoReciclaje extends BorderPane {

    // =====================================================================
    // CONSTANTES PÚBLICAS
    // =====================================================================

    /** Catálogo completo de residuos: {archivo, nombre, categoría} */
    public static final String[][] DATOS_RESIDUOS = {
        {"delivery-box.png",  "Caja",          "Papel"},
        {"milk-carton.png",   "Brick leche",   "Papel"},
        {"book.png",          "Libro",         "Papel"},
        {"mail.png",          "Sobre",         "Papel"},
        {"newspaper.png",     "Periódico",     "Papel"},
        {"plastic-bottle.png","Botella PET",   "Plástico"},
        {"poly-bag.png",      "Bolsa",         "Plástico"},
        {"pen.png",           "Bolígrafo",     "Plástico"},
        {"toothbrush.png",    "Cepillo",       "Plástico"},
        {"ampoule.png",       "Bombilla",      "Vidrio"},
        {"cup.png",           "Vaso yogur",    "Plástico"},
        {"glass.png",         "Vidrio",        "Vidrio"},
        {"jam.png",           "Mermelada",     "Vidrio"},
        {"mirror.png",        "Espejo",        "Vidrio"},
        {"red-wine.png",      "Vino tinto",    "Vidrio"},
        {"banana.png",        "Plátano",       "Orgánico"},
        {"apple.png",         "Manzana",       "Orgánico"},
        {"compost.png",       "Compost",       "Orgánico"},
        {"stink.png",         "Residuo",       "Orgánico"},
        {"battery.png",       "Pila",          "Electrónico"},
        {"headphones.png",    "Auriculares",   "Electrónico"},
        {"joystick.png",      "Mando",         "Electrónico"},
        {"keyboard.png",      "Teclado",       "Electrónico"},
        {"lan.png",           "Cable red",     "Electrónico"},
        {"radio.png",         "Radio",         "Electrónico"},
    };

    /** Puntos que suma un acierto */
    public static final int PUNTOS_ACIERTO = 10;
    /** Puntos que resta un fallo */
    public static final int PUNTOS_FALLO   = -5;
    /** Cantidad de residuos que aparecen por partida */
    public static final int ITEMS_POR_PARTIDA = 10;
    /** Tiempo límite total en segundos */
    public static final int TIEMPO_LIMITE  = ITEMS_POR_PARTIDA * 9;

    /** Resolución base de diseño para escalado */
    private static final double BASE_ANCHO  = 900;
    private static final double BASE_ALTO   = 680;

    private static final Color COLOR_PAPEL       = Color.web("#1976D2");
    private static final Color COLOR_PLASTICO    = Color.web("#F9A825");
    private static final Color COLOR_VIDRIO      = Color.web("#388E3C");
    private static final Color COLOR_ORGANICO    = Color.web("#5D4037");
    private static final Color COLOR_ELECTRONICO = Color.web("#7B1FA2");

    /**
     * Devuelve el color asociado a una categoría de reciclaje.
     *
     * @param categoria Nombre de la categoría
     * @return Color representativo de la categoría
     */
    public static Color colorCategoria(String categoria) {
        return switch (categoria) {
            case "Papel"       -> COLOR_PAPEL;
            case "Plástico"    -> COLOR_PLASTICO;
            case "Vidrio"      -> COLOR_VIDRIO;
            case "Orgánico"    -> COLOR_ORGANICO;
            case "Electrónico" -> COLOR_ELECTRONICO;
            default            -> Color.GRAY;
        };
    }

    /**
     * Devuelve un emoji representativo de la categoría.
     *
     * @param categoria Nombre de la categoría
     * @return Emoji ilustrativo
     */
    public static String iconoCategoria(String categoria) {
        return switch (categoria) {
            case "Papel"       -> "\uD83D\uDCC4";
            case "Plástico"    -> "\uD83E\uDDF4";
            case "Vidrio"      -> "\uD83C\uDF7E";
            case "Orgánico"    -> "\uD83C\uDF43";
            case "Electrónico" -> "\uD83D\uDD0C";
            default            -> "\u2753";
        };
    }

    // =====================================================================
    // ATRIBUTOS
    // =====================================================================

    private final double anchoVentana;
    private final double altoVentana;
    private final double escala;
    private final double anchoItem;
    private final double altoItem;
    private final double anchoBin;
    private final double altoBin;
    private final double fuenteItem;
    private final double fuenteBin;

    private Pane  areaJuego;
    private Text  textoPuntuacion;
    private Text  textoTiempo;
    private Text  textoProgreso;

    private int puntuacion;
    private int residuosRestantes;
    private int tiempoRestante;
    private boolean juegoActivo;

    private final List<Residuo>    residuos    = new ArrayList<>();
    private final List<Contenedor> contenedores = new ArrayList<>();

    private Timeline temporizador;
    private StackPane overlayFinJuego;

    /** Residuo que el usuario está arrastrando actualmente (null si ninguno) */
    private Residuo itemArrastrado;

    private GameOverCallback callbackGameOver;
    private RankingCallback  callbackRanking;

    @FunctionalInterface
    public interface GameOverCallback {
        void onGameOver(int puntuacion, int tiempoRestante);
    }

    @FunctionalInterface
    public interface RankingCallback {
        void mostrarRanking();
    }

    // =====================================================================
    // CONSTRUCTOR
    // =====================================================================

    /**
     * Construye el tablero de juego con las dimensiones de la pantalla.
     * Calcula el escalado en función de la resolución base y lanza
     * la primera partida automáticamente.
     *
     * @param nombreJugador  Nombre del jugador (actualmente solo se usa en la UI)
     * @param anchoPantalla  Ancho disponible de la pantalla
     * @param altoPantalla   Alto disponible de la pantalla
     */
    public JuegoReciclaje(String nombreJugador, double anchoPantalla, double altoPantalla) {
        this.anchoVentana = anchoPantalla;
        this.altoVentana  = altoPantalla;
        this.escala       = Math.min(anchoPantalla / BASE_ANCHO, altoPantalla / BASE_ALTO);

        this.anchoItem  = 130 * escala;
        this.altoItem   = 130 * escala;
        this.anchoBin   = 160 * escala;
        this.altoBin    = 120 * escala;
        this.fuenteItem = 13  * escala;
        this.fuenteBin  = 14  * escala;

        construirInterfaz();
        iniciarJuego();
    }

    // =====================================================================
    // GETTERS PÚBLICOS
    // =====================================================================

    public boolean isJuegoActivo()                 { return juegoActivo; }
    public Pane    getAreaJuego()                  { return areaJuego; }
    public double  getAnchoVentana()               { return anchoVentana; }
    public double  getAltoVentana()                { return altoVentana; }
    public double  getAnchoItem()                  { return anchoItem; }
    public double  getAltoItem()                   { return altoItem; }
    public double  getAnchoBin()                   { return anchoBin; }
    public double  getAltoBin()                    { return altoBin; }
    public double  getEscala()                     { return escala; }
    public double  getFuenteItem()                 { return fuenteItem; }
    public double  getFuenteBin()                  { return fuenteBin; }
    public Residuo getItemArrastrado()             { return itemArrastrado; }
    public List<Contenedor> getContenedores()      { return contenedores; }
    public int     getResiduosRestantes()           { return residuosRestantes; }
    public int     getPuntuacion()                  { return puntuacion; }

    public void setItemArrastrado(Residuo r)    { this.itemArrastrado = r; }
    public void setResiduosRestantes(int n)     { this.residuosRestantes = n; }
    public void setPuntuacion(int n)            { this.puntuacion = n; }

    public void setOnGameOver(GameOverCallback c)      { this.callbackGameOver = c; }
    public void setOnMostrarRanking(RankingCallback c) { this.callbackRanking = c; }

    /**
     * Detiene el temporizador del juego. Útil en tests y al terminar la partida.
     */
    public void detenerTemporizador() {
        if (temporizador != null) temporizador.stop();
    }

    // =====================================================================
    // CONSTRUCCIÓN DE LA INTERFAZ
    // =====================================================================

    /**
     * Ensambla la interfaz completa: fondo degradado, barra superior y
     * área de juego con los contenedores en la parte inferior.
     */
    private void construirInterfaz() {
        setPrefSize(anchoVentana, altoVentana);

        setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#C8E6C9")),
                new Stop(1, Color.web("#FFFFFF"))
            ), CornerRadii.EMPTY, Insets.EMPTY
        )));

        setTop(crearBarraSuperior());

        areaJuego = new Pane();
        areaJuego.setPrefWidth(anchoVentana);
        setCenter(areaJuego);

        String[] categorias = {"Papel", "Plástico", "Vidrio", "Orgánico", "Electrónico"};
        for (String cat : categorias) {
            Contenedor c = new Contenedor(cat, this);
            contenedores.add(c);
            areaJuego.getChildren().add(c);
        }

        areaJuego.widthProperty().addListener((obs, oldVal, newVal) -> reposicionarContenedores());
        areaJuego.heightProperty().addListener((obs, oldVal, newVal) -> reposicionarContenedores());
    }

    /**
     * Crea la barra superior con el título, puntuación, tiempo, progreso
     * y botón de reinicio.
     */
    private HBox crearBarraSuperior() {
        HBox barra = new HBox(25 * escala);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(12 * escala, 25 * escala, 12 * escala, 25 * escala));
        barra.setBackground(new Background(new BackgroundFill(
            Color.web("#2E7D32"), CornerRadii.EMPTY, Insets.EMPTY
        )));
        barra.setEffect(new DropShadow(8 * escala, Color.web("#00000033")));

        Text titulo = new Text("EcoClasificador");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22 * escala));
        titulo.setFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        textoPuntuacion = new Text("\u2B50 0");
        textoPuntuacion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20 * escala));
        textoPuntuacion.setFill(Color.web("#FFD700"));

        textoTiempo = new Text("\u23F1 " + TIEMPO_LIMITE + "s");
        textoTiempo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20 * escala));
        textoTiempo.setFill(Color.WHITE);

        textoProgreso = new Text("\uD83D\uDCCA 0/" + ITEMS_POR_PARTIDA);
        textoProgreso.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18 * escala));
        textoProgreso.setFill(Color.web("#E8F5E9"));

        StackPane btnReiniciar = new StackPane();
        btnReiniciar.setPrefSize(40 * escala, 40 * escala);
        btnReiniciar.setBackground(new Background(new BackgroundFill(
            Color.web("#FFFFFF33"), new CornerRadii(20 * escala), Insets.EMPTY
        )));
        btnReiniciar.setCursor(Cursor.HAND);
        Text txtBtn = new Text("\uD83D\uDD04");
        txtBtn.setFont(Font.font("Segoe UI", 20 * escala));
        btnReiniciar.getChildren().add(txtBtn);
        btnReiniciar.setOnMouseClicked(e -> reiniciarJuego());

        barra.getChildren().addAll(titulo, spacer,
            textoPuntuacion, textoTiempo, textoProgreso, btnReiniciar);
        return barra;
    }

    // =====================================================================
    // POSICIONAMIENTO DE CONTENEDORES
    // =====================================================================

    /**
     * Reposiciona los contenedores en la parte inferior central del área de juego.
     * Se invoca al redimensionar la ventana.
     */
    private void reposicionarContenedores() {
        double anchoArea = areaJuego.getWidth();
        double altoArea  = areaJuego.getHeight();

        if (anchoArea <= 0) anchoArea = anchoVentana;
        if (altoArea  <= 0) altoArea  = altoVentana - 60;

        int n = contenedores.size();
        double espacio = 12 * escala;
        double anchoTotal = n * anchoBin + (n - 1) * espacio;
        double startX = (anchoArea - anchoTotal) / 2;
        double y = altoArea - altoBin - 25 * escala;

        for (int i = 0; i < n; i++) {
            contenedores.get(i).setLayoutX(startX + i * (anchoBin + espacio));
            contenedores.get(i).setLayoutY(y);
        }
    }

    // =====================================================================
    // INICIALIZACIÓN DEL JUEGO
    // =====================================================================

    /**
     * Inicia una nueva partida: mezcla residuos aleatorios, los distribuye
     * en el área de juego y arranca el temporizador.
     */
    private void iniciarJuego() {
        juegoActivo = true;
        GestorAudio.getInstancia().detener();
        GestorAudio.getInstancia().playGameMusic();
        puntuacion = 0;
        tiempoRestante = TIEMPO_LIMITE;
        residuosRestantes = ITEMS_POR_PARTIDA;
        itemArrastrado = null;

        actualizarTextoMarcador();

        areaJuego.getChildren().removeIf(n -> n instanceof Residuo);
        residuos.clear();

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < DATOS_RESIDUOS.length; i++) indices.add(i);
        Collections.shuffle(indices);
        indices = new ArrayList<>(indices.subList(0, ITEMS_POR_PARTIDA));

        int cols = 5;
        double margenX = 30 * escala;
        double margenY = 25 * escala;
        double anchoDisponible = areaJuego.getWidth();
        if (anchoDisponible <= 0) anchoDisponible = anchoVentana;

        double espacioX = (anchoDisponible - 2 * margenX - cols * anchoItem) / (cols + 1);
        double espacioY = 25;

        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            String archivo   = DATOS_RESIDUOS[idx][0];
            String nombre    = DATOS_RESIDUOS[idx][1];
            String categoria = DATOS_RESIDUOS[idx][2];

            Residuo r = new Residuo(archivo, nombre, categoria, this);

            int fila = i / cols;
            int col  = i % cols;
            double x = margenX + espacioX + col * (anchoItem + espacioX)
                       + (Math.random() - 0.5) * 15;
            double y = margenY + espacioY + fila * (altoItem + espacioY)
                       + (Math.random() - 0.5) * 10;

            r.setLayoutX(x);
            r.setLayoutY(y);
            r.setOrigen(x, y);

            residuos.add(r);
            areaJuego.getChildren().add(r);
        }

        reposicionarContenedores();
        iniciarTemporizador();
        Platform.runLater(this::reposicionarContenedores);
    }

    /**
     * Reinicia la partida actual: limpia el overlay de fin de juego
     * y comienza una nueva.
     */
    private void reiniciarJuego() {
        if (temporizador != null) temporizador.stop();
        if (overlayFinJuego != null && areaJuego.getChildren().contains(overlayFinJuego)) {
            areaJuego.getChildren().remove(overlayFinJuego);
            overlayFinJuego = null;
        }
        textoTiempo.setFill(Color.WHITE);
        textoTiempo.setOpacity(1.0);
        iniciarJuego();
    }

    // =====================================================================
    // TEMPORIZADOR
    // =====================================================================

    /**
     * Arranca la cuenta atrás. Cuando llega a cero, termina el juego.
     * En los últimos 10 segundos el texto parpadea en rojo.
     */
    private void iniciarTemporizador() {
        temporizador = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                tiempoRestante--;
                actualizarTextoMarcador();

                if (tiempoRestante <= 10) {
                    textoTiempo.setFill(Color.web("#FF5252"));
                    textoTiempo.setOpacity(tiempoRestante % 2 == 0 ? 0.4 : 1.0);
                }

                if (tiempoRestante <= 0) {
                    temporizador.stop();
                    terminarJuego(false);
                }
            })
        );
        temporizador.setCycleCount(TIEMPO_LIMITE);
        temporizador.play();
    }

    // =====================================================================
    // MARCADOR
    // =====================================================================

    /**
     * Actualiza los textos de puntuación, tiempo y progreso en la barra superior.
     */
    public void actualizarTextoMarcador() {
        textoPuntuacion.setText("\u2B50 " + puntuacion);
        textoTiempo.setText("\u23F1 " + tiempoRestante + "s");
        int clasificados = ITEMS_POR_PARTIDA - residuosRestantes;
        textoProgreso.setText("\uD83D\uDCCA " + clasificados + "/" + ITEMS_POR_PARTIDA);
    }

    // =====================================================================
    // FIN DEL JUEGO
    // =====================================================================

    /**
     * Finaliza la partida, muestra un overlay con el resultado y las opciones
     * de reinicio o ver el ranking.
     *
     * @param victoria true si el jugador clasificó todos los residuos
     */
    public void terminarJuego(boolean victoria) {
        juegoActivo = false;
        GestorAudio.getInstancia().detener();

        if (callbackGameOver != null) {
            callbackGameOver.onGameOver(puntuacion, tiempoRestante);
        }

        overlayFinJuego = new StackPane();
        double anchoOverlay = areaJuego.getWidth();
        double altoOverlay  = areaJuego.getHeight();
        if (anchoOverlay <= 0) anchoOverlay = anchoVentana;
        if (altoOverlay  <= 0) altoOverlay  = altoVentana - 60;
        overlayFinJuego.setPrefSize(anchoOverlay, altoOverlay);
        overlayFinJuego.setLayoutX(0);
        overlayFinJuego.setLayoutY(0);
        overlayFinJuego.setBackground(new Background(new BackgroundFill(
            Color.web("#00000088"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        VBox panel = new VBox(18 * escala);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40 * escala, 50 * escala, 40 * escala, 50 * escala));
        panel.setBackground(new Background(new BackgroundFill(
            Color.WHITE, new CornerRadii(20 * escala), Insets.EMPTY
        )));
        panel.setEffect(new DropShadow(25 * escala, Color.web("#00000055")));
        panel.setMaxWidth(420 * escala);

        String emoji, titulo, mensaje;
        if (victoria) {
            emoji   = "\uD83C\uDF89";
            titulo  = "\u00A1Felicidades!";
            mensaje = "Clasificaste TODOS los residuos correctamente.";
        } else {
            int clasificados = ITEMS_POR_PARTIDA - residuosRestantes;
            emoji   = "\u23F0";
            titulo  = "\u00A1Se acab\u00F3 el tiempo!";
            mensaje = "Clasificaste " + clasificados + " de "
                      + ITEMS_POR_PARTIDA + " residuos.";
        }

        Text txtEmoji  = new Text(emoji);
        txtEmoji.setFont(Font.font("Segoe UI", 56 * escala));

        Text txtTitulo = new Text(titulo);
        txtTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26 * escala));
        txtTitulo.setFill(Color.web("#2E7D32"));

        Text txtMensaje = new Text(mensaje);
        txtMensaje.setFont(Font.font("Segoe UI", 15 * escala));
        txtMensaje.setTextAlignment(TextAlignment.CENTER);
        txtMensaje.setFill(Color.web("#666666"));

        Text txtPuntos = new Text("Puntuaci\u00F3n final: " + puntuacion + " \u2B50");
        txtPuntos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22 * escala));
        txtPuntos.setFill(Color.web("#FFD700"));

        StackPane btnReiniciar = new StackPane();
        btnReiniciar.setPrefSize(220 * escala, 50 * escala);
        btnReiniciar.setBackground(new Background(new BackgroundFill(
            Color.web("#4CAF50"), new CornerRadii(25 * escala), Insets.EMPTY
        )));
        btnReiniciar.setCursor(Cursor.HAND);
        btnReiniciar.setEffect(new DropShadow(6 * escala, Color.web("#4CAF5066")));

        Text txtBoton = new Text("\uD83D\uDD04  Jugar de nuevo");
        txtBoton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16 * escala));
        txtBoton.setFill(Color.WHITE);
        btnReiniciar.getChildren().add(txtBoton);
        btnReiniciar.setOnMouseClicked(e -> reiniciarJuego());
        btnReiniciar.setOnMouseEntered(e -> { btnReiniciar.setScaleX(1.06); btnReiniciar.setScaleY(1.06); });
        btnReiniciar.setOnMouseExited(e  -> { btnReiniciar.setScaleX(1.0);  btnReiniciar.setScaleY(1.0); });

        StackPane btnRanking = new StackPane();
        btnRanking.setPrefSize(220 * escala, 45 * escala);
        btnRanking.setBackground(new Background(new BackgroundFill(
            Color.web("#FF8F00"), new CornerRadii(25 * escala), Insets.EMPTY
        )));
        btnRanking.setCursor(Cursor.HAND);
        btnRanking.setEffect(new DropShadow(6 * escala, Color.web("#FF8F0066")));

        Text txtRanking = new Text("\uD83C\uDFC6  Ver ranking");
        txtRanking.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15 * escala));
        txtRanking.setFill(Color.WHITE);
        btnRanking.getChildren().add(txtRanking);
        btnRanking.setOnMouseClicked(e -> {
            if (callbackRanking != null) callbackRanking.mostrarRanking();
        });
        btnRanking.setOnMouseEntered(e -> { btnRanking.setScaleX(1.06); btnRanking.setScaleY(1.06); });
        btnRanking.setOnMouseExited(e  -> { btnRanking.setScaleX(1.0);  btnRanking.setScaleY(1.0); });

        VBox botones = new VBox(12 * escala);
        botones.setAlignment(Pos.CENTER);
        botones.getChildren().addAll(btnReiniciar, btnRanking);

        panel.getChildren().addAll(txtEmoji, txtTitulo, txtMensaje, txtPuntos, botones);
        overlayFinJuego.getChildren().add(panel);

        overlayFinJuego.setOpacity(0);
        areaJuego.getChildren().add(overlayFinJuego);

        Timeline animEntrada = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(overlayFinJuego.opacityProperty(), 0)),
            new KeyFrame(Duration.millis(400), new KeyValue(overlayFinJuego.opacityProperty(), 1))
        );
        animEntrada.play();
    }

    // =====================================================================
    // ANIMACIONES
    // =====================================================================

    /**
     * Anima el residuo moviéndolo al centro del contenedor y reduciendo
     * su escala hasta desaparecer. Muestra un texto flotante "+10".
     *
     * @param r Residuo acertado
     * @param c Contenedor destino
     */
    public void animarAcierto(Residuo r, Contenedor c) {
        GestorAudio.getInstancia().playAcierto();

        double centroX = c.getLayoutX() + c.getWidth()  / 2 - r.getWidth()  / 2;
        double centroY = c.getLayoutY() + c.getHeight() / 2 - r.getHeight() / 2;

        TranslateTransition mover = new TranslateTransition(Duration.millis(350), r);
        mover.setToX(centroX - r.getLayoutX());
        mover.setToY(centroY - r.getLayoutY());
        mover.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition encoger = new ScaleTransition(Duration.millis(350), r);
        encoger.setToX(0.2);
        encoger.setToY(0.2);

        ParallelTransition animacion = new ParallelTransition(mover, encoger);
        animacion.setOnFinished(e -> r.setVisible(false));
        animacion.play();

        Text flotante = new Text("+10");
        flotante.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28 * escala));
        flotante.setFill(Color.web("#4CAF50"));
        flotante.setEffect(new DropShadow(4 * escala, Color.web("#4CAF5088")));
        flotante.setLayoutX(c.getLayoutX() + c.getWidth() / 2 - 20);
        flotante.setLayoutY(c.getLayoutY() - 5 * escala);
        areaJuego.getChildren().add(flotante);

        TranslateTransition subir = new TranslateTransition(Duration.millis(900), flotante);
        subir.setByY(-65 * escala);
        FadeTransition desvanecer = new FadeTransition(Duration.millis(900), flotante);
        desvanecer.setFromValue(1);
        desvanecer.setToValue(0);

        ParallelTransition animFloat = new ParallelTransition(subir, desvanecer);
        animFloat.setOnFinished(e -> areaJuego.getChildren().remove(flotante));
        animFloat.play();
    }

    /**
     * Anima un fallo: sacude el residuo y muestra un texto flotante "-5".
     * El residuo vuelve a su posición original automáticamente porque
     * su {@code translateX/Y} se anima a 0.
     *
     * @param r Residuo clasificado incorrectamente
     */
    public void animarFallo(Residuo r) {
        GestorAudio.getInstancia().playFallo();

        Timeline sacudida = new Timeline(
            new KeyFrame(Duration.millis(0),   e -> r.setTranslateX(8)),
            new KeyFrame(Duration.millis(50),  e -> r.setTranslateX(-8)),
            new KeyFrame(Duration.millis(100), e -> r.setTranslateX(5)),
            new KeyFrame(Duration.millis(150), e -> r.setTranslateX(-5)),
            new KeyFrame(Duration.millis(200), e -> r.setTranslateX(0))
        );
        sacudida.play();

        Text flotante = new Text("-5");
        flotante.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24 * escala));
        flotante.setFill(Color.RED);
        flotante.setEffect(new DropShadow(4 * escala, Color.web("#FF000066")));
        flotante.setLayoutX(r.getLayoutX() + r.getWidth() / 2 - 15);
        flotante.setLayoutY(r.getLayoutY() - 5 * escala);
        areaJuego.getChildren().add(flotante);

        TranslateTransition subir = new TranslateTransition(Duration.millis(700), flotante);
        subir.setByY(-45 * escala);
        FadeTransition desvanecer = new FadeTransition(Duration.millis(700), flotante);
        desvanecer.setFromValue(1);
        desvanecer.setToValue(0);

        ParallelTransition animFloat = new ParallelTransition(subir, desvanecer);
        animFloat.setOnFinished(e -> areaJuego.getChildren().remove(flotante));
        animFloat.play();

        TranslateTransition volver = new TranslateTransition(Duration.millis(250), r);
        volver.setToX(0);
        volver.setToY(0);
        volver.play();
    }

    /**
     * Anima el residuo de vuelta a su posición original cuando se suelta
     * fuera de cualquier contenedor. La animación lleva {@code translateX/Y}
     * a 0, que es la posición de reposo relativa al {@code layoutX/Y}.
     *
     * @param r Residuo a retornar
     */
    public void animarVuelta(Residuo r) {
        TranslateTransition volver = new TranslateTransition(Duration.millis(200), r);
        volver.setToX(0);
        volver.setToY(0);
        volver.setInterpolator(Interpolator.EASE_BOTH);
        volver.play();
    }
}
