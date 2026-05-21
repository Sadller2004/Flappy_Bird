package com.graphics.renderers;

import com.graphics.Bird;

/**
 * Esta clase separa el dibujo del pajaro para que Renderer.java no crezca
 * demasiado. Renderer coordina el orden de dibujo, pero BirdRenderer se encarga
 * unicamente de la apariencia visual de los jugadores.
 *
 * BirdRenderer no modifica fisica ni colisiones: solo consulta la posicion y la
 * velocidad de Bird para dibujar el pajaro geometrico del requerimiento 2.1.
 */
public class BirdRenderer {

    private final ShapeRenderer shapes;

    public BirdRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    /**
     * Jugador 1: mismo pajaro geometrico, pero con cresta, ala, cola y franja
     * rojo/amaranto para distinguirlo claramente del jugador 2.
     */
    public void renderBirdPlayer1(Bird bird) {
        renderGeometricBird(bird, 0.90f, 0.17f, 0.31f, true);
    }

    /**
     * Jugador 2: mismo pajaro geometrico, pero con cresta, ala, cola y franja
     * azul para mantener la identidad visual del segundo jugador.
     */
    public void renderBirdPlayer2(Bird bird) {
        renderGeometricBird(bird, 0.10f, 0.28f, 1.00f, true);
    }

    /**
     * Jugador 3: mismo pajaro geometrico, pero con verde para separarlo de los
     * jugadores rojo y azul.
     */
    public void renderBirdPlayer3(Bird bird) {
        renderGeometricBird(bird, 0.20f, 0.85f, 0.35f, true);
    }

    /**
     * Pajaro muerto: conserva la figura compuesta, pero usa colores apagados.
     * Esto evita volver al rectangulo simple y permite ver que ese jugador ya no
     * esta activo mientras el otro puede seguir jugando.
     */
    public void renderDeadBird(Bird bird) {
        renderGeometricBird(bird, 0.35f, 0.35f, 0.35f, false);
    }

    /**
     * Dibuja un pajaro estilo caricatura/pixel art inspirado en Mordecai de
     * "Un Show mas".
     *
     * Figuras que forman al pajaro:
     * - cuerpo principal: elipse blanca con borde negro;
     * - cabeza/cresta: poligono del color del jugador con borde negro;
     * - pico: rectangulo alargado oscuro con borde;
     * - ala: poligono del color del jugador con detalles blancos;
     * - cola: triangulo del color del jugador;
     * - ojo: elipse blanca con pupila negra.
     *
     * La inclinacion se calcula con velocityY: velocidad positiva inclina el ave
     * hacia arriba y velocidad negativa la inclina hacia abajo. clamp() limita el
     * angulo para que el giro siga siendo jugable y legible.
     *
     * El ala usa la misma velocityY para subir cuando el pajaro salta y bajar
     * cuando cae. No se agrega timer nuevo: la animacion queda sincronizada con
     * la fisica existente.
     */
    public void renderGeometricBird(Bird bird, float mainR, float mainG, float mainB, boolean alive) {
        float x = bird.getX();
        float y = bird.getY();

        float angle = shapes.clamp(bird.getVelocityY() * 0.28f, -0.42f, 0.35f);
        float wingLift = shapes.clamp(bird.getVelocityY() * 0.018f, -0.018f, 0.020f);

        float playerColorR = alive ? mainR : 0.42f;
        float playerColorG = alive ? mainG : 0.42f;
        float playerColorB = alive ? mainB : 0.42f;
        float white = alive ? 1.00f : 0.72f;
        float beak = alive ? 0.43f : 0.34f;
        float outline = 0.00f;

        // Cola pequena: triangulo con borde negro, ubicada detras del cuerpo.
        shapes.drawTriangle(x, y,
                -0.070f, -0.006f,
                -0.123f, -0.034f,
                -0.078f, 0.032f,
                angle, outline, outline, outline);
        shapes.drawTriangle(x, y,
                -0.073f, -0.007f,
                -0.110f, -0.027f,
                -0.080f, 0.024f,
                angle, playerColorR, playerColorG, playerColorB);

        // Ala visible: poligono celeste; wingLift simula el aleteo.
        float[][] wing = {
                { -0.062f, -0.004f + wingLift },
                { -0.112f, -0.050f + wingLift },
                { -0.040f, -0.048f + wingLift },
                { -0.014f, -0.014f + wingLift }
        };
        shapes.drawPolygon(x, y, wing, angle, 1.12f, outline, outline, outline);
        shapes.drawPolygon(x, y, wing, angle, 1.00f, playerColorR, playerColorG, playerColorB);
        shapes.drawRotatedRect(x, y, -0.073f, -0.031f + wingLift, 0.030f, 0.010f, angle, white, white, white);
        shapes.drawRotatedRect(x, y, -0.049f, -0.021f + wingLift, 0.023f, 0.008f, angle, white, white, white);

        // Cuerpo blanco principal, separado de la colision AABB.
        shapes.drawEllipse(x, y, -0.004f, -0.007f, 0.060f, 0.043f, angle, outline, outline, outline);
        shapes.drawEllipse(x, y, -0.002f, -0.006f, 0.052f, 0.035f, angle, white, white, white);

        // Cresta/cabeza celeste superior, hecha con poligono para recordar el pixel art.
        float[][] crest = {
                { -0.060f, 0.006f },
                { -0.045f, 0.067f },
                { -0.015f, 0.038f },
                { -0.002f, 0.088f },
                { 0.070f, 0.025f },
                { 0.030f, 0.020f },
                { 0.010f, -0.004f },
                { -0.030f, 0.000f }
        };
        shapes.drawPolygon(x, y, crest, angle, 1.08f, outline, outline, outline);
        shapes.drawPolygon(x, y, crest, angle, 0.96f, playerColorR, playerColorG, playerColorB);

        // Franja de acento: usa el mismo color principal para reforzar la identidad.
        shapes.drawRotatedRect(x, y, -0.036f, 0.001f, 0.057f, 0.011f, angle, outline, outline, outline);
        shapes.drawRotatedRect(x, y, -0.036f, 0.003f, 0.048f, 0.007f, angle,
                playerColorR, playerColorG, playerColorB);

        // Cara blanca frontal.
        shapes.drawEllipse(x, y, 0.028f, 0.010f, 0.044f, 0.034f, angle, outline, outline, outline);
        shapes.drawEllipse(x, y, 0.028f, 0.010f, 0.036f, 0.026f, angle, white, white, white);

        // Pico corto oscuro: mantiene el borde unido a la cara.
        shapes.drawRotatedRect(x, y, 0.055f, -0.012f, 0.060f, 0.030f, angle, outline, outline, outline);
        shapes.drawRotatedRect(x, y, 0.055f, -0.012f, 0.054f, 0.018f, angle, beak, beak, beak);
        shapes.drawRotatedRect(x, y, 0.055f, -0.012f, 0.048f, 0.004f, angle, 0.04f, 0.04f, 0.04f);

        // Ojo y pupila interna.
        shapes.drawEllipse(x, y, 0.026f, 0.023f, 0.018f, 0.018f, angle, outline, outline, outline);
        shapes.drawEllipse(x, y, 0.026f, 0.023f, 0.013f, 0.013f, angle, white, white, white);
        shapes.drawEllipse(x, y, 0.033f, 0.022f, 0.0045f, 0.0045f, angle, 0.02f, 0.02f, 0.02f);
    }
}
