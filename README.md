# ♻️ EcoClasificador — Juego educativo de reciclaje

**EcoClasificador** es un juego drag-and-drop desarrollado en **JavaFX 24** con **Java 25** que
pone a prueba tus conocimientos de reciclaje. Arrastra cada residuo al contenedor correcto
antes de que se acabe el tiempo.

---

## 📦 Requisitos

- **Java 25** (JDK 25+)
- **Maven 3.9+**
- **JavaFX 24** (se resuelve automáticamente vía Maven)
- **SQLite 3** (embebido, no requiere instalación)

---

## 🚀 Cómo ejecutar

```bash
mvn javafx:run
```

Esto compila y lanza la aplicación en pantalla completa. La primera vez Maven descargará
todas las dependencias (JavaFX, SQLite JDBC, JUnit).

Para compilar sin ejecutar:

```bash
mvn compile
```

---

## 🧪 Cómo testear

```bash
mvn test
```

Ejecuta los 8 tests unitarios (4 de `DatabaseManagerTest` + 4 de `UtilColisionTest`).
Los tests de colisión requieren JavaFX (se inicializa automáticamente vía `Platform.startup()`).

---

## 📁 Estructura del proyecto

```
JavaFX/
├── pom.xml                           # Build Maven: dependencias y plugins
├── ecoclasificador.db                # Base de datos SQLite (se crea sola al ejecutar)
├── README.md                         # Este archivo
│
├── asset/                            # Recursos multimedia
│   ├── audio/                        #   Música y efectos de sonido
│   │   ├── menu_music.mp3            #     Música del menú principal
│   │   ├── bgmusic.mp3               #     Música durante el juego
│   │   ├── correct.wav               #     Efecto de acierto
│   │   ├── wrong1.wav                #     Efecto de fallo
│   │   └── wrong2.wav                #     (sin usar actualmente)
│   │
│   └── *.png                         # 25 imágenes de residuos reales
│       ├── delivery-box.png          #   Papel: Caja de cartón
│       ├── milk-carton.png           #   Papel: Brick de leche
│       ├── book.png                  #   Papel: Libro
│       ├── mail.png                  #   Papel: Sobre
│       ├── newspaper.png             #   Papel: Periódico
│       ├── plastic-bottle.png        #   Plástico: Botella PET
│       ├── poly-bag.png              #   Plástico: Bolsa
│       ├── pen.png                   #   Plástico: Bolígrafo
│       ├── toothbrush.png            #   Plástico: Cepillo de dientes
│       ├── cup.png                   #   Plástico: Vaso de yogur
│       ├── glass.png                 #   Vidrio: Botella de vidrio
│       ├── jam.png                   #   Vidrio: Tarro de mermelada
│       ├── mirror.png                #   Vidrio: Espejo
│       ├── red-wine.png              #   Vidrio: Botella de vino
│       ├── ampoule.png               #   Vidrio: Bombilla
│       ├── banana.png                #   Orgánico: Plátano
│       ├── apple.png                 #   Orgánico: Manzana
│       ├── compost.png               #   Orgánico: Compost
│       ├── stink.png                 #   Orgánico: Residuo orgánico
│       ├── battery.png               #   Electrónico: Pila
│       ├── headphones.png            #   Electrónico: Auriculares
│       ├── joystick.png              #   Electrónico: Mando
│       ├── keyboard.png              #   Electrónico: Teclado
│       ├── lan.png                   #   Electrónico: Cable de red
│       └── radio.png                 #   Electrónico: Radio
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java              # Declaración del módulo Java
│   │   │   └── org/ecoclasificador/
│   │   │       ├── Main.java                 # Punto de entrada / orquestador
│   │   │       ├── PantallaLogin.java        # Pantalla de inicio (nombre)
│   │   │       ├── JuegoReciclaje.java       # Controlador principal del juego
│   │   │       ├── Residuo.java              # Item arrastrable
│   │   │       ├── Contenedor.java           # Cubo de reciclaje
│   │   │       ├── GestorAudio.java          # Singleton de audio
│   │   │       ├── DatabaseManager.java      # Conexión SQLite
│   │   │       ├── PantallaRanking.java      # Tabla de clasificación
│   │   │       └── UtilColision.java         # Detección de colisiones
│   │   │
│   └── test/
│       └── java/org/ecoclasificador/
│           ├── DatabaseManagerTest.java       # Tests de base de datos
│           └── UtilColisionTest.java          # Tests de colisión
│
└── target/                          # Compilado (generado por Maven)
```

---

## 🗺️ Flujo de la aplicación

```
inicio
  │
  ▼
Main.start()
  ├── Lee tamaño de pantalla (Screen.getPrimary().getVisualBounds())
  ├── DatabaseManager.inicializar()  → crea tabla SQLite si no existe
  ├── GestorAudio.playMenuMusic()    → suena menu_music.mp3
  └── PantallaLogin.mostrar()
        │
        ▼  (usuario escribe nombre + pulsa "¡A jugar!")
        │
  Consumer<String> → Main.iniciarJuego()
        │
        ▼
  new JuegoReciclaje(nombre, ancho, alto)
    ├── construirInterfaz()
    │     ├── BorderPane.setTop()      → barra superior (HBox)
    │     └── BorderPane.setCenter()   → areaJuego (Pane)
    │           └── 5 Contenedores añadidos (Papel, Plástico, Vidrio, Orgánico, Electrónico)
    │
    └── iniciarJuego()
          ├── GestorAudio.detener()        → para música anterior
          ├── GestorAudio.playGameMusic()  → suena bgmusic.mp3
          ├── Baraja 10 residuos aleatorios de 25
          ├── Los coloca en cuadrícula 5×2
          ├── reposicionarContenedores()   → alinea los 5 cubos abajo
          └── iniciarTemporizador()        → Timeline 90s (1 tick/s)
                │
                ▼  (usuario arrastra residuos con el ratón)
                │
          Residuo.alPresionar()
            ├── Guarda offset ratón→esquina
            ├── toFront()  → eleva sobre otros elementos
            └── escala 1.08 + sombra
                │
          Residuo.alArrastrar()
            ├── Mueve layoutX/layoutY
            ├── Muros invisibles 15px en bordes
            └── UtilColision.colisionan() con cada Contenedor
                  └── setHoverActivo() en el que coincida
                │
          Residuo.alSoltar()
            ├── Comprueba colisión con cada Contenedor
            │
            ├── SI categoría coincide → ACIERTO
            │     ├── clasificado = true
            │     ├── +10 puntos
            │     ├── GestorAudio.playAcierto()
            │     ├── animarAcierto() → se encoge y va al centro del contenedor
            │     └── Si residuosRestantes == 0 → terminarJuego(true)
            │
            ├── SI categoría NO coincide → FALLO
            │     ├── -5 puntos (mínimo 0)
            │     ├── GestorAudio.playFallo()
            │     └── animarFallo() → sacudida + vuelve a origen
            │
            └── SI no colisiona con ninguno
                  └── animarVuelta() → vuelve suavemente a origen
                │
                ▼  (tiempo llega a 0 O todos clasificados)
                │
          terminarJuego(victoria)
            ├── juegoActivo = false
            ├── GestorAudio.detener() → para música
            ├── callbackGameOver.onGameOver() → DatabaseManager.guardarPuntuacion()
            └── Muestra overlay con resultados:
                  ├── Emoji (🎉/⏰)
                  ├── Mensaje
                  ├── Puntuación final
                  ├── Botón "Jugar de nuevo" → reiniciarJuego()
                  └── Botón "Ver ranking" → PantallaRanking
                        │
                        ▼  (usuario pulsa "Ver ranking")
                        │
                  PantallaRanking.mostrar()
                    ├── DatabaseManager.obtenerTop10()
                    ├── Tabla con posiciones, nombres, puntuaciones y fechas
                    ├── Medallas 🥇🥈🥉 para los 3 primeros
                    ├── Botón "Jugar de nuevo" → vuelve al juego
                    └── Botón "Salir" → escenario.close()
```

---

## 📐 Decisiones de diseño

### 1. Separación en clases pequeñas con responsabilidades claras

| Clase | Responsabilidad |
|---|---|
| `Main` | Orquestar pantallas, inicializar BD y audio |
| `PantallaLogin` | Recoger nombre del jugador |
| `JuegoReciclaje` | Controlar la lógica del juego (temporizador, puntuación, animaciones) |
| `Residuo` | Representar y manejar el drag-and-drop de cada item |
| `Contenedor` | Representar visualmente cada categoría de reciclaje |
| `GestorAudio` | Gestionar toda la reproducción de sonido (singleton) |
| `DatabaseManager` | Manejar toda la comunicación con SQLite |
| `PantallaRanking` | Mostrar la tabla de clasificación |
| `UtilColision` | Detectar colisiones con hitbox reducida |

Cada clase es independiente y se comunica con las demás mediante **callbacks** (interfaces
funcionales) o **métodos públicos** (getters/setters). Esto facilita el estudio y la
modificación del código.

### 2. Drag-and-drop con MouseEvent (no Dragboard)

En lugar del sistema DragBoard de JavaFX (más complejo y acoplado), el arrastre se
implementa con tres eventos de ratón:

- `MOUSE_PRESSED`  → registrar offset y posición de origen
- `MOUSE_DRAGGED`  → actualizar `layoutX`/`layoutY` directamente
- `MOUSE_RELEASED` → comprobar colisiones y actuar

Esto da control total sobre el comportamiento y es más fácil de entender para
estudiantes.

### 3. Hitbox reducida al 55%

```java
FACTOR_HITBOX = 0.55;
```

La bounding box del residuo se reduce al 55% centrado para evitar que un item
entre dos contenedores active ambos simultáneamente. Cálculo:

```
hitboxX = anchoItem * 0.55
hitboxY = altoItem * 0.55
offsetX = anchoItem * (1 - 0.55) / 2
offsetY = altoItem * (1 - 0.55) / 2
```

Esto significa que el residuo debe estar aproximadamente ~22.5% más cerca del centro
del contenedor para que la colisión se active.

### 4. Escalado uniforme a cualquier resolución

```java
escala = Math.min(anchoPantalla / 900, altoPantalla / 680);
```

Todos los tamaños (items, contenedores, fuentes, márgenes) se multiplican por `escala`.
La resolución base es 900×680. En una pantalla 1920×1080, escala = min(2.13, 1.59) = 1.59.
Todo se agranda proporcionalmente sin deformarse.

### 5. SQLite embebido (sin servidor)

La base de datos es un único archivo `ecoclasificador.db` que se crea automáticamente.
Ventajas:
- No requiere instalar ni configurar un servidor
- El archivo se puede copiar, borrar o compartir
- Perfecto para aplicaciones de escritorio monousuario

### 6. Callbacks para comunicación entre pantallas

Las transiciones entre pantallas usan interfaces funcionales:

```java
@FunctionalInterface
public interface GameOverCallback {
    void onGameOver(int puntuacion, int tiempoRestante);
}

@FunctionalInterface
public interface RankingCallback {
    void mostrarRanking();
}
```

`Main` registra los callbacks y `JuegoReciclaje` los invoca cuando corresponde.

### 7. Imágenes reales sin pistas de color

Los residuos se muestran con imágenes de productos reales (no iconos de colores).
El único color visible es el del contenedor. Esto obliga al jugador a **pensar**
qué material es cada objeto, no a asociar colores.

### 8. Tres Scene separadas

Cada pantalla (Login, Juego, Ranking) es una `Scene` independiente que se asigna
al mismo `Stage`. Esto es más limpio que superponer nodos y permite un ciclo de
vida claro para cada pantalla.

---

## 🧠 Explicación detallada de cada archivo

### `pom.xml`

```xml
<project>
    <groupId>org.ecoclasificador</groupId>
    <artifactId>eco-clasificador</artifactId>
    <version>1.0</version>
    ...
</project>
```

**Dependencias:**

| Dependencia | Versión | Ámbito | Propósito |
|---|---|---|---|
| `javafx-controls` | 24 | compile | Scene, Stage, layouts (BorderPane, VBox, HBox, Pane, StackPane), nodos (Text, Rectangle, ImageView), animaciones (Timeline, TranslateTransition) |
| `javafx-media` | 24 | compile | Reproducir audio: Media, MediaPlayer para MP3 y WAV |
| `sqlite-jdbc` | 3.45.3.0 | compile | Driver JDBC para conectar a SQLite |
| `junit-jupiter` | 5.11.4 | test | Framework JUnit 5 para tests unitarios |

**Plugins:**

| Plugin | Propósito |
|---|---|
| `javafx-maven-plugin` 0.0.8 | Ejecutar `mvn javafx:run` → lanza la aplicación |
| `maven-compiler-plugin` 3.12.1 | Compila con Java 25 (source/target 25) |
| `maven-surefire-plugin` 3.2.5 | Ejecuta tests con JUnit 5, configurado con `useModulePath=false` para que los tests puedan usar JavaFX desde classpath |

---

### `module-info.java`

```java
module org.ecoclasificador {
    requires javafx.controls;
    requires javafx.media;
    requires java.sql;
    exports org.ecoclasificador;
}
```

Declara el módulo `org.ecoclasificador` y sus dependencias:
- **javafx.controls**: todos los componentes gráficos (Stage, Scene, Button, Pane...)
- **javafx.media**: reproducción de audio (Media, MediaPlayer)
- **java.sql**: conectividad JDBC (DriverManager, Connection, PreparedStatement)

`exports org.ecoclasificador` hace que el paquete sea accesible desde otros módulos
(necesario para que los tests puedan usar las clases).

---

### `Main.java`

**Clase:** `public class Main extends Application`

**Propósito:** Punto de entrada de la aplicación. Gestiona las transiciones entre las
tres pantallas: Login → Juego → Ranking.

**Atributos:**

| Atributo | Tipo | Descripción |
|---|---|---|
| `escenario` | `Stage` | Ventana principal de la aplicación |
| `anchoPantalla` | `double` | Ancho de la pantalla (obtenido de Screen) |
| `altoPantalla` | `double` | Alto de la pantalla |
| `nombreJugador` | `String` | Nombre introducido en el login |

**Métodos:**

| Método | Visibilidad | Descripción |
|---|---|---|
| `start(Stage)` | `@Override public` | Inicializa BD, obtiene tamaño de pantalla, muestra login |
| `mostrarLogin()` | `private` | Crea PantallaLogin con callback que recibe el nombre y llama a `iniciarJuego()` |
| `iniciarJuego()` | `private` | Crea JuegoReciclaje, configura callbacks, cambia la Scene |
| `mostrarRanking()` | `private` | Crea PantallaRanking (desde login) |
| `mostrarRankingDesdeJuego()` | `private` | Crea PantallaRanking (desde el juego, con callback para volver a jugar) |
| `main(String[])` | `public static` | Punto de entrada Java: `launch(args)` |

**Decisión importante:** PantallaLogin recibe `Consumer<String>` (no `Runnable`)
para evitar el problema de "variable local no inicializada en lambda" que ocurriría
con `Runnable` + variable externa.

---

### `PantallaLogin.java`

**Clase:** `public class PantallaLogin` (NO extiende Node, es una clase independiente)

**Propósito:** Pantalla de inicio donde el jugador introduce su nombre.

**Constructor:**
```java
public PantallaLogin(Stage escenario, double anchoPantalla, double altoPantalla,
                     Consumer<String> alIniciarJuego, Runnable alMostrarRanking)
```

**Método clave: `mostrar()`**

Construye la Scene con:
- Gradiente verde de fondo (oscuro arriba → claro abajo)
- Título "♻️ EcoClasificador" con sombra
- Subtítulo "Juego educativo de reciclaje"
- Formulario blanco centrado con:
  - Etiqueta "Introduce tu nombre para jugar"
  - `TextField` con placeholder "Ej: Ana, Carlos, María..."
  - Label rojo para error (nombre vacío)
  - Botón "♻️ ¡A jugar!" con efecto hover (escala 1.05)
- Botón "🏆 Ver mejores puntuaciones" (transparente)
- Hint "ESC para salir de p. completa" (abajo a la derecha)

El botón "Jugar" y el Enter en el campo de texto disparan el callback `alIniciarJuego`
con el nombre validado (no vacío).

---

### `JuegoReciclaje.java`

**Clase:** `public class JuegoReciclaje extends BorderPane`

**Propósito:** Controlador principal del juego. Gestiona el tablero, temporizador,
puntuación, residuos, contenedores, animaciones y fin de partida.

Es la clase más grande (~587 líneas) y central del proyecto.

**Constantes públicas:**

| Constante | Valor | Descripción |
|---|---|---|
| `DATOS_RESIDUOS` | `String[25][3]` | Catálogo completo: `{archivo, nombre, categoría}` |
| `PUNTOS_ACIERTO` | `10` | Puntos que suma cuando acierta |
| `PUNTOS_FALLO` | `-5` | Puntos que resta cuando falla |
| `ITEMS_POR_PARTIDA` | `10` | Número de residuos por ronda |
| `TIEMPO_LIMITE` | `90` | Segundos totales (10 × 9) |

**Categorías y colores:**

| Categoría | Color | Código |
|---|---|---|
| Papel | Azul | `#1976D2` |
| Plástico | Amarillo | `#F9A825` |
| Vidrio | Verde | `#388E3C` |
| Orgánico | Marrón | `#5D4037` |
| Electrónico | Púrpura | `#7B1FA2` |

**Arquitectura visual:**

```
JuegoReciclaje (BorderPane)
│
├── TOP: HBox (barra superior, fondo verde #2E7D32)
│     ├── Título "EcoClasificador" (blanco, bold 22×escala)
│     ├── Region (spacer elástico)
│     ├── TextoPuntuacion "⭐ 0" (dorado)
│     ├── TextoTiempo "⏱ 90s" (blanco, parpadea rojo últimos 10s)
│     ├── TextoProgreso "📊 0/10" (verde claro)
│     └── Botón reiniciar "🔄" (círculo transparente)
│
└── CENTER: Pane (areaJuego)
      ├── 5 Contenedores (abajo, centrados horizontalmente)
      │     ├── Papel (azul)
      │     ├── Plástico (amarillo)
      │     ├── Vidrio (verde)
      │     ├── Orgánico (marrón)
      │     └── Electrónico (púrpura)
      │
      ├── 10 Residuos (cuadrícula 5×2)
      │     └── Cada uno con imagen + nombre + categoría oculta
      │
      └── Overlay (al terminar)
            └── Panel blanco centrado con:
                  ├── Emoji (🎉 victoria / ⏰ tiempo)
                  ├── Título
                  ├── Mensaje
                  ├── Puntuación ⭐
                  ├── Botón "Jugar de nuevo" (verde)
                  └── Botón "Ver ranking" (naranja)
```

**Métodos públicos:**

| Método | Retorno | Descripción |
|---|---|---|
| `isJuegoActivo()` | `boolean` | True mientras la partida está en curso |
| `getAreaJuego()` | `Pane` | El panel central donde están items y contenedores |
| `getAnchoVentana()` | `double` | Ancho de la ventana |
| `getAltoVentana()` | `double` | Alto de la ventana |
| `getAnchoItem()` | `double` | Ancho de cada residuo (130×escala) |
| `getAltoItem()` | `double` | Alto de cada residuo (130×escala) |
| `getAnchoBin()` | `double` | Ancho de cada contenedor (160×escala) |
| `getAltoBin()` | `double` | Alto de cada contenedor (120×escala) |
| `getEscala()` | `double` | Factor de escala (min(ancho/900, alto/680)) |
| `getFuenteItem()` | `double` | Tamaño de fuente del nombre del item (13×escala) |
| `getFuenteBin()` | `double` | Tamaño de fuente del contenedor (14×escala) |
| `getItemArrastrado()` | `Residuo` | Residuo que se está arrastrando actualmente |
| `setItemArrastrado(Residuo)` | `void` | Registra qué residuo se arrastra |
| `getContenedores()` | `List<Contenedor>` | Lista de los 5 contenedores |
| `getResiduosRestantes()` | `int` | Cuántos residuos faltan por clasificar |
| `setResiduosRestantes(int)` | `void` | Actualiza el contador |
| `getPuntuacion()` | `int` | Puntuación actual del jugador |
| `setPuntuacion(int)` | `void` | Actualiza la puntuación |
| `detenerTemporizador()` | `void` | Detiene el Timeline del temporizador |
| `actualizarTextoMarcador()` | `void` | Refresca los textos de puntuación, tiempo y progreso |
| `terminarJuego(boolean)` | `void` | Finaliza la partida (victoria o derrota) |
| `animarAcierto(Residuo, Contenedor)` | `void` | Animación cuando acierta |
| `animarFallo(Residuo)` | `void` | Animación cuando falla |
| `animarVuelta(Residuo)` | `void` | Animación cuando vuelve a origen |
| `setOnGameOver(GameOverCallback)` | `void` | Registra callback de fin de juego |
| `setOnMostrarRanking(RankingCallback)` | `void` | Registra callback para mostrar ranking |
| `colorCategoria(String)` | `static Color` | Devuelve el color asociado a una categoría |
| `iconoCategoria(String)` | `static String` | Devuelve el emoji asociado a una categoría |

**Métodos privados:**

| Método | Descripción |
|---|---|
| `construirInterfaz()` | Crea la barra superior, el areaJuego y los 5 contenedores |
| `crearBarraSuperior()` | Construye el HBox superior con título, puntuación, tiempo, progreso y reinicio |
| `reposicionarContenedores()` | Alinea los 5 contenedores en la parte inferior centrada |
| `iniciarJuego()` | Prepara la partida: elige 10 residuos, los coloca en cuadrícula, arranca temporizador |
| `reiniciarJuego()` | Limpia estado actual y llama a `iniciarJuego()` |
| `iniciarTemporizador()` | Crea y arranca el Timeline de 90 ticks (1 por segundo) |

**Callbacks:**

```java
@FunctionalInterface
public interface GameOverCallback {
    void onGameOver(int puntuacion, int tiempoRestante);
}

@FunctionalInterface
public interface RankingCallback {
    void mostrarRanking();
}
```

**Temporizador:**

```java
temporizador = new Timeline(
    new KeyFrame(Duration.seconds(1), e -> {
        tiempoRestante--;
        actualizarTextoMarcador();
        if (tiempoRestante <= 10) {
            textoTiempo.setFill(Color.web("#FF5252"));
            textoTiempo.setOpacity(tiempoRestante % 2 == 0 ? 0.4 : 1.0);
        }
        if (tiempoRestante <= 0) {
            temporizador.stop();
            terminarJuego(false);
        }
    })
);
temporizador.setCycleCount(TIEMPO_LIMITE);  // 90 ciclos
```

Cada segundo descuenta 1. Los últimos 10 segundos el texto parpadea en rojo.
Al llegar a 0, termina el juego con derrota.

**Animaciones:**

`animarAcierto()`:
1. Calcula centro del contenedor
2. `TranslateTransition` 350ms → mueve el residuo al centro
3. `ScaleTransition` 350ms → encoge a 0.2 (desaparece)
4. `ParallelTransition` combina ambos movimientos
5. Al terminar: `setVisible(false)`
6. Texto flotante "+10" que sube y se desvanece en 900ms

`animarFallo()`:
1. `Timeline` de 5 fotogramas → sacudida horizontal (8,-8,5,-5,0) en 200ms
2. Texto flotante "-5" en rojo que sube y se desvanece en 700ms
3. `TranslateTransition` 250ms → vuelve el residuo a su posición original

`animarVuelta()`:
1. `TranslateTransition` 200ms → vuelve el residuo a origen (translateX/Y a 0)

---

### `Residuo.java`

**Clase:** `public class Residuo extends StackPane`

**Propósito:** Cada residuo que el jugador arrastra. Contiene la imagen, el nombre
y la lógica de drag-and-drop.

**Atributos (package-private para acceso desde test y JuegoReciclaje):**

| Atributo | Tipo | Descripción |
|---|---|---|
| `nombre` | `String` | Nombre visible (ej: "Periódico") |
| `categoria` | `String` | Categoría real (ej: "Papel") |
| `colorCategoria` | `Color` | Color asociado a su categoría |
| `nombreArchivo` | `String` | Nombre del PNG en asset/ |
| `origenX` | `double` | Posición X inicial (para volver tras fallo) |
| `origenY` | `double` | Posición Y inicial |
| `clasificado` | `boolean` | True cuando ya fue depositado correctamente |

**Atributos privados:**

| Atributo | Descripción |
|---|---|
| `juego` | Referencia a JuegoReciclaje para invocar métodos |
| `dx, dy` | Offset del ratón respecto a la esquina del elemento |

**Eventos de ratón:**

| Evento | Handler | Descripción |
|---|---|---|
| `MOUSE_PRESSED` | `alPresionar()` | Guarda offset, eleva el nodo (`toFront()`), escala 1.08, añade sombra |
| `MOUSE_DRAGGED` | `alArrastrar()` | Actualiza layoutX/layoutY con muros de 15px, comprueba hover |
| `MOUSE_RELEASED` | `alSoltar()` | Si colisiona con el contenedor correcto → clasifica; si no → falla o vuelve |
| `MOUSE_ENTERED` | Cambia cursor a HAND | |
| `MOUSE_EXITED` | Cambia cursor a DEFAULT | |

**Muros invisibles (en alArrastrar):**

```java
nuevaX = Math.max(15, Math.min(limiteX - anchoItem - 15, nuevaX));
nuevaY = Math.max(15, Math.min(limiteY - altoItem - 15, nuevaY));
```

El residuo no puede salir de los límites del areaJuego. Hay un margen de 15px
en los 4 bordes para que no quede pegado al borde.

**Lógica de soltar (alSoltar):**

```
alSoltar()
  ├── Si no es el item arrastrado O juego inactivo O ya clasificado → ignorar
  │
  ├── Limpiar estado de arrastre (itemArrastrado = null)
  ├── Restaurar escala y sombra
  ├── Desactivar hover en todos los contenedores
  │
  └── Por cada contenedor:
        ├── Si UtilColision.colisionan(this, contenedor):
        │     ├── Si categoria coincide:
        │     │     ├── clasificado = true
        │     │     ├── residuosRestantes--
        │     │     ├── puntuacion += 10
        │     │     ├── animarAcierto()
        │     │     └── Si residuosRestantes == 0 → terminarJuego(true)
        │     │
        │     └── Si NO coincide:
        │           ├── puntuacion = max(0, puntuacion - 5)
        │           ├── animarFallo()
        │           └── return (categoría mostrada para depuración...)
        │
        └── Si no colisiona con ninguno:
              └── animarVuelta()
```

**Método `construirVisual()`:**

1. Crea `Rectangle` blanco con bordes redondeados (arcWidth = 16×escala) + sombra
2. Carga la imagen desde `"file:asset/" + nombreArchivo` dentro de try-catch
3. La imagen se redimensiona a `min(anchoItem×0.45, altoItem×0.40)` manteniendo proporción
4. Texto con el nombre del residuo (fuente 13×escala, color #333333)
5. VBox centra la imagen y el texto verticalmente
6. StackPane apila el fondo y el VBox

---

### `Contenedor.java`

**Clase:** `public class Contenedor extends StackPane`

**Propósito:** Cubo de reciclaje donde se depositan los residuos.

**Constructor:**
```java
public Contenedor(String categoria, JuegoReciclaje juego)
```

Recibe la categoría (ej: "Papel") y la referencia al juego (para obtener tamaños).

**Método `setHoverActivo(boolean)`:**

Ilumina el contenedor cuando un residuo pasa por encima:
- Activo: escala 1.06 + sombra blanca resplandeciente
- Inactivo: escala 1.0 + sin sombra

**Visual:**

Cada contenedor muestra:
- Rectángulo de color (categoría) con bordes redondeados (18×escala)
- Esquina blanca semitransparente (stroke #FFFFFF 0.3)
- Sombra suave
- Emoji grande (32×escala) + nombre de categoría en mayúsculas (bold 14×escala)
- VBox centra el emoji y el texto

---

### `UtilColision.java`

**Clase:** `public final class UtilColision`

**Propósito:** Utilidad estática con un único método público para detectar colisiones.

**Por qué `final` + constructor privado:** Patrón de clase utilitaria (como Math o
Collections). No se puede instanciar ni heredar.

**Método único:**

```java
public static boolean colisionan(Residuo r, Contenedor c)
```

**Algoritmo:**

1. Obtiene `getBoundsInParent()` del residuo y del contenedor
2. Calcula offset de la hitbox reducida:
   - `sx = bR.getWidth() * (1 - 0.55) / 2`  → 22.5% de cada lado
   - `sy = bR.getHeight() * (1 - 0.55) / 2`
3. Crea un `BoundingBox` nuevo centrado con el 55% del tamaño original
4. Comprueba `hitbox.intersects(contenedorBounds)`

**¿Por qué getBoundsInParent?** Porque residuos y contenedores son hijos directos
del mismo `Pane` (areaJuego). `getBoundsInParent()` devuelve las coordenadas en el
espacio del padre, por lo que son directamente comparables.

---

### `DatabaseManager.java`

**Clase:** `public class DatabaseManager`

**Propósito:** Capa de persistencia. Toda la comunicación con SQLite.

**URL de conexión:** `"jdbc:sqlite:ecoclasificador.db"`

**Tabla `puntuaciones`:**

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Identificador único |
| `nombre` | TEXT NOT NULL | Nombre del jugador |
| `puntuacion` | INTEGER NOT NULL | Puntos obtenidos |
| `tiempo_restante` | INTEGER NOT NULL | Segundos que quedaban al terminar |
| `fecha` | TEXT DEFAULT (datetime('now','localtime')) | Fecha y hora automáticas |

**Métodos estáticos:**

| Método | Descripción |
|---|---|
| `inicializar()` | Crea la tabla si no existe. Se llama al arrancar la app |
| `guardarPuntuacion(nombre, puntuacion, tiempoRestante)` | INSERT con PreparedStatement (protege SQL injection) |
| `obtenerTop10()` | SELECT ordenado por puntuacion DESC, tiempo_restante DESC LIMIT 10. Devuelve List<String[]> con [posición, nombre, puntuacion, tiempo_restante, fecha] |

Todas las operaciones usan try-with-resources para cerrar automáticamente las
conexiones y statements.

---

### `GestorAudio.java`

**Clase:** `public class GestorAudio`

**Propósito:** Singleton que centraliza toda la reproducción de audio.

**Estructura:**

```java
private static GestorAudio instancia;
private MediaPlayer reproductorMusica;        // para música de fondo
private List<MediaPlayer> efectosActivos;     // para efectos de sonido (evita GC)
```

**Métodos públicos:**

| Método | Archivo | Volumen | Loop | Descripción |
|---|---|---|---|---|
| `getInstancia()` | — | — | — | Devuelve la única instancia (singleton) |
| `playMenuMusic()` | `asset/audio/menu_music.mp3` | 0.25 | Infinito | Música del menú principal |
| `playGameMusic()` | `asset/audio/bgmusic.mp3` | 0.25 | Infinito | Música durante el juego |
| `playAcierto()` | `asset/audio/correct.wav` | 0.6 | No | Efecto de acierto |
| `playFallo()` | `asset/audio/wrong1.wav` | 0.6 | No | Efecto de fallo |
| `detener()` | — | — | — | Para música y efectos, libera recursos |

**Protección contra GC de efectos:** Los MediaPlayer de efectos se almacenan en
`efectosActivos` (ArrayList) para evitar que el recolector de basura los elimine
mientras se están reproduciendo. Al terminar (`setOnEndOfMedia`), se eliminan de la
lista y se liberan.

**Manejo de errores:** Todos los accesos a archivos están envueltos en try-catch.
Si un archivo de audio no existe o no se puede cargar, simplemente no hay sonido
(sin crash).

---

### `PantallaRanking.java`

**Clase:** `public class PantallaRanking` (NO extiende Node)

**Propósito:** Muestra la tabla de clasificación con las 10 mejores puntuaciones.

**Constructor:**
```java
public PantallaRanking(Stage escenario, double anchoPantalla, double altoPantalla,
                       Runnable alVolverAJugar, Runnable alVolverAlLogin)
```

**Método `mostrar()`:**

1. Consulta `DatabaseManager.obtenerTop10()`
2. Construye tabla con:
   - Encabezado verde (#2E7D32) con columnas: #, Jugador, Puntos, Tiempo, Fecha
   - Filas alternadas (fondo gris claro en pares)
   - Medallas: 🥇 posición 1, 🥈 posición 2, 🥉 posición 3
   - Color dorado para el primero, bold para el top 3
3. Si no hay puntuaciones: muestra mensaje "Todavía no hay puntuaciones registradas."
4. Botones:
   - "🔄 Jugar de nuevo" (verde) → callback `alVolverAJugar`
   - "Salir" (rojo) → `escenario.close()`

---

## 🐛 Bugs resueltos

### Bug 1: La música del juego no se oía

**Síntoma:** La música del menú se oye correctamente, pero al empezar a jugar
no hay música de fondo.

**Causa:** En `Main.iniciarJuego()` (línea 55) había una llamada a
`GestorAudio.getInstancia().detener()` que se ejecutaba **después** de que el
constructor de `JuegoReciclaje` ya hubiera llamado a `playGameMusic()`.

Flujo problemático:
```
Main.iniciarJuego()
  ├── new JuegoReciclaje(...)         → constructor
  │     └── iniciarJuego()
  │           ├── GestorAudio.detener()      ← para música del menú
  │           └── GestorAudio.playGameMusic() ← ARRANCA música del juego
  │
  ├── setOnGameOver(...)
  ├── setOnMostrarRanking(...)
  ├── GestorAudio.detener()             ← ¡DETIENE la música que acababa de arrancar!
  └── escenario.setScene(...)
```

**Solución:** Eliminar la línea `GestorAudio.getInstancia().detener()` de
`Main.iniciarJuego()`.

### Bug 2: Efectos de sonido se cortaban a veces

**Síntoma:** Al acertar o fallar, el sonido a veces no se oía completo o se
cortaba antes de terminar.

**Causa:** En `GestorAudio.playAcierto()` y `playFallo()`, el `MediaPlayer` se
creaba como variable local. Al salir del método, la única referencia al objeto
era a través del lambda `mp::dispose` en `setOnEndOfMedia`. El Garbage Collector
podía considerar el `MediaPlayer` como alcanzable débilmente y recolectarlo
antes de que terminara la reproducción.

**Solución:** Añadir una lista `List<MediaPlayer> efectosActivos` en `GestorAudio`
que retiene referencias fuertes a los MediaPlayer de efectos hasta que terminan
de reproducirse:

```java
private final List<MediaPlayer> efectosActivos = new ArrayList<>();

public void playAcierto() {
    ...
    efectosActivos.add(mp);
    mp.setOnEndOfMedia(() -> {
        mp.dispose();
        efectosActivos.remove(mp);
    });
    ...
}
```

---

## ✅ Tests unitarios

### `DatabaseManagerTest` (4 tests)

| Test | Descripción |
|---|---|
| `guardarPuntuacion_insertaRegistro` | Inserta un registro y verifica con SELECT COUNT que existe |
| `obtenerTop10_contieneDatosGuardados` | Inserta 3 registros y comprueba que aparecen en el top 10 con valores correctos |
| `obtenerTop10_devuelveOrdenDescendente` | Inserta 4 registros con puntuaciones diferentes y verifica que el ORDER BY es descendente |
| `obtenerTop10_sinDatos_devuelveListaVacia` | Limpia datos de prueba y verifica que no aparecen |

Los datos de prueba usan prefijo `UTEST_%` y se limpian en `@AfterEach`.

### `UtilColisionTest` (4 tests)

Requieren JavaFX (se inicializa con `Platform.startup()`).

| Test | Descripción |
|---|---|
| `colisionan_itemsSuperpuestos_devuelveTrue` | Residuo y contenedor en la misma posición → colisionan |
| `colisionan_itemsSeparados_devuelveFalse` | A 500px de distancia → NO colisionan |
| `colisionan_hitboxReducidaEvitaFalsosPositivos` | Residuo justo en el borde del contenedor → NO colisiona (el hitbox reducido lo evita) |
| `colisionan_esDeterminista` | Llamar dos veces al método da exactamente el mismo resultado |

Usan `CountDownLatch` + `Platform.runLater()` para sincronizar el hilo de tests
con el hilo de JavaFX, y `AtomicBoolean` para capturar resultados.
