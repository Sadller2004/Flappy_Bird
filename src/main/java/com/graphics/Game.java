package com.graphics;

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
 * Game no dibuja directamente y tampoco calcula los detalles internos del pajaro
 * o las tuberias. Para eso delega en Bird, PipeManager, InputManager y Renderer.
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
    private final Bird bird = new Bird();
    private final PipeManager pipeManager = new PipeManager();
    private final Renderer renderer = new Renderer();
    private final InputManager inputManager = new InputManager();

    private int score;
    private boolean started;
    private boolean gameOver;

    /**
     * Metodo principal de ejecucion del juego.
     *
     * Recibe: nada.
     * Modifica: crea ventana, inicializa objetos, ejecuta el loop y libera recursos.
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
         * esta ventana y con la GPU. Debe ser actual antes de usar GL.createCapabilities().
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
     * Reinicia el estado completo de una partida.
     *
     * Recibe: nada.
     * Modifica: posicion del pajaro, lista de tuberias, puntaje y flags de estado.
     * Devuelve: nada.
     * Momento: al iniciar el juego y al reiniciar despues de game over.
     */
    private void resetGame() {
        bird.reset();
        pipeManager.reset();
        score = 0;
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
     * Interpreta las acciones de teclado segun el estado del juego.
     *
     * Recibe: nada directamente; InputManager lee el estado de la ventana.
     * Modifica: started, gameOver mediante resetGame(), y velocidad del pajaro con jump().
     * Devuelve: nada.
     * Momento: una vez por frame, antes de update(dt).
     */
    private void processInput() {
        InputManager.InputState input = inputManager.poll(window);

        if (input.isSpacePressed()) {
            if (gameOver) {
                resetGame();
            }
            started = true;
            bird.jump();
            updateWindowTitle();
        }

        if (input.isRPressed() && gameOver) {
            resetGame();
        }
    }

    /**
     * Actualiza fisica, tuberias, puntaje y condiciones de game over.
     *
     * Recibe: dt, el tiempo en segundos desde el frame anterior.
     * Modifica: posicion/velocidad del pajaro, tuberias activas, score y gameOver.
     * Devuelve: nada.
     * Momento: una vez por frame, despues del input y antes del render.
     */
    private void update(float dt) {
        if (!started || gameOver) {
            return;
        }

        bird.update(dt);
        if (bird.isOutOfBounds()) {
            gameOver = true;
            updateWindowTitle();
            return;
        }

        /*
         * PipeManager devuelve un resultado pequeno para que Game siga siendo
         * quien decide sobre el puntaje global y el estado gameOver.
         */
        PipeManager.UpdateResult result = pipeManager.update(dt, bird);
        if (result.getScoreDelta() > 0) {
            score += result.getScoreDelta();
            updateWindowTitle();
        }

        if (result.hasCollision()) {
            gameOver = true;
            updateWindowTitle();
        }
    }

    /**
     * Renderiza el estado actual.
     *
     * Recibe: nada directamente; usa bird, pipeManager y gameOver.
     * Modifica: el framebuffer de OpenGL, es decir, lo que se vera en pantalla.
     * Devuelve: nada.
     * Momento: una vez por frame, despues de update(dt).
     */
    private void render() {
        renderer.render(bird, pipeManager.getPipes(), gameOver);
    }

    /**
     * Actualiza el texto de la barra de titulo como feedback simple.
     *
     * Recibe: nada.
     * Modifica: titulo de la ventana GLFW.
     * Devuelve: nada.
     * Momento: al iniciar, sumar puntos, empezar o terminar la partida.
     */
    private void updateWindowTitle() {
        if (window == 0) {
            return;
        }

        String baseTitle = "Flappy Bird OpenGL | Puntos: " + score;
        if (!started) {
            GLFW.glfwSetWindowTitle(window, baseTitle + " | SPACE para empezar");
        } else if (gameOver) {
            GLFW.glfwSetWindowTitle(window, baseTitle + " | GAME OVER - SPACE o R para reiniciar");
        } else {
            GLFW.glfwSetWindowTitle(window, baseTitle);
        }
    }

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
