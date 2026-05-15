package com.graphics;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.graphics.renderers.BackgroundRenderer;
import com.graphics.renderers.BirdRenderer;
import com.graphics.renderers.HudRenderer;
import com.graphics.renderers.PipeRenderer;
import com.graphics.renderers.ShapeRenderer;

/**
 * Encapsula los recursos principales de OpenGL y coordina el orden de dibujo.
 *
 * Este proyecto dibuja todo en 2D usando NDC (Normalized Device Coordinates):
 * - X va de -1 a 1,
 * - Y va de -1 a 1,
 * - el centro de la pantalla es (0, 0).
 *
 * La refactorizacion deja a Renderer como coordinador. Los detalles visuales
 * viven en clases especializadas: ShapeRenderer, BackgroundRenderer,
 * PipeRenderer, BirdRenderer y HudRenderer.
 */
public class Renderer {

    private int program;
    private int offsetLocation;
    private int scaleLocation;
    private int colorLocation;

    private ShapeRenderer shapeRenderer;
    private BackgroundRenderer backgroundRenderer;
    private PipeRenderer pipeRenderer;
    private BirdRenderer birdRenderer;
    private HudRenderer hudRenderer;

    /**
     * Prepara shader y renderizadores especializados.
     *
     * Recibe: nada.
     * Modifica: shader program, uniforms y recursos compartidos de dibujo.
     * Devuelve: nada.
     * Momento: Game.init() lo llama despues de crear el contexto OpenGL.
     */
    public void init() {
        createShaders();

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.init(offsetLocation, scaleLocation, colorLocation);

        backgroundRenderer = new BackgroundRenderer(shapeRenderer);
        pipeRenderer = new PipeRenderer(shapeRenderer);
        birdRenderer = new BirdRenderer(shapeRenderer);
        hudRenderer = new HudRenderer(shapeRenderer);
    }

    /**
     * R4.
     * Renderiza un frame completo coordinando fondo, tuberias, jugadores y HUD.
     *
     * Recibe: estado visual actual del juego.
     * Modifica: el framebuffer activo de OpenGL.
     * Devuelve: nada.
     * Momento: Game.render() lo llama una vez por frame.
     *
     * Renderer ya no contiene el dibujo detallado del pajaro, fondo, HUD ni
     * tuberias. Solo decide el orden correcto de las capas.
     */
    public void render(Bird player1, Bird player2, List<Pipe> pipes, boolean player1Alive, boolean player2Alive,
            boolean started, boolean gameOver, int scorePlayer1, int scorePlayer2, int currentLevel,
            float currentPipeSpeed, float currentSpawnInterval) {
        GL20.glUseProgram(program);

        backgroundRenderer.renderEnhancedBackground();
        pipeRenderer.renderEnhancedPipes(pipes);

        if (player1Alive) {
            birdRenderer.renderBirdPlayer1(player1);
        } else {
            birdRenderer.renderDeadBird(player1);
        }

        if (player2Alive) {
            birdRenderer.renderBirdPlayer2(player2);
        } else {
            birdRenderer.renderDeadBird(player2);
        }

        hudRenderer.renderHud(scorePlayer1, scorePlayer2, currentLevel, currentPipeSpeed, currentSpawnInterval);

        if (!started) {
            hudRenderer.renderStartScreen();
        }

        if (gameOver) {
            hudRenderer.renderGameOverScreen();
        }

        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    /**
     * Crea shaders 2D.
     *
     * Recibe: nada.
     * Modifica: program y las locations de uniforms.
     * Devuelve: nada.
     * Momento: durante Renderer.init().
     *
     * Shader:
     * - Vertex shader: transforma cada vertice usando escala y offset.
     * - Fragment shader: decide el color final de cada pixel dibujado.
     */
    private void createShaders() {
        String vertexSrc = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform vec2 uScale;
                void main() {
                    vec2 finalPos = aPos.xy * uScale + uOffset;
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                }
                """;

        String fragmentSrc = """
                #version 330 core
                uniform vec3 uColor;
                out vec4 fragColor;
                void main() {
                    fragColor = vec4(uColor, 1.0);
                }
                """;

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        checkShader(vertexShader, "Vertex");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        checkShader(fragmentShader, "Fragment");

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(program));
        }

        offsetLocation = GL20.glGetUniformLocation(program, "uOffset");
        scaleLocation = GL20.glGetUniformLocation(program, "uScale");
        colorLocation = GL20.glGetUniformLocation(program, "uColor");
        if (offsetLocation == -1 || scaleLocation == -1 || colorLocation == -1) {
            throw new RuntimeException("No se pudieron obtener uniforms del shader");
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    /**
     * Verifica si un shader GLSL compilo correctamente.
     */
    private void checkShader(int shader, String type) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(type + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    /**
     * Libera los recursos propios de OpenGL.
     *
     * Recibe: nada.
     * Modifica: elimina buffers compartidos y shader program si existen.
     * Devuelve: nada.
     * Momento: Game.cleanup() lo llama al cerrar el juego.
     */
    public void cleanup() {
        if (shapeRenderer != null) {
            shapeRenderer.cleanup();
        }
        if (program != 0) {
            GL20.glDeleteProgram(program);
            program = 0;
        }
    }
}
