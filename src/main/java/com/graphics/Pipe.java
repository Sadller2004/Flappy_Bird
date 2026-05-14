package com.graphics;

/**
 * Modelo de una tuberia.
 * Una misma instancia representa la parte superior e inferior separadas por un gap.
 */
public class Pipe {

    public static final float DEFAULT_WIDTH = 0.18f;
    public static final float DEFAULT_GAP_HEIGHT = 0.48f;
    public static final float DEFAULT_SPEED = 0.62f;

    private final float width;
    private final float gapHeight;
    private float x;
    private final float gapCenterY;
    private boolean scored;

    public Pipe(float x, float gapCenterY) {
        this(x, gapCenterY, DEFAULT_WIDTH, DEFAULT_GAP_HEIGHT);
    }

    public Pipe(float x, float gapCenterY, float width, float gapHeight) {
        this.x = x;
        this.gapCenterY = gapCenterY;
        this.width = width;
        this.gapHeight = gapHeight;
    }

    /**
     * Mueve la tuberia de derecha a izquierda.
     */
    public void update(float dt) {
        x -= DEFAULT_SPEED * dt;
    }

    /**
     * Indica si el pajaro ya supero esta tuberia y todavia no sumo punto.
     */
    public boolean canScore(float birdX) {
        return !scored && getRight() < birdX;
    }

    public void markScored() {
        scored = true;
    }

    public boolean isOffScreen() {
        return getRight() < -1.3f;
    }

    public float getX() {
        return x;
    }

    public float getWidth() {
        return width;
    }

    public float getGapCenterY() {
        return gapCenterY;
    }

    public float getGapHeight() {
        return gapHeight;
    }

    public float getLeft() {
        return x - (width * 0.5f);
    }

    public float getRight() {
        return x + (width * 0.5f);
    }

    public float getGapTop() {
        return gapCenterY + (gapHeight * 0.5f);
    }

    public float getGapBottom() {
        return gapCenterY - (gapHeight * 0.5f);
    }

    public float getUpperHeight() {
        return 1.0f - getGapTop();
    }

    public float getUpperCenterY() {
        return getGapTop() + (getUpperHeight() * 0.5f);
    }

    public float getLowerHeight() {
        return getGapBottom() + 1.0f;
    }

    public float getLowerCenterY() {
        return -1.0f + (getLowerHeight() * 0.5f);
    }
}
