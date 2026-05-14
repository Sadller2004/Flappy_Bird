package com.graphics;

/**
 * Representa al jugador del juego.
 *
 * El pajaro se modela como un rectangulo en coordenadas NDC:
 * - x es fija para que el jugador no avance realmente por la pantalla.
 * - y cambia por gravedad y salto.
 * - width/height se usan para renderizar y para colisiones.
 *
 * Tener esta clase separada facilita agregar despues un segundo jugador:
 * se podria crear otro Bird con otra posicion o controles sin mezclarlo con OpenGL.
 */
public class Bird {

    public static final float DEFAULT_X = -0.45f;
    public static final float WIDTH = 0.10f;
    public static final float HEIGHT = 0.10f;

    /*
     * Constantes de fisica. Como el juego usa NDC, estas unidades no son pixeles:
     * indican cuanto cambia la posicion dentro del rango visible de OpenGL.
     */
    private static final float GRAVITY = -1.9f;
    private static final float JUMP_IMPULSE = 0.85f;
    private static final float MAX_FALL_SPEED = -1.8f;

    private final float x;
    private float y;
    private float velocityY;

    /**
     * Crea el pajaro en su posicion horizontal fija y lo reinicia.
     */
    public Bird() {
        this.x = DEFAULT_X;
        reset();
    }

    /**
     * Reinicia al pajaro al centro de la pantalla, sin velocidad.
     *
     * Recibe: nada.
     * Modifica: y y velocityY.
     * Devuelve: nada.
     * Momento: al iniciar una partida o al reiniciar despues de game over.
     */
    public void reset() {
        y = 0.0f;
        velocityY = 0.0f;
    }

    /**
     * Aplica un impulso vertical.
     *
     * Recibe: nada.
     * Modifica: velocityY, reemplazandola por una velocidad positiva.
     * Devuelve: nada.
     * Momento: cuando Game detecta SPACE.
     */
    public void jump() {
        velocityY = JUMP_IMPULSE;
    }

    /**
     * Integra la fisica vertical usando delta time.
     *
     * Recibe: dt en segundos, calculado por Game.loop().
     * Modifica: velocityY por gravedad y y por movimiento vertical.
     * Devuelve: nada.
     * Momento: una vez por frame mientras la partida esta activa.
     *
     * Usar dt evita que la fisica dependa directamente de los FPS:
     * si un frame tarda mas, el avance se ajusta proporcionalmente.
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
     *
     * Recibe: nada.
     * Modifica: nada.
     * Devuelve: true si el rectangulo del pajaro sale del rango vertical [-1, 1].
     * Momento: Game.update() lo revisa despues de mover al pajaro.
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

    /*
     * Bordes del rectangulo usados para AABB collision.
     * AABB significa "Axis-Aligned Bounding Box": cajas rectangulares alineadas
     * a los ejes X/Y, una forma simple y rapida de detectar choques.
     */
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
