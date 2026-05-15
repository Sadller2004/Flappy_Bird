package com.graphics;

import org.lwjgl.glfw.GLFW;

/**
 * Centraliza la lectura del teclado con GLFW.
 *
 * Input polling significa preguntar en cada frame "en que estado esta esta
 * tecla?".
 * En este juego no se usan callbacks de teclado; Game llama a poll(window) una
 * vez
 * por frame y recibe un InputState simple.
 *
 * La clase tambien detecta flancos: una tecla cuenta como presionada solo en el
 * frame en que pasa de "suelta" a "apretada". Asi SPACE mantenido no genera
 * muchos saltos o reinicios.
 */
public class InputManager {

    private boolean previousSpace;
    private boolean previousR;

    // R2.
    private boolean previousW;
    private boolean previousUp;

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

        // -------------------------- R2. ---------------------------------------------
        boolean wNow = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        boolean wPressed = wNow && !previousW;
        previousW = wNow;

        boolean upNow = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
        boolean upPressed = upNow && !previousUp;
        previousUp = upNow;

        boolean player2JumpPressed = wPressed || upPressed;

        return new InputState(spacePressed, player2JumpPressed, rPressed);
        //--------------------------------------------------------------------------------
    }

    /**
     * -------------------------- R2.--------------------------
     * Estado simple de entradas relevantes para el juego.
     *
     * No contiene la tecla ESC porque esa tecla ya se aplica directamente sobre la
     * ventana.
     */
    public static class InputState {
        private final boolean player1JumpPressed;
        private final boolean player2JumpPressed;
        private final boolean rPressed;

        public InputState(boolean player1JumpPressed, boolean player2JumpPressed, boolean rPressed) {
            this.player1JumpPressed = player1JumpPressed;
            this.player2JumpPressed = player2JumpPressed;
            this.rPressed = rPressed;
        }

        public boolean isPlayer1JumpPressed() {
            return player1JumpPressed;
        }

        public boolean isPlayer2JumpPressed() {
            return player2JumpPressed;
        }

        public boolean isRPressed() {
            return rPressed;
        }
    }
    //------------------------------------------------------------------------------
}
