package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Administra las tuberias activas.
 *
 * Responsabilidades:
 * - crear tuberias cada cierto tiempo,
 * - moverlas,
 * - eliminar las que salen de pantalla,
 * - detectar si el pajaro paso una tuberia,
 * - detectar colisiones AABB.
 *
 * Game no necesita conocer el detalle de cada tuberia; solo recibe un
 * UpdateResult
 * indicando cuantos puntos se ganaron y si ocurrio una colision.
 */
public class PipeManager {

    private static final float SPAWN_INTERVAL = 1.5f;
    private static final float SPAWN_X = 1.2f;
    private static final float GAP_MIN_CENTER = -0.45f;
    private static final float GAP_MAX_CENTER = 0.45f;

    private final List<Pipe> pipes = new ArrayList<>();
    private final Random random = new Random();
    private float spawnTimer;

    /**
     * Limpia todas las tuberias y reinicia el temporizador de spawn.
     *
     * Recibe: nada.
     * Modifica: pipes y spawnTimer.
     * Devuelve: nada.
     * Momento: al iniciar o reiniciar una partida.
     */
    public void reset() {
        pipes.clear();
        spawnTimer = 0.0f;
    }

    /**
     * Devuelve la lista actual para que Renderer pueda dibujarla.
     */
    public List<Pipe> getPipes() {
        return pipes;
    }

    /**
     * Crea una tuberia en el borde derecho con un gap vertical aleatorio.
     */
    private void spawnPipe() {
        float gapCenter = GAP_MIN_CENTER + random.nextFloat() * (GAP_MAX_CENTER - GAP_MIN_CENTER);
        pipes.add(new Pipe(SPAWN_X, gapCenter));
    }

    /**
     * Colision AABB entre el rectangulo del pajaro y la zona solida de la tuberia.
     *
     * Recibe: pipe y bird.
     * Modifica: nada.
     * Devuelve: true si hay choque.
     * Momento: durante PipeManager.update(), despues de mover cada tuberia.
     *
     * AABB collision:
     * 1. Primero se revisa si los rectangulos se cruzan horizontalmente.
     * 2. Si no hay cruce en X, no puede haber choque.
     * 3. Si hay cruce en X, el pajaro colisiona cuando esta por encima del gap
     * o por debajo del gap.
     */
    private boolean collidesWithBird(Pipe pipe, Bird bird) {
        boolean overlapX = bird.getRight() > pipe.getLeft() && bird.getLeft() < pipe.getRight();
        if (!overlapX) {
            return false;
        }

        return bird.getTop() > pipe.getGapTop() || bird.getBottom() < pipe.getGapBottom();
    }

    /**
     * R2.
     */
    // Actualizar solo las tuberías
    public void updatePipes(float dt) {
        spawnTimer += dt;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer = 0.0f;
            spawnPipe();
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update(dt);

            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }
    }

    // Detectar colisión para cualquier pájaro
    public boolean collidesWithBird(Bird bird) {
        for (Pipe pipe : pipes) {
            if (collidesWithBird(pipe, bird)) {
                return true;
            }
        }
        return false;
    }

    // Calcular puntaje por jugador
    public int consumeScoreForPlayer1(Bird bird) {
        int scoreDelta = 0;

        for (Pipe pipe : pipes) {
            if (pipe.canScorePlayer1(bird.getX())) {
                pipe.markScoredPlayer1();
                scoreDelta++;
            }
        }

        return scoreDelta;
    }

    public int consumeScoreForPlayer2(Bird bird) {
        int scoreDelta = 0;

        for (Pipe pipe : pipes) {
            if (pipe.canScorePlayer2(bird.getX())) {
                pipe.markScoredPlayer2();
                scoreDelta++;
            }
        }

        return scoreDelta;
    }
    // --------------------------------------------------------------------------------------------
}
