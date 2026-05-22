package org.ecoclasificador;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Representa un contenedor de reciclaje donde el jugador deposita los residuos.
 * <p>
 * Cada contenedor pertenece a una categoría (Papel, Plástico, Vidrio, Orgánico,
 * Electrónico) y se muestra visualmente con un color distintivo, un icono
 * representativo y el nombre de la categoría. Cuando un residuo se arrastra
 * sobre él, activa un efecto hover que indica que es un destino válido.
 */
public class Contenedor extends StackPane {

    private final String categoria;
    private final Color color;
    private final JuegoReciclaje juego;
    private boolean hover;

    /**
     * Crea un contenedor para la categoría especificada.
     *
     * @param categoria Categoría que acepta este contenedor (Papel, Plástico, etc.)
     * @param juego     Referencia al juego principal para obtener dimensiones escaladas
     */
    public Contenedor(String categoria, JuegoReciclaje juego) {
        this.categoria = categoria;
        this.color     = JuegoReciclaje.colorCategoria(categoria);
        this.juego     = juego;
        construirVisual();
    }

    /**
     * Devuelve la categoría de este contenedor.
     *
     * @return Categoría del contenedor (ej: "Papel", "Plástico")
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * Construye la apariencia visual del contenedor: fondo redondeado,
     * icono de categoría y texto con el nombre.
     */
    private void construirVisual() {
        double anchoBin  = juego.getAnchoBin();
        double altoBin   = juego.getAltoBin();
        double escala    = juego.getEscala();
        double fuenteBin = juego.getFuenteBin();

        setPrefSize(anchoBin, altoBin);

        Rectangle cuerpo = new Rectangle(anchoBin, altoBin);
        cuerpo.setArcWidth(18 * escala);
        cuerpo.setArcHeight(18 * escala);
        cuerpo.setFill(color);
        cuerpo.setStroke(Color.web("#FFFFFF", 0.3));
        cuerpo.setStrokeWidth(3 * escala);
        cuerpo.setEffect(new DropShadow(8 * escala, Color.web("#00000033")));

        Text icono = new Text(JuegoReciclaje.iconoCategoria(categoria));
        icono.setFont(Font.font("Segoe UI", 32 * escala));

        Text texto = new Text(categoria.toUpperCase());
        texto.setFont(Font.font("Segoe UI", FontWeight.BOLD, fuenteBin));
        texto.setFill(Color.WHITE);

        VBox contenido = new VBox(3 * escala, icono, texto);
        contenido.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().addAll(cuerpo, contenido);
    }

    /**
     * Activa o desactiva el efecto visual de hover (resaltado) cuando
     * un residuo se arrastra sobre este contenedor.
     *
     * @param activo true para iluminar el contenedor, false para restaurarlo
     */
    public void setHoverActivo(boolean activo) {
        if (this.hover == activo) return;
        this.hover = activo;

        if (activo) {
            setScaleX(1.06);
            setScaleY(1.06);
            setEffect(new DropShadow(18, Color.web("#FFFFFF99")));
        } else {
            setScaleX(1.0);
            setScaleY(1.0);
            setEffect(null);
        }
    }
}
