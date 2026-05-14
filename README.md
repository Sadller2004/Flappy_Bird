# Flappy Bird en Java + LWJGL/OpenGL

## 1. Descripcion general

Este proyecto es un mini juego estilo Flappy Bird hecho en Java usando LWJGL y OpenGL 3.3 core profile. El jugador controla un pajaro representado por un rectangulo. El objetivo es pasar entre tuberias, sumar puntos y evitar colisiones.

El renderizado es 2D y usa coordenadas NDC directas de OpenGL, sin texturas. Todos los objetos visibles son rectangulos dibujados a partir de un mismo quad base. Ese quad se transforma con uniforms para cambiar posicion, escala y color.

La logica esta separada en clases sencillas para que el flujo sea facil de defender:

- `Game` coordina el ciclo principal.
- `Bird` representa al jugador.
- `Pipe` representa una tuberia.
- `PipeManager` administra todas las tuberias.
- `InputManager` lee teclado.
- `Renderer` encapsula OpenGL.

## 2. Estructura del proyecto

### AppFlappyBird.java

Es el punto de entrada del programa. Su `main()` solo crea un `Game` y llama a `run()`.

Esto mantiene claro donde empieza la aplicacion:

```java
new Game().run();
```

### Game.java

Coordina todo el juego. Es la clase que sabe en que orden ocurren las cosas:

- inicializa GLFW y OpenGL,
- reinicia la partida,
- ejecuta el game loop,
- procesa input,
- actualiza logica,
- renderiza,
- limpia recursos al cerrar.

Tambien guarda el estado general:

- `started`: indica si la partida ya empezo.
- `gameOver`: indica si la partida termino.
- `score`: puntaje actual.

### Bird.java

Representa al jugador. Guarda:

- posicion X fija,
- posicion Y variable,
- velocidad vertical,
- ancho y alto,
- gravedad,
- impulso de salto,
- limite de velocidad de caida.

No dibuja y no lee teclado. Solo modela la fisica del pajaro.

### InputManager.java

Centraliza la lectura del teclado con GLFW. Detecta:

- `SPACE` para empezar, saltar o reiniciar,
- `R` para reiniciar en game over,
- `ESC` para cerrar la ventana.

Usa deteccion de flanco para que una tecla mantenida no se tome como muchas pulsaciones.

### Pipe.java

Representa una tuberia completa. Aunque visualmente se ven dos rectangulos, una instancia de `Pipe` contiene:

- posicion X,
- centro del gap,
- ancho,
- alto del gap,
- estado `scored` para no sumar dos veces.

Tambien calcula los limites necesarios para colisiones y renderizado.

### PipeManager.java

Administra la lista de tuberias activas. Se encarga de:

- crear tuberias cada cierto tiempo,
- moverlas,
- eliminarlas si salen de pantalla,
- detectar si el pajaro paso una tuberia,
- calcular cuantos puntos se suman,
- detectar colisiones.

Devuelve un `UpdateResult` para informar a `Game` si hubo puntaje o colision.

### Renderer.java

Encapsula OpenGL. Se encarga de:

- crear shaders,
- crear VAO y VBO,
- dibujar rectangulos,
- limpiar el fondo,
- dibujar pajaro,
- dibujar tuberias,
- dibujar overlay de game over,
- liberar recursos OpenGL.

## 3. Flujo general del programa

El flujo desde que inicia el programa es:

1. `AppFlappyBird.main()`
2. `new Game().run()`
3. `Game.init()`
4. `Game.resetGame()`
5. `Game.loop()`
6. Dentro del loop:
   - `processInput()`
   - `update(dt)`
   - `render()`
7. Al cerrar:
   - `cleanup()`

En forma resumida:

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

`Game.run()` usa un bloque `try/finally` para asegurar que `cleanup()` se ejecute incluso si ocurre un error durante el juego.

## 4. Ciclo principal del juego

El game loop es el ciclo que se repite mientras la ventana siga abierta. Cada vuelta del ciclo equivale a un frame.

En cada frame ocurre esto:

1. Se calcula `delta time`.
2. Se lee el teclado.
3. Se actualiza la fisica y la logica.
4. Se dibuja la escena.
5. Se intercambian buffers.
6. GLFW procesa eventos pendientes.

### Delta time

`delta time` o `dt` es el tiempo, en segundos, entre el frame anterior y el actual.

Se calcula con:

```java
float now = (float) GLFW.glfwGetTime();
float dt = now - lastTime;
lastTime = now;
```

Esto permite que la fisica no dependa directamente de los FPS. Si una computadora dibuja mas rapido o mas lento, el movimiento se ajusta usando el tiempo real transcurrido.

El proyecto limita `dt` con `MAX_DELTA_TIME` para evitar saltos grandes si un frame tarda demasiado.

### Buffers

OpenGL normalmente usa doble buffer:

- se dibuja en un buffer oculto,
- luego `glfwSwapBuffers(window)` lo muestra en pantalla.

Asi se evita ver la escena incompleta mientras se esta dibujando.

## 5. Como funciona el pajaro

`Bird` mantiene una posicion horizontal fija:

```java
DEFAULT_X = -0.45f
```

Eso significa que el pajaro no avanza realmente en X. Lo que se mueve son las tuberias hacia la izquierda, creando la sensacion de avance.

La posicion vertical `y` cambia por:

- gravedad,
- impulso de salto,
- velocidad vertical.

Cuando se presiona `SPACE`, `Game.processInput()` llama a:

```java
bird.jump();
```

Eso asigna una velocidad vertical positiva. Luego, en cada frame activo:

```java
bird.update(dt);
```

Dentro de `update(dt)`:

1. La gravedad reduce la velocidad vertical.
2. La velocidad de caida se limita.
3. La posicion `y` cambia segun la velocidad y el `dt`.

El choque con techo o suelo se revisa con:

```java
bird.isOutOfBounds();
```

Ese metodo compara el borde superior e inferior del pajaro contra el rango vertical de NDC: `-1` a `1`.

## 6. Como funcionan las tuberias

Una `Pipe` representa el obstaculo completo. Visualmente se dibuja como dos rectangulos:

- tuberia superior,
- tuberia inferior.

Entre ambas queda un espacio libre llamado `gap`.

Cada tuberia guarda:

- `x`: posicion horizontal,
- `gapCenterY`: centro vertical del hueco,
- `width`: ancho de la tuberia,
- `gapHeight`: alto del hueco,
- `scored`: si ya dio punto.

La tuberia se mueve hacia la izquierda con:

```java
x -= DEFAULT_SPEED * dt;
```

Sale de pantalla cuando su borde derecho queda suficientemente lejos a la izquierda:

```java
getRight() < -1.3f
```

`Pipe` tambien calcula valores utiles para render:

- `getUpperHeight()`
- `getUpperCenterY()`
- `getLowerHeight()`
- `getLowerCenterY()`

Estos metodos convierten una tuberia con gap en dos rectangulos que `Renderer` puede dibujar.

## 7. Como funciona PipeManager

`PipeManager` controla todas las tuberias activas.

### Creacion de tuberias

Usa un temporizador:

```java
spawnTimer += dt;
```

Cuando `spawnTimer` supera `SPAWN_INTERVAL`, crea una nueva tuberia:

```java
spawnPipe();
```

La nueva tuberia aparece a la derecha (`SPAWN_X = 1.2f`) y su gap tiene una altura aleatoria entre `GAP_MIN_CENTER` y `GAP_MAX_CENTER`.

### Actualizacion

En cada frame, `PipeManager.update(dt, bird)`:

1. actualiza el temporizador,
2. crea tuberias si corresponde,
3. recorre la lista de tuberias,
4. mueve cada tuberia,
5. revisa puntaje,
6. revisa colision,
7. elimina tuberias fuera de pantalla.

### Puntaje

El puntaje se detecta cuando la tuberia ya quedo detras del pajaro:

```java
pipe.canScore(bird.getX())
```

Si eso devuelve `true`:

1. se llama a `pipe.markScored()`,
2. aumenta `scoreDelta`,
3. `Game` recibe ese resultado,
4. `Game` suma al `score` global.

### Colisiones usando AABB

AABB significa `Axis-Aligned Bounding Box`. Es una tecnica de colision con rectangulos alineados a los ejes X/Y.

Primero se revisa cruce horizontal:

```java
boolean overlapX = bird.getRight() > pipe.getLeft()
        && bird.getLeft() < pipe.getRight();
```

Si no hay cruce en X, no hay colision.

Si hay cruce horizontal, el pajaro colisiona si esta fuera del gap:

```java
bird.getTop() > pipe.getGapTop()
        || bird.getBottom() < pipe.getGapBottom()
```

## 8. Como funciona el input

`InputManager` usa GLFW para consultar el estado de teclas:

```java
GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE)
```

Este enfoque se llama input polling: en cada frame se pregunta si una tecla esta presionada.

### SPACE

`SPACE` se usa para:

- empezar la partida,
- saltar,
- reiniciar si ya hay game over.

`InputManager` no decide que significa `SPACE`; solo informa si hubo una pulsacion nueva. `Game.processInput()` interpreta la accion segun el estado actual.

### R

`R` reinicia solo si `gameOver` es `true`.

### ESC

`ESC` marca la ventana para cerrarse:

```java
GLFW.glfwSetWindowShouldClose(window, true);
```

Luego el game loop termina porque:

```java
GLFW.glfwWindowShouldClose(window)
```

devuelve `true`.

### Deteccion de flanco

Se compara el estado actual de una tecla con el estado del frame anterior:

```java
boolean spacePressed = spaceNow && !previousSpace;
```

Eso significa: "SPACE esta presionado ahora, pero antes no lo estaba".

Gracias a esto, mantener `SPACE` apretado no genera muchos saltos en frames consecutivos.

## 9. Como funciona el renderizado

`Renderer` es la clase que habla directamente con OpenGL.

### Shaders

Un shader es un pequeño programa que corre en la GPU.

Este proyecto usa:

- vertex shader,
- fragment shader.

El vertex shader recibe los vertices del quad base y los transforma con:

- `uOffset`: posicion del rectangulo,
- `uScale`: tamaño del rectangulo.

El fragment shader usa:

- `uColor`: color RGB del rectangulo.

### VAO y VBO

El VBO (`Vertex Buffer Object`) guarda los vertices del quad base en memoria de GPU.

El VAO (`Vertex Array Object`) recuerda como interpretar esos vertices. En este proyecto, cada vertice tiene:

- `x`,
- `y`,
- `z`.

### Quad base

El quad base es un rectangulo unitario centrado en el origen:

```text
(-0.5, -0.5) a (0.5, 0.5)
```

Esta formado por 2 triangulos, 6 vertices en total.

Para dibujar cualquier rectangulo, `drawRect()` cambia uniforms:

```java
glUniform2f(uOffset, x, y);
glUniform2f(uScale, width, height);
glUniform3f(uColor, r, g, b);
glDrawArrays(GL_TRIANGLES, 0, 6);
```

### Orden de renderizado

En cada frame:

1. `renderBackground()` limpia la pantalla con color de cielo.
2. `renderPipes(pipes)` dibuja tuberias.
3. `renderBird(bird)` dibuja al jugador.
4. Si `gameOver` es true, `renderGameOverOverlay()` dibuja una franja oscura.

## 10. Coordenadas usadas en el juego

El juego usa NDC: `Normalized Device Coordinates`.

En OpenGL, despues del vertex shader, lo visible esta en este rango:

- X va de `-1` a `1`.
- Y va de `-1` a `1`.
- El centro de pantalla es `(0, 0)`.

Ejemplos:

- `x = -1`: borde izquierdo.
- `x = 1`: borde derecho.
- `y = -1`: borde inferior.
- `y = 1`: borde superior.
- `x = 0`, `y = 0`: centro.

Por eso las medidas del juego parecen pequeñas, como `0.10f` o `0.18f`: son proporciones dentro del espacio NDC, no pixeles.

## 11. Ejemplo completo: que pasa cuando el pajaro pasa una tuberia

Flujo exacto:

1. `Game.update(dt)` se ejecuta dentro del game loop.
2. `Game` llama a `pipeManager.update(dt, bird)`.
3. `PipeManager` recorre las tuberias activas.
4. Para cada tuberia llama a `pipe.update(dt)`.
5. `pipe.update(dt)` mueve la tuberia hacia la izquierda.
6. `PipeManager` llama a `pipe.canScore(bird.getX())`.
7. `canScore()` revisa si el borde derecho de la tuberia ya quedo detras del pajaro.
8. Si puede puntuar, `PipeManager` llama a `pipe.markScored()`.
9. `scoreDelta` aumenta en 1.
10. `PipeManager.update()` devuelve un `UpdateResult`.
11. `Game` lee `result.getScoreDelta()`.
12. Si es mayor a 0, suma ese valor a `score`.
13. `Game` llama a `updateWindowTitle()`.
14. El titulo de la ventana muestra el nuevo puntaje.

## 12. Ejemplo completo: que pasa cuando hay colision

Flujo exacto:

1. `Game.update(dt)` actualiza el pajaro con `bird.update(dt)`.
2. `Game` revisa si el pajaro salio de la pantalla con `bird.isOutOfBounds()`.
3. Si salio por techo o suelo, `Game` activa `gameOver`.
4. Si no salio, `Game` llama a `pipeManager.update(dt, bird)`.
5. `PipeManager` mueve cada tuberia.
6. Para cada tuberia llama a `collidesWithBird(pipe, bird)`.
7. `collidesWithBird()` revisa primero si hay cruce horizontal.
8. Si no hay cruce horizontal, devuelve `false`.
9. Si hay cruce horizontal, revisa si el pajaro esta fuera del gap.
10. Si esta fuera del gap, devuelve `true`.
11. `PipeManager` devuelve `UpdateResult` con `collision = true`.
12. `Game` lee `result.hasCollision()`.
13. Si hay colision, `Game` activa `gameOver = true`.
14. `Game` actualiza el titulo de ventana con mensaje de game over.
15. En el siguiente render, `Renderer` dibuja el overlay de game over.

## 13. Como podria extenderse el juego despues

Estas ideas no estan implementadas todavia, pero esta estructura deja lugares claros para agregarlas:

- Segundo jugador: crear otro `Bird` y extender `InputManager` para leer otras teclas. `Game` tendria que actualizar y renderizar ambos jugadores.
- Pajaro con figuras geometricas: modificar `Renderer.renderBird()` para dibujar varias figuras en vez de un solo rectangulo.
- Animacion de ala: agregar estado de animacion en `Bird` o una clase visual, y usar `dt` para cambiarla con el tiempo.
- Sonidos: agregar una clase nueva, por ejemplo `SoundManager`, y llamarla desde `Game` cuando haya salto, punto o colision.
- Menu inicial: agregar nuevos estados en `Game`, como `MENU`, `PLAYING` y `GAME_OVER`, o reemplazar los booleanos por un enum.
- Puntaje visual dentro de OpenGL: crear un sistema de texto, o una representacion simple con figuras, y dibujarlo desde `Renderer`.

Importante: para extender, conviene no mezclar input, fisica y render en una sola clase. La separacion actual ayuda a evitar eso.

## 14. Orden recomendado para estudiar el codigo

1. `AppFlappyBird`: entender donde empieza el programa.
2. `Game`: entender el ciclo principal y el orden input/update/render.
3. `Bird`: entender fisica del jugador.
4. `Pipe` y `PipeManager`: entender obstaculos, puntaje y colisiones.
5. `InputManager`: entender teclado y deteccion de flanco.
6. `Renderer`: entender OpenGL, shaders, VAO/VBO y dibujo.

## Mapa mental del flujo del juego

```text
AppFlappyBird
    ↓
Game.run()
    ↓
init()
    ↓
resetGame()
    ↓
loop()
    ├── processInput()
    │      ├── InputManager.poll(window)
    │      ├── SPACE: empezar / saltar / reiniciar
    │      ├── R: reiniciar en game over
    │      └── ESC: cerrar ventana
    ├── update(dt)
    │      ├── bird.update(dt)
    │      ├── bird.isOutOfBounds()
    │      ├── pipeManager.update(dt, bird)
    │      ├── puntaje
    │      └── colisiones
    └── render()
           ├── fondo
           ├── tuberias
           ├── pajaro
           └── overlay game over
```

## Conceptos clave para defender

### GLFW

GLFW es la biblioteca que permite crear la ventana, leer teclado y manejar el contexto OpenGL. LWJGL permite usar GLFW desde Java.

### OpenGL context

El contexto OpenGL conecta las llamadas de OpenGL con una ventana y con la GPU. Antes de crear shaders o buffers, el contexto debe existir y estar activo.

### Shader

Un shader es codigo que corre en la GPU. En este proyecto:

- el vertex shader posiciona vertices,
- el fragment shader pinta pixeles.

### Uniform

Un uniform es una variable del shader que Java puede cambiar antes de dibujar. Aqui se usan uniforms para cambiar posicion, escala y color sin crear nuevos vertices para cada objeto.

### VAO

El VAO recuerda como estan organizados los vertices. En core profile es importante tener un VAO activo al dibujar.

### VBO

El VBO guarda vertices en memoria de GPU. Aqui guarda el quad base reutilizable.

### Render

Renderizar es convertir el estado actual del juego en una imagen en pantalla.

### Update

Actualizar es avanzar la simulacion del juego: fisica, tuberias, puntaje y colisiones.

### AABB collision

Es colision entre rectangulos alineados a los ejes. Es simple y suficiente para este juego porque el pajaro y las tuberias son rectangulos sin rotacion.

## Como ejecutar

Con Maven:

```bash
mvn clean compile exec:java
```

El `pom.xml` tiene configurado como clase principal:

```text
com.graphics.AppFlappyBird
```

## Advertencias para futuras modificaciones

- No usar funciones OpenGL antes de `GL.createCapabilities()`.
- No llamar a `renderer.init()` antes de crear y activar el contexto OpenGL.
- No modificar la lista de tuberias con un `for-each` si tambien se van a eliminar elementos; por eso se usa `Iterator`.
- Si se agrega segundo jugador, revisar colisiones y puntaje con cuidado para decidir si el puntaje es compartido o individual.
- Si se agregan textos reales en OpenGL, se necesitara una estrategia nueva: texturas, atlas de fuentes o figuras manuales.
- Si se agregan mas estados de juego, puede convenir reemplazar `started` y `gameOver` por un enum.
