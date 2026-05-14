package com.graphics;

import org.lwjgl.glfw.GLFW;

/**
 * Centraliza la lectura del teclado con GLFW.
 *
 * Input polling significa preguntar en cada frame "en que estado esta esta tecla?".
 * En este juego no se usan callbacks de teclado; Game llama a poll(window) una vez
 * por frame y recibe un InputState simple.
 *
 * La clase tambien detecta flancos: una tecla cuenta como presionada solo en el
 * frame en que pasa de "suelta" a "apretada". Asi SPACE mantenido no genera
 * muchos saltos o reinicios.
 */
public class InputManager {

    private boolean previousSpace;
    private boolean previousR;

    /**
     * Lee el teclado de la ventana actual.
     *
     * Recibe: window, el identificador de la ventana GLFW.
     * Modifica: previousSpace/previousR para recordar el estado del frame anterior.
     * Devuelve: InputState con acciones de SPACE y R detectadas por flanco.
     * Momento: Game.processInput() lo llama una vez por frame.
     */
    public InputState poll(long window) {
        /*
         * ESC no se devuelve como accion: directamente marca la ventana para cerrar.
         * El game loop terminara cuando glfwWindowShouldClose(window) sea true.
         */
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        boolean spaceNow = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        boolean spacePressed = spaceNow && !previousSpace;
        previousSpace = spaceNow;

        boolean rNow = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        boolean rPressed = rNow && !previousR;
        previousR = rNow;

        return new InputState(spacePressed, rPressed);
    }

    /**
     * Estado simple de entradas relevantes para el juego.
     *
     * No contiene la tecla ESC porque esa tecla ya se aplica directamente sobre la ventana.
     */
    public static class InputState {
        private final boolean spacePressed;
        private final boolean rPressed;

        public InputState(boolean spacePressed, boolean rPressed) {
            this.spacePressed = spacePressed;
            this.rPressed = rPressed;
        }

        public boolean isSpacePressed() {
            return spacePressed;
        }

        public boolean isRPressed() {
            return rPressed;
        }
    }
}
