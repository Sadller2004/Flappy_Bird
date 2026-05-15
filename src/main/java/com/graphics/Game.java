package com.graphics;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

/**
 * Coordina el ciclo principal del juego.
 *
 * Responsabilidades:
 * - Inicializar GLFW y crear la ventana.
 * - Crear el contexto de OpenGL para que Renderer pueda usar la GPU.
 * - Ejecutar el game loop: input -> update -> render.
 * - Guardar el estado general de la partida: started, gameOver y score.
 * - Liberar recursos al cerrar.
 *
 * Game no dibuja directamente y tampoco calcula los detalles internos del
 * pajaro
 * o las tuberias. Para eso delega en Bird, PipeManager, InputManager y
 * Renderer.
 */
public class Game {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;

    /*
     * Delta time (dt) es el tiempo transcurrido entre un frame y el siguiente.
     * Se limita para que, si la computadora se congela un instante, la fisica no
     * avance con un salto demasiado grande.
     */
    private static final float MAX_DELTA_TIME = 0.033f;

    /*
     * window es el "handle" de GLFW: un identificador numerico de la ventana.
     * LWJGL usa este long para consultar teclado, cambiar titulo, cerrar, etc.
     */
    private long window;

    /*
     * Objetos principales del juego. Game los conecta:
     * - Bird guarda el estado del jugador.
     * - PipeManager guarda y actualiza obstaculos.
     * - Renderer dibuja el estado actual usando OpenGL.
     * - InputManager convierte teclas en acciones simples.
     */
    // ------------------------------- R2. -------------------------------
    private final Bird player1 = new Bird(-0.45f, 0.00f);
    private final Bird player2 = new Bird(-0.65f, 0.00f);
    private int scorePlayer1;
    private int scorePlayer2;
    private boolean player1Alive;
    private boolean player2Alive;
    // -------------------------------//-------------------------------

    private final PipeManager pipeManager = new PipeManager();
    private final Renderer renderer = new Renderer();
    private final InputManager inputManager = new InputManager();

    private boolean started;
    private boolean gameOver;

    /**
     * Metodo principal de ejecucion del juego.
     *
     * Recibe: nada.
     * Modifica: crea ventana, inicializa objetos, ejecuta el loop y libera
     * recursos.
     * Devuelve: nada.
     * Momento: se llama desde AppFlappyBird.main().
     */
    public void run() {
        try {
            init();
            resetGame();
            loop();
        } finally {
            cleanup();
        }
    }

    /**
     * Inicializa GLFW, crea la ventana y prepara los recursos OpenGL.
     *
     * Recibe: nada.
     * Modifica: window, contexto OpenGL actual y recursos internos del Renderer.
     * Devuelve: nada; si algo falla, lanza una excepcion.
     * Momento: antes de empezar la partida y antes del game loop.
     */
    private void init() {
        /*
         * GLFW es la biblioteca usada para crear ventanas, manejar teclado y
         * administrar el contexto de OpenGL. LWJGL expone GLFW desde Java.
         */
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        /*
         * Window hints: configuracion que GLFW usara al crear la ventana.
         * Aqui pedimos OpenGL 3.3 core profile, compatible con shaders modernos.
         */
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Flappy Bird OpenGL", 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        /*
         * El OpenGL context es el estado que conecta las llamadas OpenGL con
         * esta ventana y con la GPU. Debe ser actual antes de usar
         * GL.createCapabilities().
         */
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // VSync: sincroniza el swap con el refresco del monitor.
        GLFW.glfwShowWindow(window);

        /*
         * LWJGL carga aqui las funciones reales de OpenGL disponibles en la maquina.
         * Sin esto, llamadas como glCreateShader o glDrawArrays no estan listas.
         */
        GL.createCapabilities();
        renderer.init();
    }

    /**
     * -------------------------------R2.R3.------------------------------------
     * Reinicia el estado completo de una partida.
     *
     * Recibe: nada.
     * Modifica: posicion del pajaro, lista de tuberias, puntaje y flags de estado.
     * Devuelve: nada.
     * Momento: al iniciar el juego y al reiniciar despues de game over.
     */
    private void resetGame() {
        player1.reset();
        player2.reset();

        scorePlayer1 = 0;
        scorePlayer2 = 0;

        pipeManager.reset();
        pipeManager.updateDifficulty(scorePlayer1, scorePlayer2); //R3. despues del puntaje para obtener cambios

        player1Alive = true;
        player2Alive = true;

        started = false;
        gameOver = false;

        updateWindowTitle();
    }

    /**
     * Bucle principal o game loop.
     *
     * Recibe: nada.
     * Modifica: el estado del juego frame por frame.
     * Devuelve: nada; termina cuando la ventana pide cerrarse.
     * Momento: despues de init() y resetGame().
     *
     * Cada vuelta del loop representa un frame:
     * 1. calcular delta time,
     * 2. leer input,
     * 3. actualizar logica,
     * 4. renderizar,
     * 5. intercambiar buffers y procesar eventos.
     */
    private void loop() {
        float lastTime = (float) GLFW.glfwGetTime();
        while (!GLFW.glfwWindowShouldClose(window)) {
            float now = (float) GLFW.glfwGetTime();
            float dt = now - lastTime;
            lastTime = now;
            if (dt > MAX_DELTA_TIME) {
                dt = MAX_DELTA_TIME;
            }

            processInput();
            update(dt);
            render();

            /*
             * Double buffering: se dibuja en un buffer oculto y luego se intercambia
             * con el visible. Asi se evita ver la escena a medio dibujar.
             */
            GLFW.glfwSwapBuffers(window);

            /*
             * input polling/event polling: GLFW procesa eventos pendientes
             * como teclado, cierre de ventana o cambios del sistema.
             */
            GLFW.glfwPollEvents();
        }
    }

    /**
     * ---------------------------R2.--------------------------------
     * Interpreta las acciones de teclado segun el estado del juego.
     *
     * Recibe: nada directamente; InputManager lee el estado de la ventana.
     * Modifica: started, gameOver mediante resetGame(), y velocidad del pajaro con
     * jump().
     * Devuelve: nada.
     * Momento: una vez por frame, antes de update(dt).
     */
    private void processInput() {
        InputManager.InputState input = inputManager.poll(window);

        if (gameOver && (input.isPlayer1JumpPressed() || input.isPlayer2JumpPressed())) {
            resetGame();
            return;
        }

        if (input.isPlayer1JumpPressed()) {
            started = true;
            if (player1Alive) {// Validacion si un player esta kill no salte
                player1.jump();
            }
            updateWindowTitle();
        }

        if (input.isPlayer2JumpPressed()) {
            started = true;
            if (player2Alive) {// Validacion si un player esta kill no salte
                player2.jump();
            }
            updateWindowTitle();
        }

        if (input.isRPressed() && gameOver) {
            resetGame();
        }
    }

    /**
     * R2.R3.
     * Actualiza fisica, tuberias, puntaje, dificultad y condiciones de game over.
     *
     * Recibe: dt, el tiempo en segundos desde el frame anterior.
     * Modifica: posicion/velocidad del pajaro, tuberias activas, score,
     * dificultad y gameOver.
     * Devuelve: nada.
     * Momento: una vez por frame, despues del input y antes del render.
     */
    private void update(float dt) {
        if (!started || gameOver) {
            return;
        }

        if (!player1Alive && !player2Alive) {
            gameOver = true;
        }

        pipeManager.updatePipes(dt);

        if (player1Alive) {
            player1.update(dt);

            if (player1.isOutOfBounds() || pipeManager.collidesWithBird(player1)) {
                player1.setVelocityY(-0.3f);
                player1Alive = false;
            } else {
                scorePlayer1 += pipeManager.consumeScoreForPlayer1(player1);
            }
        } else {
            player1.dead(dt);
        }

        if (player2Alive) {
            player2.update(dt);

            if (player2.isOutOfBounds() || pipeManager.collidesWithBird(player2)) {
                player2.setVelocityY(-0.3f);
                player2Alive = false;
            } else {
                scorePlayer2 += pipeManager.consumeScoreForPlayer2(player2);
            }
        } else {
            player2.dead(dt);
        }

        /*
         * R2.3.
         * La dificultad se actualiza despues de sumar los puntos de ambos jugadores.
         * Asi el nivel nuevo aparece inmediatamente en el titulo y se usa desde el
         * siguiente frame para mover y generar tuberias.
         */
        pipeManager.updateDifficulty(scorePlayer1, scorePlayer2);

        updateWindowTitle();
    }
    // ---------------------------------------------//---------------------------------------------

    /**
     * R2.
     * Renderiza el estado actual.
     *
     * Recibe: nada directamente; usa bird, pipeManager y gameOver.
     * Modifica: el framebuffer de OpenGL, es decir, lo que se vera en pantalla.
     * Devuelve: nada.
     * Momento: una vez por frame, despues de update(dt).
     */
    private void render() {
        renderer.render(player1, player2, pipeManager.getPipes(), player1Alive, player2Alive, gameOver);
    }
    // ----------------------------------------------//----------------------------------------------

    /**
     * R2.
     * Actualiza el texto de la barra de titulo como feedback simple.
     *
     * Recibe: nada.
     * Modifica: titulo de la ventana GLFW.
     * Devuelve: nada.
     * Momento: al iniciar, sumar puntos, cambiar dificultad, empezar o terminar la
     * partida.
     *
     * R2.3 agrega nivel, velocidad e intervalo para que la dificultad actual sea
     * visible sin modificar el Renderer ni crear un HUD nuevo.
     */
    private void updateWindowTitle() {
        if (window == 0) {
            return;
        }

        String difficultyTitle = String.format(
                Locale.US,
                " | Nivel: %d | Vel: %.2f | Spawn: %.2fs",
                pipeManager.getCurrentLevel(),
                pipeManager.getCurrentPipeSpeed(),
                pipeManager.getCurrentSpawnInterval());

        String baseTitle = "Flappy Bird OpenGL"
                + " | azul: " + scorePlayer2 + (player2Alive ? " vivo" : " muerto")
                + " | rojo: " + scorePlayer1 + (player1Alive ? " vivo" : " muerto")
                + difficultyTitle;

        if (!started) {
            GLFW.glfwSetWindowTitle(window, baseTitle + " | SPACE J1 / W o UP J2 para empezar");
        } else if (gameOver) {
            GLFW.glfwSetWindowTitle(window, baseTitle + " | GAME OVER - R para reiniciar");
        } else {
            GLFW.glfwSetWindowTitle(window, baseTitle);
        }
    }
    // --------------------------------------------------------//--------------------------------------------------------

    /**
     * Limpia OpenGL y GLFW en orden inverso a la inicializacion.
     *
     * Recibe: nada.
     * Modifica: libera VAO/VBO/shader program y destruye la ventana.
     * Devuelve: nada.
     * Momento: al salir del game loop o si ocurre una excepcion durante run().
     */
    private void cleanup() {
        renderer.cleanup();
        if (window != 0) {
            GLFW.glfwDestroyWindow(window);
            window = 0;
        }
        GLFW.glfwTerminate();
    }
}
