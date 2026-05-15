# Flappy Bird en Java + LWJGL/OpenGL

## Descripcion general

Este proyecto es un juego estilo Flappy Bird desarrollado en Java con LWJGL, GLFW y OpenGL 3.3. El juego usa renderizado 2D con coordenadas NDC, sin imagenes externas ni texturas: todos los elementos visibles se dibujan con figuras geometricas generadas en OpenGL.

El objetivo es que los jugadores se mantengan en el aire, pasen entre las tuberias y acumulen puntos. La partida termina cuando ambos jugadores quedan eliminados.

## Requerimientos implementados

### 2.1 Pajaro compuesto por figuras geometricas

El pajaro ya no es un rectangulo simple. Ahora se dibuja como una figura compuesta usando primitivas geometricas:

- elipses para cuerpo, cara y ojo;
- triangulos para cola;
- poligonos para cresta/cabeza y ala;
- rectangulos rotados para pico, detalles y franja de color.

La clase principal de esta mejora es `BirdRenderer`, que usa `ShapeRenderer` para dibujar las figuras. La apariencia cambia segun el jugador:

- jugador 1: pajaro rojo/rosado;
- jugador 2: pajaro azul;
- jugador eliminado: pajaro gris.

Tambien se aprovecha la velocidad vertical del pajaro para inclinarlo y mover el ala. Asi, cuando sube o cae, la figura se ve mas dinamica sin cambiar la logica de colision.

Archivos relacionados:

- `src/main/java/com/graphics/renderers/BirdRenderer.java`
- `src/main/java/com/graphics/renderers/ShapeRenderer.java`
- `src/main/java/com/graphics/Bird.java`

### 2.2 Modo de dos jugadores simultaneos

El juego permite que dos jugadores participen al mismo tiempo en la misma partida. Cada jugador tiene su propio pajaro, posicion, estado de vida y puntaje.

Controles:

- Jugador 1: `SPACE`
- Jugador 2: `W` o flecha `UP`
- Reiniciar: `R` cuando termina la partida
- Salir: `ESC`

La clase `Game` mantiene dos instancias de `Bird`:

- `player1`, ubicado mas adelante en la pantalla;
- `player2`, ubicado un poco mas atras.

Cada jugador puede saltar de forma independiente. Si un jugador choca o sale de la pantalla, queda eliminado, pero el otro puede seguir jugando. El `game over` solo ocurre cuando ambos jugadores estan muertos.

El puntaje tambien es independiente. Cada tuberia recuerda si ya fue puntuada por el jugador 1 o por el jugador 2 para evitar sumar dos veces al mismo jugador.

Archivos relacionados:

- `src/main/java/com/graphics/Game.java`
- `src/main/java/com/graphics/InputManager.java`
- `src/main/java/com/graphics/Pipe.java`
- `src/main/java/com/graphics/PipeManager.java`

### 2.3 Incremento progresivo de la velocidad

La dificultad aumenta de forma progresiva segun el puntaje mas alto entre los dos jugadores. Como las tuberias son compartidas, se usa el mayor puntaje para que la partida completa suba de nivel.

La dificultad se maneja con niveles:

- empieza en nivel 1;
- sube cada 5 puntos;
- llega como maximo al nivel 5.

Al subir de nivel ocurren dos cambios:

- las tuberias se mueven mas rapido;
- las tuberias aparecen con menor intervalo de tiempo.

Los valores principales estan en `PipeManager`:

- velocidad base: `0.62`
- incremento por nivel: `0.08`
- velocidad maxima: `1.00`
- intervalo base de aparicion: `1.5s`
- intervalo minimo: `1.0s`

La clase `Game` actualiza la dificultad despues de sumar los puntos de cada jugador, por lo que el nuevo nivel se aplica durante la partida.

Archivos relacionados:

- `src/main/java/com/graphics/PipeManager.java`
- `src/main/java/com/graphics/Pipe.java`
- `src/main/java/com/graphics/Game.java`

### 2.4 Mejora de la interfaz del juego

La interfaz visual fue mejorada separando el renderizado en clases especializadas y agregando mas elementos graficos.

Mejoras principales:

- fondo nocturno con degradado, luna, estrellas, nubes y ciudad;
- tuberias con bordes, sombras, brillos, tapas y franjas;
- HUD con puntajes de ambos jugadores;
- indicador visual de nivel;
- barras de dificultad para velocidad y frecuencia de aparicion;
- pantalla inicial con indicaciones visuales de controles;
- pantalla de game over con overlay y figura central;
- colores distintos para identificar a cada jugador.

Estas mejoras se dibujan con primitivas de OpenGL, sin depender de imagenes externas. `Renderer` coordina el orden de dibujo, mientras que las clases especializadas se encargan de cada parte visual.

Archivos relacionados:

- `src/main/java/com/graphics/Renderer.java`
- `src/main/java/com/graphics/renderers/BackgroundRenderer.java`
- `src/main/java/com/graphics/renderers/PipeRenderer.java`
- `src/main/java/com/graphics/renderers/HudRenderer.java`
- `src/main/java/com/graphics/renderers/BirdRenderer.java`
- `src/main/java/com/graphics/renderers/ShapeRenderer.java`

## Estructura principal del codigo

- `AppFlappyBird`: punto de entrada del programa.
- `Game`: controla ventana, game loop, estados, jugadores, puntajes y reinicio.
- `Bird`: guarda posicion, velocidad, fisica y caja de colision del pajaro.
- `InputManager`: lee teclado y detecta pulsaciones nuevas.
- `Pipe`: representa una tuberia y sus datos de puntaje por jugador.
- `PipeManager`: administra tuberias, colisiones, puntaje y dificultad.
- `Renderer`: coordina el renderizado general.
- `ShapeRenderer`: dibuja rectangulos, triangulos, poligonos y elipses.
- `BackgroundRenderer`: dibuja el fondo mejorado.
- `PipeRenderer`: dibuja tuberias mejoradas.
- `BirdRenderer`: dibuja los pajaros geometricos.
- `HudRenderer`: dibuja puntajes, nivel, inicio y game over.

## Flujo general del juego

```text
AppFlappyBird.main()
    -> Game.run()
        -> init()
        -> resetGame()
        -> loop()
            -> processInput()
            -> update(dt)
            -> render()
        -> cleanup()
```

En cada frame:

1. Se calcula `delta time`.
2. Se leen las teclas.
3. Se actualizan jugadores, tuberias, colisiones, puntajes y dificultad.
4. Se renderiza fondo, tuberias, pajaros, HUD y pantallas de estado.
5. Se intercambian buffers con GLFW.

## Como ejecutar

Requisitos:

- Java 17
- Maven
- Windows, porque el `pom.xml` usa natives de LWJGL para Windows

Comando:

```bash
mvn clean compile exec:java
```

La clase principal configurada en Maven es:

```text
com.graphics.AppFlappyBird
```

## Resumen para defensa

El proyecto cumple los cuatro requerimientos solicitados pero no en su totalidad. El pajaro esta construido con figuras geometricas, existen dos jugadores simultaneos con controles y puntajes independientes, la dificultad aumenta progresivamente por nivel y la interfaz fue mejorada con fondo, HUD, tuberias detalladas y pantallas de inicio/game over. Todo se mantiene organizado en clases separadas para que la logica del juego, el input, la dificultad y el renderizado sean faciles de explicar y revisar.
