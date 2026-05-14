package com.graphics;

/**
 * Punto de entrada especifico del mini-juego.
 *
 * Flujo:
 * 1. Java ejecuta main().
 * 2. Se crea una instancia de Game.
 * 3. Game.run() toma el control de la inicializacion, el ciclo principal y la limpieza.
 *
 * Esta clase se mantiene pequena a proposito: sirve para explicar claramente
 * donde arranca el programa sin mezclar aqui la logica del juego.
 */
public class AppFlappyBird {

    /**
     * Recibe los argumentos de linea de comandos, no modifica estado propio
     * y no devuelve nada. Se ejecuta una sola vez al iniciar la aplicacion.
     */
    public static void main(String[] args) {
        new Game().run();
    }
}
