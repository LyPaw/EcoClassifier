package org.ecoclasificador;

/**
 * Launcher principal de la aplicación EcoClasificador.
 * <p>
 * Esta clase NO extiende {@link javafx.application.Application} para que el
 * empaquetado con jpackage funcione correctamente. Simplemente delega en
 * {@link AppPrincipal#main(String[])}, que a su vez invoca
 * {@link javafx.application.Application#launch(String...)}.
 * <p>
 * Uso:
 * <ul>
 *   <li>Ejecutar como JAR: {@code java -jar eco-clasificador.jar}</li>
 *   <li>Ejecutar con Maven: {@code mvn javafx:run}</li>
 *   <li>Empaquetar: jpackage utiliza esta clase como punto de entrada</li>
 * </ul>
 */
public class Main {

    /**
     * Punto de entrada de la aplicación. Delega en {@link AppPrincipal#main(String[])}.
     *
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        AppPrincipal.main(args);
    }
}
