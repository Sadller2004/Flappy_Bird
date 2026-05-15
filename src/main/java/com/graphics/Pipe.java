package com.graphics;

/**
 * Modelo de una tuberia.
 *
 * Una instancia de Pipe representa el obstaculo completo:
 * - un rectangulo superior,
 * - un rectangulo inferior,
 * - y el espacio libre o gap entre ambos.
 *
 * La clase no dibuja directamente. Solo calcula posiciones y limites para que
 * PipeManager pueda detectar colisiones y Renderer pueda dibujar las partes.
 */
public class Pipe {

    public static final float DEFAULT_WIDTH = 0.18f;
    public static final float DEFAULT_GAP_HEIGHT = 0.48f;
    public static final float DEFAULT_SPEED = 0.62f;

    private final float width;
    private final float gapHeight;
    private float x;
    private final float gapCenterY;

    // ----------------------------- R2. --------------------------------
    private boolean scoredPlayer1;
    private boolean scoredPlayer2;
    // -------------------------------------------------------------

    /**
     * Crea una tuberia con ancho, gap y velocidad por defecto.
     *
     * Recibe: x inicial y centro vertical del gap.
     * Modifica: inicializa el estado interno de la tuberia.
     * Devuelve: una nueva instancia de Pipe.
     * Momento: PipeManager.spawnPipe() lo usa al crear obstaculos.
     */
    public Pipe(float x, float gapCenterY) {
        this(x, gapCenterY, DEFAULT_WIDTH, DEFAULT_GAP_HEIGHT);
    }

    /**
     * Constructor mas configurable para mantener la clase flexible.
     */
    public Pipe(float x, float gapCenterY, float width, float gapHeight) {
        this.x = x;
        this.gapCenterY = gapCenterY;
        this.width = width;
        this.gapHeight = gapHeight;
    }

    /**
     * Mueve la tuberia de derecha a izquierda.
     *
     * Recibe: dt, tiempo en segundos desde el frame anterior.
     * Modifica: x.
     * Devuelve: nada.
     * Momento: PipeManager.update() lo llama una vez por frame por cada tuberia.
     */
    public void update(float dt) {
        x -= DEFAULT_SPEED * dt;
    }

    /**
     * -------------------------------------R2.----------------------------------------
     * Indica si el pajaro ya supero esta tuberia y todavia no sumo punto.
     * Con la logica de 2 jugadores haora cada uno tiene sus metodos
     * Recibe: birdX, posicion horizontal del pajaro.
     * Modifica: nada.
     * Devuelve: true si el borde derecho de la tuberia quedo detras del pajaro.
     * Momento: PipeManager.update() lo revisa despues de mover la tuberia.
     */
    public boolean canScorePlayer1(float birdX) {
        return !scoredPlayer1 && getRight() < birdX;
    }

    public boolean canScorePlayer2(float birdX) {
        return !scoredPlayer2 && getRight() < birdX;
    }

    /**
     * 
     * Marca la tuberia como ya puntuada para no sumar varias veces.
     */
    public void markScoredPlayer1() {
        scoredPlayer1 = true;
    }

    public void markScoredPlayer2() {
        scoredPlayer2 = true;
    }
    // ----------------------------------------------------------------------------

    /**
     * Devuelve true cuando la tuberia ya salio por la izquierda de la pantalla.
     */
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

    /*
     * Los siguientes metodos convierten el gap en dos rectangulos renderizables.
     * OpenGL dibuja rectangulos por centro y escala, por eso calculamos altura
     * y centro de cada tramo.
     */
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
