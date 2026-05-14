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
 * Game no necesita conocer el detalle de cada tuberia; solo recibe un UpdateResult
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
     * Resultado de actualizar tuberias en un frame.
     *
     * scoreDelta indica cuantos puntos se suman en este frame.
     * collision indica si alguna tuberia choco con el pajaro.
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
     * Actualiza obstaculos y devuelve si hubo puntos o colision.
     *
     * Recibe: dt y bird. dt controla el avance temporal; bird aporta limites para puntaje/choque.
     * Modifica: lista de tuberias, posiciones, estado scored y temporizador de spawn.
     * Devuelve: UpdateResult con puntos ganados y bandera de colision.
     * Momento: Game.update(dt) lo llama una vez por frame cuando el juego esta activo.
     */
    public UpdateResult update(float dt, Bird bird) {
        int scoreDelta = 0;

        spawnTimer += dt;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer = 0.0f;
            spawnPipe();
        }

        /*
         * Iterator permite eliminar tuberias de la lista mientras se recorre,
         * sin provocar errores por modificar la coleccion en medio del loop.
         */
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
     *    o por debajo del gap.
     */
    private boolean collidesWithBird(Pipe pipe, Bird bird) {
        boolean overlapX = bird.getRight() > pipe.getLeft() && bird.getLeft() < pipe.getRight();
        if (!overlapX) {
            return false;
        }

        return bird.getTop() > pipe.getGapTop() || bird.getBottom() < pipe.getGapBottom();
    }
}
