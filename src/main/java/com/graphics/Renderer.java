package com.graphics;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Encapsula los recursos y llamadas de OpenGL.
 *
 * Este proyecto dibuja todo en 2D usando NDC (Normalized Device Coordinates):
 * - X va de -1 a 1,
 * - Y va de -1 a 1,
 * - el centro de la pantalla es (0, 0).
 *
 * No se usan texturas. Todos los objetos son rectangulos hechos a partir de
 * un solo quad base, que se mueve, escala y colorea con uniforms.
 */
public class Renderer {

    /*
     * program: shader program enlazado. Combina vertex shader + fragment shader.
     * vao: Vertex Array Object. Recuerda como interpretar los vertices.
     * vbo: Vertex Buffer Object. Guarda los vertices del quad en memoria de GPU.
     */
    private int program;
    private int vao;
    private int vbo;

    /*
     * Uniforms: variables del shader que Java puede cambiar antes de dibujar.
     * En este juego se usan para offset, escala y color de cada rectangulo.
     */
    private int offsetLocation;
    private int scaleLocation;
    private int colorLocation;

    /**
     * Prepara los recursos OpenGL necesarios para dibujar.
     *
     * Recibe: nada.
     * Modifica: program, vao, vbo y locations de uniforms.
     * Devuelve: nada.
     * Momento: Game.init() lo llama despues de crear el contexto OpenGL.
     */
    public void init() {
        createShaders();
        createBaseQuad();
    }

    /**
     * R2.
     * Renderiza un frame completo del juego.
     *
     * Recibe: bird, lista de pipes y bandera gameOver.
     * Modifica: el framebuffer activo de OpenGL.
     * Devuelve: nada.
     * Momento: Game.render() lo llama una vez por frame.
     */
    public void render(Bird player1, Bird player2, List<Pipe> pipes, boolean player1Alive, boolean player2Alive,
            boolean gameOver) {
        renderBackground();

        /*
         * Antes de dibujar, se activa el shader program y el VAO.
         * Asi OpenGL sabe que codigo GLSL usar y de donde salen los vertices.
         */
        GL20.glUseProgram(program);
        GL30.glBindVertexArray(vao);

        renderPipes(pipes);

        if (player1Alive) {
            renderBirdPlayer1(player1);
        } else {
            renderDeadBird(player1);
        }

        if (player2Alive) {
            renderBirdPlayer2(player2);
        } else {
            renderDeadBird(player2);
        }

        if (gameOver) {
            renderGameOverOverlay();
        }

        /*
         * Desactivar al final ayuda a dejar el estado de OpenGL mas limpio.
         */
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    /**
     * Dibuja el pajaro usando su posicion y tamano actuales.
     */
    public void renderBirdPlayer1(Bird bird) {
        drawRect(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight(),
                0.898f, 0.169f, 0.314f); // rojo amaranto
    }

    /**
     * Dibuja el pajaro usando su posicion y tamano actuales.
     */
    public void renderBirdPlayer2(Bird bird) {
        drawRect(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight(),
                0.20f, 0.45f, 1.00f); // azul
    }

    /**
     * Dibuja el pajaro usando su posicion y tamano actuales de la colision y cambiandole de color.
     */
    public void renderDeadBird(Bird bird) {
        drawRect(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight(),
                0.35f, 0.35f, 0.35f); // gris
    }

    // --------------------------------------------------//--------------------------------------------------

    /**
     * Crea shaders 2D.
     *
     * Recibe: nada.
     * Modifica: program y las locations de uniforms.
     * Devuelve: nada.
     * Momento: durante Renderer.init().
     *
     * Shader:
     * - Vertex shader: transforma cada vertice del quad base usando escala y
     * offset.
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

        /*
         * Una vez enlazados dentro del program, los shader objects separados ya
         * no se necesitan y se pueden liberar.
         */
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
     * Crea un rectangulo unitario centrado en el origen usando 2 triangulos.
     *
     * Recibe: nada.
     * Modifica: vao y vbo.
     * Devuelve: nada.
     * Momento: durante Renderer.init().
     *
     * El quad base mide 1x1 en coordenadas locales, de -0.5 a +0.5.
     * Luego drawRect() lo convierte en cualquier rectangulo usando uniforms.
     */
    private void createBaseQuad() {
        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        /*
         * BufferUtils crea un FloatBuffer que LWJGL puede pasar a OpenGL.
         * glBufferData copia esos vertices al VBO en memoria de GPU.
         */
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        /*
         * Atributo 0: posicion vec3 del vertex shader.
         * Cada vertice tiene 3 floats: x, y, z.
         */
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Limpia la pantalla con color de cielo.
     */
    public void renderBackground() {
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Dibuja las tuberias como dos rectangulos por cada Pipe.
     */
    public void renderPipes(List<Pipe> pipes) {
        for (Pipe pipe : pipes) {
            if (pipe.getUpperHeight() > 0.0f) {
                drawRect(pipe.getX(), pipe.getUpperCenterY(), pipe.getWidth(), pipe.getUpperHeight(),
                        0.18f, 0.70f, 0.25f);
            }

            if (pipe.getLowerHeight() > 0.0f) {
                drawRect(pipe.getX(), pipe.getLowerCenterY(), pipe.getWidth(), pipe.getLowerHeight(),
                        0.18f, 0.70f, 0.25f);
            }
        }
    }

    /**
     * Dibuja una franja oscura al centro para indicar game over sin usar texto
     * OpenGL.
     */
    public void renderGameOverOverlay() {
        drawRect(0.0f, 0.0f, 2.0f, 0.22f, 0.15f, 0.18f, 0.22f);
    }

    /**
     * Dibuja un rectangulo en NDC usando uniforms sobre el quad base.
     *
     * Recibe: centro (x, y), tamano (width, height) y color RGB.
     * Modifica: uniforms del shader y framebuffer al ejecutar glDrawArrays.
     * Devuelve: nada.
     * Momento: lo usan renderBird(), renderPipes() y renderGameOverOverlay().
     */
    public void drawRect(float x, float y, float width, float height, float r, float g, float b) {
        GL20.glUniform2f(offsetLocation, x, y);
        GL20.glUniform2f(scaleLocation, width, height);
        GL20.glUniform3f(colorLocation, r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /**
     * Libera los recursos propios de OpenGL.
     *
     * Recibe: nada.
     * Modifica: elimina VAO, VBO y shader program si existen.
     * Devuelve: nada.
     * Momento: Game.cleanup() lo llama al cerrar el juego.
     */
    public void cleanup() {
        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            vao = 0;
        }
        if (vbo != 0) {
            GL15.glDeleteBuffers(vbo);
            vbo = 0;
        }
        if (program != 0) {
            GL20.glDeleteProgram(program);
            program = 0;
        }
    }
}
