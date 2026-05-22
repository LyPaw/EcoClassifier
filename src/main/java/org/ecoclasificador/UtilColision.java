package org.ecoclasificador;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

/**
 * Utilidad estática para la detección de colisiones entre residuos
 * y contenedores en el área de juego.
 * <p>
 * La hitbox del residuo se reduce al 55 % de su tamaño visual para
 * evitar falsos positivos cuando el elemento se encuentra entre dos
 * contenedores o tangente a los bordes de uno.
 * <p>
 * Esta clase es de utilidad pura (sin estado) y no debe ser instanciada.
 */
public final class UtilColision {

    /** Factor de reducción de la hitbox del residuo (0.55 = 55 % del tamaño visual) */
    private static final double FACTOR_HITBOX = 0.55;

    private UtilColision() {}

    /**
     * Comprueba si la hitbox reducida de un {@link Residuo} intersecta
     * con el área de un {@link Contenedor}.
     * <p>
     * La hitbox del residuo se centra reduciendo sus dimensiones
     * uniformemente según {@link #FACTOR_HITBOX}.
     *
     * @param r Residuo arrastrado por el jugador
     * @param c Contenedor a comprobar
     * @return {@code true} si la hitbox reducida del residuo intersecta al contenedor
     */
    public static boolean colisionan(Residuo r, Contenedor c) {
        Bounds bR = r.getBoundsInParent();
        Bounds bC = c.getBoundsInParent();

        double sx = bR.getWidth()  * (1 - FACTOR_HITBOX) / 2;
        double sy = bR.getHeight() * (1 - FACTOR_HITBOX) / 2;

        Bounds hitbox = new BoundingBox(
            bR.getMinX() + sx,
            bR.getMinY() + sy,
            bR.getWidth()  * FACTOR_HITBOX,
            bR.getHeight() * FACTOR_HITBOX
        );

        return hitbox.intersects(bC);
    }
}
