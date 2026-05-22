package org.ecoclasificador;

import javafx.application.Platform;
import javafx.scene.Scene;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UtilColisionTest {

    private static JuegoReciclaje juego;
    private static Residuo residuo;
    private static Contenedor contenedor;

    @BeforeAll
    static void initJfx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
    }

    @BeforeEach
    void setupGame() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            juego = new JuegoReciclaje("Test", 900, 680);
            juego.detenerTemporizador();

            new Scene(juego);
            juego.applyCss();
            juego.layout();

            List<Residuo> residuos = juego.getAreaJuego().getChildren().stream()
                .filter(n -> n instanceof Residuo)
                .map(n -> (Residuo) n)
                .collect(Collectors.toList());
            List<Contenedor> contenedores = juego.getAreaJuego().getChildren().stream()
                .filter(n -> n instanceof Contenedor)
                .map(n -> (Contenedor) n)
                .collect(Collectors.toList());

            if (!residuos.isEmpty() && !contenedores.isEmpty()) {
                residuo = residuos.get(0);
                contenedor = contenedores.get(0);
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertNotNull(residuo, "Debe haber al menos un Residuo en el juego");
        assertNotNull(contenedor, "Debe haber al menos un Contenedor en el juego");
    }

    @AfterEach
    void cleanup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            juego = null;
            residuo = null;
            contenedor = null;
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void colisionan_itemsSuperpuestos_devuelveTrue() throws Exception {
        AtomicBoolean resultado = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                double cx = contenedor.getLayoutX();
                double cy = contenedor.getLayoutY();

                residuo.setLayoutX(cx);
                residuo.setLayoutY(cy);
                juego.layout();

                resultado.set(UtilColision.colisionan(residuo, contenedor));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(resultado.get(),
            "Residuo en la misma posición que el contenedor debe colisionar");
    }

    @Test
    void colisionan_itemsSeparados_devuelveFalse() throws Exception {
        AtomicBoolean resultado = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                residuo.setLayoutX(0);
                residuo.setLayoutY(0);
                contenedor.setLayoutX(500);
                contenedor.setLayoutY(500);

                juego.layout();

                resultado.set(UtilColision.colisionan(residuo, contenedor));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(resultado.get(),
            "Residuo y contenedor muy separados NO deben colisionar");
    }

    @Test
    void colisionan_hitboxReducidaEvitaFalsosPositivos() throws Exception {
        AtomicBoolean resultado = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                double cx = contenedor.getLayoutX();
                double cy = contenedor.getLayoutY();
                double cw = contenedor.getBoundsInParent().getWidth();
                double itemW = residuo.getBoundsInParent().getWidth();

                double hitboxOffset = itemW * (1 - 0.55) / 2;

                double itemLayoutX = cx + cw + 1 - hitboxOffset;

                residuo.setLayoutX(itemLayoutX);
                residuo.setLayoutY(cy);
                juego.layout();

                resultado.set(UtilColision.colisionan(residuo, contenedor));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(resultado.get(),
            "Residuo pegado al borde del contenedor NO debe colisionar (hitbox reducida)");
    }

    @Test
    void colisionan_esDeterminista() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean r1 = new AtomicBoolean();
        AtomicBoolean r2 = new AtomicBoolean();

        Platform.runLater(() -> {
            try {
                double cx = contenedor.getLayoutX();
                double cy = contenedor.getLayoutY();

                residuo.setLayoutX(cx);
                residuo.setLayoutY(cy);
                juego.layout();

                r1.set(UtilColision.colisionan(residuo, contenedor));
                r2.set(UtilColision.colisionan(residuo, contenedor));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(r1.get(), r2.get(),
            "La colisión debe ser determinista (mismo resultado siempre)");
    }
}
