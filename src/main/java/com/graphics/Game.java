package com.graphics;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

/**
 * Coordina el ciclo principal del juego.
 * Inicializa ventana/OpenGL, procesa input, actualiza logica, renderiza y limpia recursos.
 */
public class Game {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;
    private static final float MAX_DELTA_TIME = 0.033f;

    private long window;
    private final Bird bird = new Bird();
    private final PipeManager pipeManager = new PipeManager();
    private final Renderer renderer = new Renderer();
    private final InputManager inputManager = new InputManager();

    private int score;
    private boolean started;
    private boolean gameOver;

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
     */
    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

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

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();
        renderer.init();
    }

    /**
     * Reinicia el estado completo de una partida.
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
     * Bucle principal: delta time, input, update, render y eventos.
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

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    /**
     * Interpreta las acciones de teclado segun el estado del juego.
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

    private void render() {
        renderer.render(bird, pipeManager.getPipes(), gameOver);
    }

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
