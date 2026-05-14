package com.graphics;

/**
 * Punto de entrada especifico del mini-juego.
 * La logica real vive en Game para mantener esta clase simple y defendible.
 */
public class AppFlappyBird {

    public static void main(String[] args) {
        new Game().run();
    }
}
