package org.ecoclasificador;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Pantalla de inicio de sesión del juego.
 * <p>
 * Solicita al jugador que introduzca su nombre para comenzar una partida.
 * También ofrece un acceso directo al ranking de puntuaciones.
 * <p>
 * La comunicación con {@link Main} se realiza mediante callbacks:
 * <ul>
 *   <li>{@code alIniciarJuego} — se invoca con el nombre cuando el jugador pulsa "Jugar"</li>
 *   <li>{@code alMostrarRanking} — se invoca cuando el jugador quiere ver el ranking</li>
 * </ul>
 */
public class PantallaLogin {

    private final Stage escenario;
    private final double anchoPantalla;
    private final double altoPantalla;
    private final Consumer<String> alIniciarJuego;
    private final Runnable alMostrarRanking;

    private TextField campoNombre;

    /**
     * Crea la pantalla de login.
     *
     * @param escenario       Stage principal de la aplicación
     * @param anchoPantalla   Ancho disponible de la pantalla
     * @param altoPantalla    Alto disponible de la pantalla
     * @param alIniciarJuego  Callback al iniciar juego (recibe el nombre)
     * @param alMostrarRanking Callback para mostrar el ranking
     */
    public PantallaLogin(Stage escenario, double anchoPantalla, double altoPantalla,
                         Consumer<String> alIniciarJuego, Runnable alMostrarRanking) {
        this.escenario        = escenario;
        this.anchoPantalla    = anchoPantalla;
        this.altoPantalla     = altoPantalla;
        this.alIniciarJuego   = alIniciarJuego;
        this.alMostrarRanking = alMostrarRanking;
    }

    /**
     * Construye y muestra la escena de login con el formulario de nombre,
     * botón de juego y acceso al ranking.
     */
    public void mostrar() {
        VBox raiz = new VBox(20);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(50));
        raiz.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2E7D32")),
                new Stop(1, Color.web("#C8E6C9"))
            ), CornerRadii.EMPTY, Insets.EMPTY
        )));

        Text titulo = new Text("\u267B EcoClasificador");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titulo.setFill(Color.WHITE);
        titulo.setEffect(new DropShadow(8, Color.web("#00000044")));

        Text subtitulo = new Text("Juego educativo de reciclaje");
        subtitulo.setFont(Font.font("Segoe UI", 18));
        subtitulo.setFill(Color.web("#E8F5E9"));

        VBox formulario = new VBox(18);
        formulario.setAlignment(Pos.CENTER);
        formulario.setPadding(new Insets(40, 60, 40, 60));
        formulario.setBackground(new Background(new BackgroundFill(
            Color.WHITE, new CornerRadii(20), Insets.EMPTY
        )));
        formulario.setEffect(new DropShadow(20, Color.web("#00000044")));
        formulario.setMaxWidth(400);

        Text etiqueta = new Text("Introduce tu nombre para jugar");
        etiqueta.setFont(Font.font("Segoe UI", 16));
        etiqueta.setFill(Color.web("#555555"));

        campoNombre = new TextField();
        campoNombre.setPromptText("Ej: Ana, Carlos, Mar\u00EDa...");
        campoNombre.setPrefHeight(45);
        campoNombre.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-color: #CCCCCC;" +
            "-fx-padding: 8 15;"
        );
        campoNombre.requestFocus();

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Segoe UI", 13));

        StackPane btnJugar = crearBoton("\u267B  \u00A1A jugar!", "#4CAF50", 250, 50, 18);
        Runnable iniciar = () -> {
            String nombre = campoNombre.getText().trim();
            if (nombre.isEmpty()) {
                errorLabel.setText("Por favor, introduce tu nombre.");
                return;
            }
            alIniciarJuego.accept(nombre);
        };
        btnJugar.setOnMouseClicked(e -> iniciar.run());
        campoNombre.setOnAction(e -> iniciar.run());

        formulario.getChildren().addAll(etiqueta, campoNombre, errorLabel, btnJugar);
        raiz.getChildren().addAll(titulo, subtitulo, formulario);

        StackPane btnRanking = crearBoton("\uD83C\uDFC6  Ver mejores puntuaciones", "#FFFFFF33", 220, 40, 14);
        btnRanking.setOnMouseClicked(e -> alMostrarRanking.run());

        VBox.setMargin(btnRanking, new Insets(10, 0, 0, 0));
        raiz.getChildren().add(btnRanking);

        Label hint = crearHintSalida();
        StackPane contenedor = new StackPane(raiz, hint);
        StackPane.setAlignment(hint, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(hint, new Insets(0, 15, 10, 0));

        Scene escena = new Scene(contenedor, anchoPantalla, altoPantalla);
        escenario.setTitle("EcoClasificador - Inicio");
        escenario.setScene(escena);
        escenario.setFullScreen(true);
        escenario.show();
    }

    /**
     * Crea un botón estilizado con texto, color de fondo, tamaño y fuente.
     */
    private StackPane crearBoton(String texto, String colorHex, double ancho, double alto, int fontSize) {
        StackPane btn = new StackPane();
        btn.setPrefSize(ancho, alto);
        btn.setBackground(new Background(new BackgroundFill(
            Color.web(colorHex), new CornerRadii(25), Insets.EMPTY
        )));
        btn.setCursor(Cursor.HAND);
        btn.setEffect(new DropShadow(6, Color.web("#00000044")));

        Text txt = new Text(texto);
        txt.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        txt.setFill(Color.WHITE);
        btn.getChildren().add(txt);

        btn.setOnMouseEntered(e -> { btn.setScaleX(1.05); btn.setScaleY(1.05); });
        btn.setOnMouseExited(e  -> { btn.setScaleX(1.0);  btn.setScaleY(1.0); });

        return btn;
    }

    private Label crearHintSalida() {
        Label hint = new Label("ESC para salir de p. completa");
        hint.setFont(Font.font("Segoe UI", 11));
        hint.setTextFill(Color.web("#FFFFFF88"));
        hint.setBackground(new Background(new BackgroundFill(
            Color.web("#00000044"), new CornerRadii(6), Insets.EMPTY
        )));
        hint.setPadding(new Insets(4, 10, 4, 10));
        return hint;
    }
}
