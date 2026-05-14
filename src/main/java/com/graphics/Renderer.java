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
 * Dibuja todo con un quad base y uniforms de posicion, escala y color.
 */
public class Renderer {

    private int program;
    private int vao;
    private int vbo;
    private int offsetLocation;
    private int scaleLocation;
    private int colorLocation;

    public void init() {
        createShaders();
        createBaseQuad();
    }

    /**
     * Renderiza un frame completo del juego.
     */
    public void render(Bird bird, List<Pipe> pipes, boolean gameOver) {
        renderBackground();

        GL20.glUseProgram(program);
        GL30.glBindVertexArray(vao);

        renderPipes(pipes);
        renderBird(bird);

        if (gameOver) {
            renderGameOverOverlay();
        }

        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    /**
     * Crea shaders 2D: vertex para escala/offset y fragment para color solido.
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

    private void checkShader(int shader, String type) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(type + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    /**
     * Crea un rectangulo unitario centrado en el origen usando 2 triangulos.
     */
    private void createBaseQuad() {
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void renderBackground() {
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    public void renderBird(Bird bird) {
        drawRect(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight(), 0.98f, 0.85f, 0.20f);
    }

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

    public void renderGameOverOverlay() {
        drawRect(0.0f, 0.0f, 2.0f, 0.22f, 0.15f, 0.18f, 0.22f);
    }

    /**
     * Dibuja un rectangulo en NDC usando uniforms sobre el quad base.
     */
    public void drawRect(float x, float y, float width, float height, float r, float g, float b) {
        GL20.glUniform2f(offsetLocation, x, y);
        GL20.glUniform2f(scaleLocation, width, height);
        GL20.glUniform3f(colorLocation, r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /**
     * Libera los recursos propios de OpenGL.
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
