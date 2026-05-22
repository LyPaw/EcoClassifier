package org.ecoclasificador;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de audio singleton. Centraliza la reproducción de música de fondo
 * y efectos de sonido para toda la aplicación.
 * <p>
 * Los archivos de audio se cargan desde el classpath ({@code /asset/audio/})
 * para garantizar que funcionan tanto en desarrollo como en el JAR empaquetado,
 * independientemente del directorio de trabajo.
 */
public class GestorAudio {

    private static GestorAudio instancia;

    private MediaPlayer reproductorMusica;

    /** Retiene referencias fuertes a efectos en reproducción para evitar garbage collection */
    private final List<MediaPlayer> efectosActivos = new ArrayList<>();

    private GestorAudio() {}

    /**
     * Devuelve la única instancia del gestor de audio (patrón singleton).
     *
     * @return Instancia global de GestorAudio
     */
    public static GestorAudio getInstancia() {
        if (instancia == null) {
            instancia = new GestorAudio();
        }
        return instancia;
    }

    /**
     * Inicia la música de fondo del menú principal en bucle infinito.
     */
    public void playMenuMusic() {
        detener();
        reproductorMusica = crearReproductor("/asset/audio/menu_music.mp3", 0.25, MediaPlayer.INDEFINITE);
    }

    /**
     * Inicia la música de fondo durante el juego en bucle infinito.
     */
    public void playGameMusic() {
        detener();
        reproductorMusica = crearReproductor("/asset/audio/bgmusic.mp3", 0.25, MediaPlayer.INDEFINITE);
    }

    /**
     * Detiene toda reproducción de música y efectos activos.
     */
    public void detener() {
        if (reproductorMusica != null) {
            reproductorMusica.stop();
            reproductorMusica.dispose();
            reproductorMusica = null;
        }
        for (MediaPlayer mp : efectosActivos) {
            mp.stop();
            mp.dispose();
        }
        efectosActivos.clear();
    }

    /**
     * Reproduce el sonido breve de acierto.
     */
    public void playAcierto() {
        reproducirEfecto("/asset/audio/correct.wav", 0.6);
    }

    /**
     * Reproduce el sonido breve de fallo.
     */
    public void playFallo() {
        reproducirEfecto("/asset/audio/wrong1.wav", 0.6);
    }

    /**
     * Crea un reproductor con bucle para música de fondo.
     *
     * @param rutaClasspath Ruta del recurso dentro del classpath
     * @param volumen       Volumen (0.0 - 1.0)
     * @param ciclo         Número de repeticiones (INDEFINITE para bucle)
     * @return MediaPlayer configurado o null si no se pudo cargar
     */
    private MediaPlayer crearReproductor(String rutaClasspath, double volumen, int ciclo) {
        try {
            String url = GestorAudio.class.getResource(rutaClasspath).toExternalForm();
            MediaPlayer mp = new MediaPlayer(new Media(url));
            mp.setCycleCount(ciclo);
            mp.setVolume(volumen);
            mp.play();
            return mp;
        } catch (Exception e) {
            System.err.println("No se pudo cargar audio: " + rutaClasspath);
            return null;
        }
    }

    /**
     * Reproduce un efecto de sonido de una sola vez y lo gestiona
     * en la lista de efectos activos para evitar GC prematuro.
     *
     * @param rutaClasspath Ruta del recurso dentro del classpath
     * @param volumen       Volumen (0.0 - 1.0)
     */
    private void reproducirEfecto(String rutaClasspath, double volumen) {
        try {
            String url = GestorAudio.class.getResource(rutaClasspath).toExternalForm();
            MediaPlayer mp = new MediaPlayer(new Media(url));
            mp.setVolume(volumen);
            efectosActivos.add(mp);
            mp.setOnEndOfMedia(() -> {
                mp.dispose();
                efectosActivos.remove(mp);
            });
            mp.play();
        } catch (Exception e) {
            System.err.println("No se pudo reproducir efecto: " + rutaClasspath);
        }
    }
}
