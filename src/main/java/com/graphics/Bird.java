package com.graphics;

/**
 * Representa al jugador del juego.
 * Guarda su posicion, velocidad vertical y aplica la fisica basica.
 */
public class Bird {

    public static final float DEFAULT_X = -0.45f;
    public static final float WIDTH = 0.10f;
    public static final float HEIGHT = 0.10f;

    private static final float GRAVITY = -1.9f;
    private static final float JUMP_IMPULSE = 0.85f;
    private static final float MAX_FALL_SPEED = -1.8f;

    private final float x;
    private float y;
    private float velocityY;

    public Bird() {
        this.x = DEFAULT_X;
        reset();
    }

    /**
     * Reinicia al pajaro al centro de la pantalla, sin velocidad.
     */
    public void reset() {
        y = 0.0f;
        velocityY = 0.0f;
    }

    /**
     * Aplica un impulso vertical. Se usa al iniciar y al saltar.
     */
    public void jump() {
        velocityY = JUMP_IMPULSE;
    }

    /**
     * Integra la fisica vertical usando delta time en segundos.
     */
    public void update(float dt) {
        velocityY += GRAVITY * dt;
        if (velocityY < MAX_FALL_SPEED) {
            velocityY = MAX_FALL_SPEED;
        }
        y += velocityY * dt;
    }

    /**
     * Verifica choque contra techo o suelo en coordenadas NDC.
     */
    public boolean isOutOfBounds() {
        return getTop() >= 1.0f || getBottom() <= -1.0f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public float getLeft() {
        return x - (WIDTH * 0.5f);
    }

    public float getRight() {
        return x + (WIDTH * 0.5f);
    }

    public float getBottom() {
        return y - (HEIGHT * 0.5f);
    }

    public float getTop() {
        return y + (HEIGHT * 0.5f);
    }
}
