package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Administra las tuberias activas: spawn, movimiento, puntaje y colisiones.
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
     * Resultado de actualizar tuberias en un frame.
     */
    public static class UpdateResult {
        private final int scoreDelta;
        private final boolean collision;

        public UpdateResult(int scoreDelta, boolean collision) {
            this.scoreDelta = scoreDelta;
            this.collision = collision;
        }

        public int getScoreDelta() {
            return scoreDelta;
        }

        public boolean hasCollision() {
            return collision;
        }
    }

    public void reset() {
        pipes.clear();
        spawnTimer = 0.0f;
    }

    /**
     * Actualiza obstaculos y devuelve si hubo puntos o colision.
     */
    public UpdateResult update(float dt, Bird bird) {
        int scoreDelta = 0;

        spawnTimer += dt;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer = 0.0f;
            spawnPipe();
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update(dt);

            if (pipe.canScore(bird.getX())) {
                pipe.markScored();
                scoreDelta++;
            }

            if (collidesWithBird(pipe, bird)) {
                return new UpdateResult(scoreDelta, true);
            }

            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        return new UpdateResult(scoreDelta, false);
    }

    public List<Pipe> getPipes() {
        return pipes;
    }

    private void spawnPipe() {
        float gapCenter = GAP_MIN_CENTER + random.nextFloat() * (GAP_MAX_CENTER - GAP_MIN_CENTER);
        pipes.add(new Pipe(SPAWN_X, gapCenter));
    }

    /**
     * Colision AABB: si hay cruce horizontal, el pajaro debe permanecer dentro del gap.
     */
    private boolean collidesWithBird(Pipe pipe, Bird bird) {
        boolean overlapX = bird.getRight() > pipe.getLeft() && bird.getLeft() < pipe.getRight();
        if (!overlapX) {
            return false;
        }

        return bird.getTop() > pipe.getGapTop() || bird.getBottom() < pipe.getGapBottom();
    }
}
