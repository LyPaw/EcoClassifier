package org.ecoclasificador;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación JavaFX EcoClasificador.
 * <p>
 * Gestiona el ciclo de vida completo de la aplicación y las transiciones
 * entre las tres pantallas principales:
 * <ol>
 *   <li>{@link PantallaLogin} — introducción del nombre del jugador</li>
 *   <li>{@link JuegoReciclaje} — partida de clasificación de residuos</li>
 *   <li>{@link PantallaRanking} — mejores puntuaciones guardadas</li>
 * </ol>
 * <p>
 * La comunicación entre pantallas se realiza mediante callbacks funcionales
 * ({@link java.util.function.Consumer} y {@link Runnable}), evitando
 * acoplamientos rígidos.
 * <p>
 * Esta clase extiende {@link Application} y debe ser lanzada a través
 * del launcher {@link Main} para que el empaquetado con jpackage funcione
 * correctamente (la clase con el método {@code main} no debe extender
 * {@code Application}).
 */
public class AppPrincipal extends Application {

    private Stage escenario;
    private double anchoPantalla;
    private double altoPantalla;
    private String nombreJugador;

    @Override
    public void start(Stage escenario) {
        try {
            this.escenario = escenario;

            Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
            anchoPantalla = pantalla.getWidth();
            altoPantalla  = pantalla.getHeight();

            DatabaseManager.inicializar();
            escenario.setFullScreenExitHint("");

            mostrarLogin();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de inicio");
            alert.setHeaderText("La aplicación no pudo iniciarse");
            alert.setContentText(sw.toString());
            alert.showAndWait();
            Platform.exit();
        }
    }

    /**
     * Muestra la pantalla de login. Al introducir un nombre válido,
     * se inicia el juego; también permite acceder al ranking.
     */
    private void mostrarLogin() {
        PantallaLogin login = new PantallaLogin(
            escenario, anchoPantalla, altoPantalla,
            nombre -> { nombreJugador = nombre; iniciarJuego(); },
            this::mostrarRanking
        );
        GestorAudio.getInstancia().playMenuMusic();
        login.mostrar();
    }

    /**
     * Inicia una nueva partida. El constructor de {@link JuegoReciclaje}
     * ya se encarga de iniciar la música del juego.
     */
    private void iniciarJuego() {
        JuegoReciclaje juego = new JuegoReciclaje(nombreJugador, anchoPantalla, altoPantalla);

        juego.setOnGameOver((puntuacion, tiempoRestante) -> {
            DatabaseManager.guardarPuntuacion(nombreJugador, puntuacion, tiempoRestante);
        });

        juego.setOnMostrarRanking(() -> mostrarRankingDesdeJuego());

        var escenaJuego = new javafx.scene.Scene(juego, anchoPantalla, altoPantalla);
        escenario.setTitle("EcoClasificador - Jugando");
        escenario.setScene(escenaJuego);
        escenario.setFullScreen(true);
    }

    /**
     * Muestra el ranking desde la pantalla de login.
     */
    private void mostrarRanking() {
        PantallaRanking ranking = new PantallaRanking(
            escenario, anchoPantalla, altoPantalla,
            this::mostrarLogin,
            this::mostrarLogin
        );
        GestorAudio.getInstancia().detener();
        ranking.mostrar();
    }

    /**
     * Muestra el ranking desde el overlay de fin de juego.
     * Al volver a jugar, si hay un nombre registrado inicia
     * directamente una nueva partida; si no, vuelve al login.
     */
    private void mostrarRankingDesdeJuego() {
        PantallaRanking ranking = new PantallaRanking(
            escenario, anchoPantalla, altoPantalla,
            () -> {
                if (nombreJugador != null) {
                    iniciarJuego();
                } else {
                    mostrarLogin();
                }
            },
            this::mostrarLogin
        );
        ranking.mostrar();
    }

    public static void main(String[] args) {
        launch(AppPrincipal.class, args);
    }
}
