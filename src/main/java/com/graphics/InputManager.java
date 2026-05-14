package com.graphics;

import org.lwjgl.glfw.GLFW;

/**
 * Centraliza el teclado.
 * Usa deteccion de flanco para que SPACE/R disparen una sola accion por pulsacion.
 */
public class InputManager {

    private boolean previousSpace;
    private boolean previousR;

    public InputState poll(long window) {
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
