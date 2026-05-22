package org.ecoclasificador;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            try {
                Path log = Paths.get(System.getProperty("java.io.tmpdir"), "EcoClassifier-error.log");
                Files.writeString(log, sw.toString());
            } catch (Exception ex) {
                System.err.println(sw);
            }
        });
        AppPrincipal.main(args);
    }
}
