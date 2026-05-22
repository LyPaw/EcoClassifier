/**
 * Módulo principal de EcoClasificador.
 * <p>
 * Declara las dependencias necesarias (JavaFX, SQLite) y exporta el
 * paquete {@code org.ecoclasificador}. La directiva {@code opens} es
 * necesaria para que JavaFX pueda instanciar reflexivamente la clase
 * {@link org.ecoclasificador.AppPrincipal} al invocar
 * {@link javafx.application.Application#launch(String...)}.
 */
module org.ecoclasificador {
    requires javafx.controls;
    requires javafx.media;
    requires java.sql;

    exports org.ecoclasificador;
    opens org.ecoclasificador to javafx.graphics;
}
