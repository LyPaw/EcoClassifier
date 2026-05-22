package org.ecoclasificador;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseManager {

    private static final String DB_NAME = "ecoclasificador.db";
    private static final String DIR_NAME = "EcoClassifier";
    private static String url;

    static {
        inicializarRuta();
    }

    private DatabaseManager() {}

    private static void inicializarRuta() {
        String baseDir;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            baseDir = System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            baseDir = System.getProperty("user.home") + "/Library/Application Support";
        } else {
            baseDir = System.getProperty("user.home") + "/.local/share";
        }
        Path dir = Paths.get(baseDir, DIR_NAME);
        Path dbPath = dir.resolve(DB_NAME);

        try {
            Files.createDirectories(dir);
            if (!Files.exists(dbPath)) {
                try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream(DB_NAME)) {
                    if (in != null) {
                        Files.copy(in, dbPath, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        // Si no hay DB empaquetada, se crea vacía al conectar
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al inicializar la carpeta de datos: " + e.getMessage());
        }

        url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
    }

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

    static String getUrl() {
        return url;
    }

    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(url);
    }

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
