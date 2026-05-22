package org.ecoclasificador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la comunicación con la base de datos SQLite para el almacenamiento
 * y recuperación de puntuaciones de los jugadores.
 * <p>
 * SQLite es una base de datos embebida (sin servidor) que almacena toda
 * la información en un único archivo local. No requiere configuración
 * adicional ni procesos externos.
 * <p>
 * La tabla {@code puntuaciones} almacena:
 * <ul>
 *   <li>{@code id} — identificador auto-incremental</li>
 *   <li>{@code nombre} — nombre del jugador</li>
 *   <li>{@code puntuacion} — puntos obtenidos en la partida</li>
 *   <li>{@code tiempo_restante} — segundos que quedaban al finalizar</li>
 *   <li>{@code fecha} — fecha y hora automática de la partida</li>
 * </ul>
 */
public final class DatabaseManager {

    private static final String URL = "jdbc:sqlite:ecoclasificador.db";

    private DatabaseManager() {}

    // =================================================================
    // INICIALIZACIÓN
    // =================================================================

    /**
     * Inicializa la base de datos creando la tabla {@code puntuaciones}
     * si aún no existe. Debe invocarse una vez al arrancar la aplicación.
     */
    public static void inicializar() {
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {

            String sql = """
                CREATE TABLE IF NOT EXISTS puntuaciones (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre          TEXT    NOT NULL,
                    puntuacion      INTEGER NOT NULL,
                    tiempo_restante INTEGER NOT NULL,
                    fecha           TEXT    DEFAULT (datetime('now', 'localtime'))
                )
                """;
            stmt.execute(sql);

        } catch (SQLException e) {
            System.err.println("Error al inicializar la BD: " + e.getMessage());
        }
    }

    // =================================================================
    // CONEXIÓN
    // =================================================================

    /**
     * Abre y devuelve una conexión a la base de datos SQLite.
     *
     * @return Conexión activa
     * @throws SQLException si no se puede establecer la conexión
     */
    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // =================================================================
    // OPERACIONES CRUD
    // =================================================================

    /**
     * Guarda la puntuación de una partida finalizada.
     *
     * @param nombre         Nombre del jugador
     * @param puntuacion     Puntos obtenidos
     * @param tiempoRestante Segundos restantes del temporizador
     */
    public static void guardarPuntuacion(String nombre, int puntuacion, int tiempoRestante) {
        String sql = "INSERT INTO puntuaciones (nombre, puntuacion, tiempo_restante) VALUES (?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setInt(2, puntuacion);
            pstmt.setInt(3, tiempoRestante);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar puntuación: " + e.getMessage());
        }
    }

    /**
     * Devuelve las 10 mejores puntuaciones ordenadas por puntos (descendente)
     * y tiempo restante (descendente) como criterio de desempate.
     * <p>
     * Cada elemento del array contiene:
     * <ol start="0">
     *   <li>Posición (1-10)</li>
     *   <li>Nombre del jugador</li>
     *   <li>Puntuación como String</li>
     *   <li>Tiempo restante como String</li>
     *   <li>Fecha de la partida</li>
     * </ol>
     *
     * @return Lista con las 10 mejores puntuaciones (vacía si no hay datos)
     */
    public static List<String[]> obtenerTop10() {
        List<String[]> ranking = new ArrayList<>();
        String sql = """
            SELECT nombre, puntuacion, tiempo_restante, fecha
            FROM puntuaciones
            ORDER BY puntuacion DESC, tiempo_restante DESC
            LIMIT 10
            """;

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int pos = 1;
            while (rs.next()) {
                ranking.add(new String[]{
                    String.valueOf(pos),
                    rs.getString("nombre"),
                    String.valueOf(rs.getInt("puntuacion")),
                    String.valueOf(rs.getInt("tiempo_restante")),
                    rs.getString("fecha")
                });
                pos++;
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ranking: " + e.getMessage());
        }

        return ranking;
    }
}
