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

    private static final float SPAWN_X = 1.2f;
    private static final float GAP_MIN_CENTER = -0.45f;
    private static final float GAP_MAX_CENTER = 0.45f;

    private final List<Pipe> pipes = new ArrayList<>();
    private final Random random = new Random();
    private float spawnTimer;

    // --------------------------------- R3. ------------------------------
    private static final int SCORES_PER_LEVEL = 5;
    private static final int MAX_LEVEL = 5;
    private static final float BASE_PIPE_SPEED = 0.62f;
    private static final float SPEED_INCREMENT_PER_LEVEL = 0.08f;
    private static final float MAX_PIPE_SPEED = 1.00f;
    private static final float BASE_SPAWN_INTERVAL = 1.5f;
    private static final float SPAWN_INTERVAL_DECREASE_PER_LEVEL = 0.10f;
    private static final float MIN_SPAWN_INTERVAL = 1.0f;

    private int currentLevel = 1;
    private float currentPipeSpeed = BASE_PIPE_SPEED;
    private float currentSpawnInterval = BASE_SPAWN_INTERVAL;

    /**
     * ---------------------------------------R3.---------------------------------------
     * Limpia todas las tuberias, reinicia el temporizador de spawn y vuelve la
     * dificultad al nivel inicial.
     *
     * Recibe: nada.
     * Modifica: pipes, spawnTimer, currentLevel, currentPipeSpeed y
     * currentSpawnInterval.
     * Devuelve: nada.
     * Momento: al iniciar o reiniciar una partida.
     */
    public void reset() {
        pipes.clear();
        spawnTimer = 0.0f;
        resetDifficulty();
    }

    /**
     * ---------------------------------------R3.------------------------------------
     * Restaura los valores iniciales de dificultad.
     *
     * Recibe: nada.
     * Modifica: currentLevel, currentPipeSpeed y currentSpawnInterval.
     * Devuelve: nada.
     * Momento: PipeManager.reset() lo llama al iniciar o reiniciar la partida.
     *
     * Al reiniciar, la partida debe volver a ser jugable desde cero: nivel 1,
     * velocidad base e intervalo base de aparicion de tuberias.
     */
    private void resetDifficulty() {
        currentLevel = 1;
        currentPipeSpeed = BASE_PIPE_SPEED;
        currentSpawnInterval = BASE_SPAWN_INTERVAL;
    }

    /**
     * --------------------------------------- R3. ---------------------------------------
     * Actualiza la dificultad de la partida segun el puntaje mas alto.
     *
     * Recibe: puntaje de los tres jugadores.
     * Modifica: currentLevel, currentPipeSpeed y currentSpawnInterval.
     * Devuelve: nada.
     * Momento: Game.update() lo llama durante la partida despues de sumar puntos.
     *
     * Se usa el puntaje mayor porque las tuberias son compartidas. Si un jugador
     * avanza mas, la partida completa debe volverse mas dificil para ambos.
     *
     * El nivel aumenta cada SCORES_PER_LEVEL puntos y se limita con MAX_LEVEL
     * para que el juego no se vuelva imposible. La velocidad tambien tiene un
     * maximo y el intervalo tiene un minimo para mantener la partida jugable.
     */
    public void updateDifficulty(int scorePlayer1, int scorePlayer2, int scorePlayer3) {
        int highestScore = Math.max(scorePlayer1, Math.max(scorePlayer2, scorePlayer3));
        currentLevel = Math.min(MAX_LEVEL, 1 + highestScore / SCORES_PER_LEVEL);

        int completedLevelIncrements = currentLevel - 1;
        currentPipeSpeed = Math.min(
                MAX_PIPE_SPEED,
                BASE_PIPE_SPEED + completedLevelIncrements * SPEED_INCREMENT_PER_LEVEL);
        currentSpawnInterval = Math.max(
                MIN_SPAWN_INTERVAL,
                BASE_SPAWN_INTERVAL - completedLevelIncrements * SPAWN_INTERVAL_DECREASE_PER_LEVEL);
    }

    /**
     * ---------------------------------------R3.---------------------------------------
     * Devuelve el nivel actual de dificultad.
     *
     * Recibe: nada.
     * Modifica: nada.
     * Devuelve: currentLevel.
     * Momento: Game.updateWindowTitle() lo usa para mostrar el nivel en la ventana.
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * ---------------------------------------R3.---------------------------------------
     * Devuelve la velocidad actual de las tuberias.
     *
     * Recibe: nada.
     * Modifica: nada.
     * Devuelve: currentPipeSpeed.
     * Momento: Game.updateWindowTitle() lo usa para mostrar la dificultad actual.
     */
    public float getCurrentPipeSpeed() {
        return currentPipeSpeed;
    }

    /**
     * ---------------------------------------R3.---------------------------------------
     * Devuelve el intervalo actual de aparicion de tuberias.
     *
     * Recibe: nada.
     * Modifica: nada.
     * Devuelve: currentSpawnInterval.
     * Momento: Game.updateWindowTitle() lo usa para mostrar la frecuencia actual.
     */
    public float getCurrentSpawnInterval() {
        return currentSpawnInterval;
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
     * R2.3.
     * Actualiza las tuberias con la dificultad actual.
     *
     * Recibe: dt, tiempo en segundos desde el frame anterior.
     * Modifica: spawnTimer, pipes y la posicion x de cada tuberia.
     * Devuelve: nada.
     * Momento: Game.update() lo llama una vez por frame durante la partida.
     *
     * currentSpawnInterval controla cada cuanto aparece una tuberia y
     * currentPipeSpeed controla que tan rapido avanza. Ambos valores cambian con
     * updateDifficulty() cuando sube el puntaje mayor de los jugadores.
     */
    public void updatePipes(float dt) {
        spawnTimer += dt;
        if (spawnTimer >= currentSpawnInterval) {
            spawnTimer = 0.0f;
            spawnPipe();
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update(dt, currentPipeSpeed);

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


    //-------------------tercer jugador-----------------
    public int consumeScoreForPlayer3(Bird bird) {
        int scoreDelta = 0;

        for (Pipe pipe : pipes) {
            if (pipe.canScorePlayer3(bird.getX())) {
                pipe.markScoredPlayer3();
                scoreDelta++;
            }
        }

        return scoreDelta;
    }
    // --------------------------------------------------------------------------------------------
}
