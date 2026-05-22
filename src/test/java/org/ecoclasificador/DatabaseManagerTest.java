package org.ecoclasificador;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class DatabaseManagerTest {

    @BeforeAll
    static void initDB() {
        DatabaseManager.inicializar();
    }

    @AfterEach
    void cleanTestData() {
        try (Connection conn = DriverManager.getConnection(DatabaseManager.getUrl());
             PreparedStatement ps = conn.prepareStatement("DELETE FROM puntuaciones WHERE nombre LIKE ?")) {
            ps.setString(1, "UTEST_%");
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Error limpiando datos de prueba: " + e.getMessage());
        }
    }

    @Test
    void guardarPuntuacion_insertaRegistro() {
        DatabaseManager.guardarPuntuacion("UTEST_GPT_1", 50, 30);

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getUrl());
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM puntuaciones WHERE nombre = ?")) {
            ps.setString(1, "UTEST_GPT_1");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void obtenerTop10_contieneDatosGuardados() {
        DatabaseManager.guardarPuntuacion("UTEST_TOP_A", 200, 10);
        DatabaseManager.guardarPuntuacion("UTEST_TOP_B", 150, 20);
        DatabaseManager.guardarPuntuacion("UTEST_TOP_C", 300, 5);

        List<String[]> ranking = DatabaseManager.obtenerTop10();

        boolean foundA = false, foundB = false, foundC = false;
        for (String[] entry : ranking) {
            String n = entry[1];
            if ("UTEST_TOP_A".equals(n)) { foundA = true; assertEquals("200", entry[2]); assertEquals("10", entry[3]); }
            if ("UTEST_TOP_B".equals(n)) { foundB = true; assertEquals("150", entry[2]); assertEquals("20", entry[3]); }
            if ("UTEST_TOP_C".equals(n)) { foundC = true; assertEquals("300", entry[2]); assertEquals("5", entry[3]); }
        }
        assertTrue(foundA, "UTEST_TOP_A debe aparecer en el ranking");
        assertTrue(foundB, "UTEST_TOP_B debe aparecer en el ranking");
        assertTrue(foundC, "UTEST_TOP_C debe aparecer en el ranking");
    }

    @Test
    void obtenerTop10_devuelveOrdenDescendente() {
        DatabaseManager.guardarPuntuacion("UTEST_ORD_A", 100, 30);
        DatabaseManager.guardarPuntuacion("UTEST_ORD_B", 300, 10);
        DatabaseManager.guardarPuntuacion("UTEST_ORD_C", 200, 0);
        DatabaseManager.guardarPuntuacion("UTEST_ORD_D", 400, 20);

        List<String[]> ranking = DatabaseManager.obtenerTop10();

        int puntuacionAnterior = Integer.MAX_VALUE;
        int contTest = 0;
        for (String[] entry : ranking) {
            if (entry[1].startsWith("UTEST_ORD_")) {
                int p = Integer.parseInt(entry[2]);
                assertTrue(p <= puntuacionAnterior,
                    "Puntuaciones deben estar en orden descendente. Se esperaba ≤ " + puntuacionAnterior + " pero se obtuvo " + p);
                puntuacionAnterior = p;
                contTest++;
            }
        }
        assertEquals(4, contTest, "Las 4 entradas de prueba deben aparecer en el top 10");
    }

    @Test
    void obtenerTop10_sinDatos_devuelveListaVacia() {
        cleanTestData();

        List<String[]> ranking = DatabaseManager.obtenerTop10();

        assertNotNull(ranking);
        for (String[] entry : ranking) {
            assertFalse(entry[1].startsWith("UTEST_"),
                "No deben aparecer entradas de prueba después de limpiar");
        }
    }
}
