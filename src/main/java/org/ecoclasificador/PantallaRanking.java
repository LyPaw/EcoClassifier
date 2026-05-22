package org.ecoclasificador;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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

import java.util.List;

/**
 * Pantalla de ranking que muestra las 10 mejores puntuaciones ordenadas
 * por puntos (descendente) y tiempo restante (descendente).
 * <p>
 * Utiliza {@link TableView} con un modelo {@link RankingEntry} para
 * una visualización escalable y profesional.
 * <p>
 * Se accede desde la pantalla de login o desde el overlay de fin de juego.
 */
public class PantallaRanking {

    /** Registro inmutable que representa una fila del ranking. */
    public record RankingEntry(int posicion, String nombre, int puntuacion, int tiempoRestante, String fecha) {}

    private final Stage escenario;
    private final double anchoPantalla;
    private final double altoPantalla;
    private final Runnable alVolverAJugar;
    private final Runnable alVolverAlLogin;

    /**
     * Crea la pantalla de ranking.
     *
     * @param escenario      Stage principal de la aplicación
     * @param anchoPantalla  Ancho disponible de la pantalla
     * @param altoPantalla   Alto disponible de la pantalla
     * @param alVolverAJugar Callback para volver a jugar
     * @param alVolverAlLogin Callback para volver al login
     */
    public PantallaRanking(Stage escenario, double anchoPantalla, double altoPantalla,
                           Runnable alVolverAJugar, Runnable alVolverAlLogin) {
        this.escenario       = escenario;
        this.anchoPantalla   = anchoPantalla;
        this.altoPantalla    = altoPantalla;
        this.alVolverAJugar  = alVolverAJugar;
        this.alVolverAlLogin = alVolverAlLogin;
    }

    /**
     Construye y muestra la escena del ranking.
     */
    public void mostrar() {
        VBox raiz = new VBox(20);
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.setPadding(new Insets(40));
        raiz.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2E7D32")),
                new Stop(1, Color.web("#C8E6C9"))
            ), CornerRadii.EMPTY, Insets.EMPTY
        )));

        Text titulo = new Text("\uD83C\uDFC6  Mejores Puntuaciones");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        titulo.setFill(Color.WHITE);
        titulo.setEffect(new DropShadow(6, Color.web("#00000044")));

        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(25, 30, 25, 30));
        panel.setBackground(new Background(new BackgroundFill(
            Color.WHITE, new CornerRadii(20), Insets.EMPTY
        )));
        panel.setEffect(new DropShadow(15, Color.web("#00000033")));
        panel.setMaxWidth(700);
        panel.setMaxHeight(altoPantalla * 0.75);

        TableView<RankingEntry> tabla = crearTabla();
        panel.getChildren().add(tabla);

        StackPane btnJugar = crearBoton("\uD83D\uDD04  Jugar de nuevo", "#4CAF50", alVolverAJugar);
        StackPane btnSalir = crearBoton("Salir", "#E53935", () -> escenario.close());
        btnSalir.setPrefWidth(150);

        VBox botones = new VBox(15, btnJugar, btnSalir);
        botones.setAlignment(Pos.CENTER);
        VBox.setMargin(botones, new Insets(15, 0, 0, 0));
        panel.getChildren().add(botones);

        raiz.getChildren().addAll(titulo, panel);

        Label hint = crearHintSalida();
        StackPane contenedor = new StackPane(raiz, hint);
        StackPane.setAlignment(hint, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(hint, new Insets(0, 15, 10, 0));

        Scene escena = new Scene(contenedor, anchoPantalla, altoPantalla);
        escenario.setTitle("EcoClasificador - Ranking");
        escenario.setScene(escena);
        escenario.setFullScreen(true);
    }

    /**
     * Construye la tabla de ranking con formato visual profesional:
     * columnas para posición, jugador, puntos, tiempo y fecha;
     * filas con color alterno y medallas para los 3 primeros.
     */
    private TableView<RankingEntry> crearTabla() {
        TableView<RankingEntry> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabla.setPlaceholder(new Text("Todav\u00EDa no hay puntuaciones registradas."));
        tabla.setSelectionModel(null);

        TableColumn<RankingEntry, Integer> colPos = new TableColumn<>("#");
        colPos.setPrefWidth(60);
        colPos.setSortable(false);
        colPos.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().posicion()));
        colPos.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer pos, boolean vacio) {
                super.updateItem(pos, vacio);
                if (vacio || pos == null) {
                    setText(null);
                    return;
                }
                String medalla = switch (pos) {
                    case 1 -> "\uD83E\uDD47  ";
                    case 2 -> "\uD83E\uDD48  ";
                    case 3 -> "\uD83E\uDD49  ";
                    default -> "";
                };
                setText(medalla + pos);
                setTextFill(pos == 1 ? Color.web("#FFD700") : Color.web("#333333"));
                setFont(Font.font("Segoe UI", pos <= 3 ? FontWeight.BOLD : FontWeight.NORMAL, 14));
            }
        });

        TableColumn<RankingEntry, String> colNombre = new TableColumn<>("Jugador");
        colNombre.setSortable(false);
        colNombre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().nombre()));
        colNombre.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) { setText(null); return; }
                setText(item);
                setFont(Font.font("Segoe UI", 14));
            }
        });

        TableColumn<RankingEntry, Integer> colPuntos = new TableColumn<>("Puntos");
        colPuntos.setSortable(false);
        colPuntos.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().puntuacion()));
        colPuntos.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer pts, boolean vacio) {
                super.updateItem(pts, vacio);
                if (vacio || pts == null) { setText(null); return; }
                setText(pts + " \u2B50");
                setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            }
        });

        TableColumn<RankingEntry, Integer> colTiempo = new TableColumn<>("Tiempo");
        colTiempo.setSortable(false);
        colTiempo.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().tiempoRestante()));
        colTiempo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer tiempo, boolean vacio) {
                super.updateItem(tiempo, vacio);
                if (vacio || tiempo == null) { setText(null); return; }
                setText(tiempo + "s");
                setFont(Font.font("Segoe UI", 14));
            }
        });

        TableColumn<RankingEntry, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setSortable(false);
        colFecha.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().fecha()));
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) { setText(null); return; }
                setText(item);
                setFont(Font.font("Segoe UI", 13));
            }
        });

        tabla.getColumns().addAll(colPos, colNombre, colPuntos, colTiempo, colFecha);

        List<String[]> datos = DatabaseManager.obtenerTop10();
        ObservableList<RankingEntry> items = FXCollections.observableArrayList();
        for (String[] fila : datos) {
            items.add(new RankingEntry(
                Integer.parseInt(fila[0]),
                fila[1],
                Integer.parseInt(fila[2]),
                Integer.parseInt(fila[3]),
                fila[4]
            ));
        }
        tabla.setItems(items);

        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RankingEntry item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setStyle("");
                } else {
                    setStyle(item.posicion() % 2 == 0
                        ? "-fx-background-color: #F5F5F5; -fx-table-cell-border-color: transparent;"
                        : "-fx-background-color: white; -fx-table-cell-border-color: transparent;");
                }
            }
        });

        tabla.setStyle(
            "-fx-background-radius: 10; -fx-border-radius: 10;" +
            "-fx-border-color: #E0E0E0; -fx-border-width: 1;" +
            "-fx-font-family: 'Segoe UI';"
        );

        return tabla;
    }

    /**
     * Crea un botón estilizado con el texto, color y acción especificados.
     */
    private StackPane crearBoton(String texto, String colorHex, Runnable accion) {
        StackPane btn = new StackPane();
        btn.setPrefSize(200, 45);
        btn.setBackground(new Background(new BackgroundFill(
            Color.web(colorHex), new CornerRadii(25), Insets.EMPTY
        )));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setEffect(new DropShadow(5, Color.web("#00000044")));

        Text txt = new Text(texto);
        txt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        txt.setFill(Color.WHITE);
        btn.getChildren().add(txt);
        btn.setOnMouseClicked(e -> accion.run());
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
