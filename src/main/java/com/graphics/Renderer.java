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
 * R2.1:
 * El pajaro ya no se representa como un rectangulo simple. La figura visual se
 * arma en Renderer con varias figuras geometricas de OpenGL: elipses,
 * rectangulos, triangulos y poligonos. Bird conserva la fisica y la caja AABB
 * de colision; Renderer solo usa esa posicion como centro del dibujo.
 */
public class Renderer {

    // -------------------------------------R1.----------------------------------------------
    private static final int MAX_SHAPE_VERTICES = 128;
    private static final int ELLIPSE_SEGMENTS = 24;
    private int shapeVao;
    private int shapeVbo;
    private final FloatBuffer shapeBuffer = BufferUtils.createFloatBuffer(MAX_SHAPE_VERTICES * 3);
    // -------------------------------------------//-------------------------------------------

    /*
     * program: shader program enlazado. Combina vertex shader + fragment shader.
     * vao/vbo: quad base usado por drawRect(), igual que el renderer original.
     * shapeVao/shapeVbo: buffer dinamico reutilizable para triangulos, poligonos
     * y elipses del pajaro geometrico.
     */
    private int program;
    private int vao;
    private int vbo;

    /*
     * Uniforms: variables del shader que Java puede cambiar antes de dibujar.
     * drawRect() usa offset y scale sobre el quad base. Las figuras dinamicas ya
     * se cargan en coordenadas NDC finales y por eso usan offset 0 y scale 1.
     */
    private int offsetLocation;
    private int scaleLocation;
    private int colorLocation;

    /**
     * -------------------------------R1.-------------------------------
     * Prepara los recursos OpenGL necesarios para dibujar.
     *
     * Recibe: nada.
     * Modifica: program, VAO/VBO del quad, VAO/VBO dinamico y uniforms.
     * Devuelve: nada.
     * Momento: Game.init() lo llama despues de crear el contexto OpenGL.
     */
    public void init() {
        createShaders();
        createBaseQuad();
        createDynamicShapeBuffer();  //animaciones de caida, aleteo y salto
    }

    /**
     * R2.
     * Renderiza un frame completo del juego.
     *
     * Recibe: dos pajaros, lista de pipes, estados vivos y gameOver.
     * Modifica: el framebuffer activo de OpenGL.
     * Devuelve: nada.
     * Momento: Game.render() lo llama una vez por frame.
     */
    public void render(Bird player1, Bird player2, List<Pipe> pipes, boolean player1Alive, boolean player2Alive,
            boolean gameOver) {
        renderBackground();

        /*
         * Antes de dibujar, se activa el shader program. Cada helper enlaza el VAO
         * que necesita: drawRect() usa el quad base y las figuras del pajaro usan el
         * VBO dinamico.
         */
        GL20.glUseProgram(program);

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
     * Jugador 1: mismo pajaro geometrico, pero con cresta, ala, cola y franja
     * rojo/amaranto para distinguirlo claramente del jugador 2.
     */
    public void renderBirdPlayer1(Bird bird) {
        renderGeometricBird(bird, 0.90f, 0.17f, 0.31f, true);
    }

    /**
     * Jugador 2: mismo pajaro geometrico, pero con cresta, ala, cola y franja
     * azul para mantener la identidad visual del segundo jugador.
     */
    public void renderBirdPlayer2(Bird bird) {
        renderGeometricBird(bird, 0.10f, 0.28f, 1.00f, true);
    }

    /**
     * Pajaro muerto: conserva la figura compuesta, pero usa colores apagados.
     * Esto evita volver al rectangulo simple y permite ver que ese jugador ya no
     * esta activo mientras el otro puede seguir jugando.
     */
    public void renderDeadBird(Bird bird) {
        renderGeometricBird(bird, 0.35f, 0.35f, 0.35f, false);
    }

    /**
     * Dibuja un pajaro estilo caricatura/pixel art inspirado en Mordecai de "Un Show mas".
     *
     * Figuras que forman al pajaro:
     * - cuerpo principal: elipse blanca con borde negro;
     * - cabeza/cresta: poligono del color del jugador con borde negro;
     * - pico: rectangulo alargado oscuro con borde;
     * - ala: poligono del color del jugador con detalles blancos;
     * - cola: triangulo del color del jugador;
     * - ojo: elipse blanca con pupila negra.
     *
     * El renderizado vive en Renderer porque Bird solo representa fisica,
     * posicion y colisiones. Asi la caja AABB de Bird.getLeft()/getRight() no se
     * altera aunque el dibujo sea mas detallado y un poco mas expresivo.
     *
     * La inclinacion se calcula con velocityY: velocidad positiva inclina el ave
     * hacia arriba y velocidad negativa la inclina hacia abajo. clamp() limita el
     * angulo para que el giro siga siendo jugable y legible.
     *
     * El ala usa la misma velocityY para subir cuando el pajaro salta y bajar
     * cuando cae. No se agrega timer nuevo: la animacion queda sincronizada con
     * la fisica existente.
     */
    private void renderGeometricBird(Bird bird, float mainR, float mainG, float mainB, boolean alive) {
        float x = bird.getX();
        float y = bird.getY();

        float angle = clamp(bird.getVelocityY() * 0.28f, -0.42f, 0.35f);
        float wingLift = clamp(bird.getVelocityY() * 0.018f, -0.018f, 0.020f);

        float playerColorR = alive ? mainR : 0.42f;
        float playerColorG = alive ? mainG : 0.42f;
        float playerColorB = alive ? mainB : 0.42f;
        float white = alive ? 1.00f : 0.72f;
        float beak = alive ? 0.43f : 0.34f;
        float outline = 0.00f;

        // Cola pequena: triangulo con borde negro, ubicada detras del cuerpo.
        drawTriangle(x, y,
                -0.070f, -0.006f,
                -0.123f, -0.034f,
                -0.078f, 0.032f,
                angle, outline, outline, outline);
        drawTriangle(x, y,
                -0.073f, -0.007f,
                -0.110f, -0.027f,
                -0.080f, 0.024f,
                angle, playerColorR, playerColorG, playerColorB);

        // Ala visible: poligono celeste; wingLift simula el aleteo.
        float[][] wing = {
                { -0.062f, -0.004f + wingLift },
                { -0.112f, -0.050f + wingLift },
                { -0.040f, -0.048f + wingLift },
                { -0.014f, -0.014f + wingLift }
        };
        drawPolygon(x, y, wing, angle, 1.12f, outline, outline, outline);
        drawPolygon(x, y, wing, angle, 1.00f, playerColorR, playerColorG, playerColorB);
        drawRotatedRect(x, y, -0.073f, -0.031f + wingLift, 0.030f, 0.010f, angle, white, white, white);
        drawRotatedRect(x, y, -0.049f, -0.021f + wingLift, 0.023f, 0.008f, angle, white, white, white);

        // Cuerpo blanco principal, separado de la colision AABB.
        drawEllipse(x, y, -0.004f, -0.007f, 0.060f, 0.043f, angle, outline, outline, outline);
        drawEllipse(x, y, -0.002f, -0.006f, 0.052f, 0.035f, angle, white, white, white);

        // Cresta/cabeza celeste superior, hecha con poligono para recordar el pixel
        // art.
        float[][] crest = {
                { -0.060f, 0.006f },
                { -0.045f, 0.067f },
                { -0.015f, 0.038f },
                { -0.002f, 0.088f },
                { 0.070f, 0.025f },
                { 0.030f, 0.020f },
                { 0.010f, -0.004f },
                { -0.030f, 0.000f }
        };
        drawPolygon(x, y, crest, angle, 1.08f, outline, outline, outline);
        drawPolygon(x, y, crest, angle, 0.96f, playerColorR, playerColorG, playerColorB);

        // Franja de acento: usa el mismo color principal para reforzar la identidad.
        drawRotatedRect(x, y, -0.036f, 0.001f, 0.057f, 0.011f, angle, outline, outline, outline);
        drawRotatedRect(x, y, -0.036f, 0.003f, 0.048f, 0.007f, angle,
                playerColorR, playerColorG, playerColorB);

        // Cara blanca frontal.
        drawEllipse(x, y, 0.028f, 0.010f, 0.044f, 0.034f, angle, outline, outline, outline);
        drawEllipse(x, y, 0.028f, 0.010f, 0.036f, 0.026f, angle, white, white, white);

        // Pico corto oscuro: mide la mitad que antes y mantiene el borde unido a la cara.
        drawRotatedRect(x, y, 0.055f, -0.012f, 0.060f, 0.030f, angle, outline, outline, outline);
        drawRotatedRect(x, y, 0.055f, -0.012f, 0.054f, 0.018f, angle, beak, beak, beak);
        drawRotatedRect(x, y, 0.055f, -0.012f, 0.048f, 0.004f, angle, 0.04f, 0.04f, 0.04f);

        // Ojo y pupila interna.
        drawEllipse(x, y, 0.026f, 0.023f, 0.018f, 0.018f, angle, outline, outline, outline);
        drawEllipse(x, y, 0.026f, 0.023f, 0.013f, 0.013f, angle, white, white, white);
        drawEllipse(x, y, 0.033f, 0.022f, 0.0045f, 0.0045f, angle, 0.02f, 0.02f, 0.02f);
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

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Crea un VBO dinamico reutilizable para figuras que no son el quad base.
     *
     * Recibe: nada.
     * Modifica: shapeVao y shapeVbo.
     * Devuelve: nada.
     * Momento: durante Renderer.init().
     *
     * Para R2.1 las elipses y poligonos cambian por inclinacion del pajaro.
     * Por eso se actualiza el contenido del VBO con glBufferSubData cada dibujo,
     * pero se reutiliza el mismo buffer para no crear/destruir recursos por frame.
     */
    private void createDynamicShapeBuffer() {
        shapeVao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(shapeVao);

        shapeVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, shapeVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) MAX_SHAPE_VERTICES * 3L * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

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
     * Momento: lo usan tuberias, overlay y partes rectangulares simples.
     */
    public void drawRect(float x, float y, float width, float height, float r, float g, float b) {
        GL30.glBindVertexArray(vao);
        GL20.glUniform2f(offsetLocation, x, y);
        GL20.glUniform2f(scaleLocation, width, height);
        GL20.glUniform3f(colorLocation, r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /**
     * Dibuja un rectangulo que rota alrededor del centro del pajaro.
     *
     * Recibe: centro del pajaro, posicion local, tamano, angulo y color.
     * Modifica: VBO dinamico y framebuffer.
     * Devuelve: nada.
     * Momento: se usa para pico, franja de acento y detalles del ala.
     */
    private void drawRotatedRect(float birdX, float birdY, float localX, float localY, float width, float height,
            float angle, float r, float g, float b) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float[][] points = {
                { localX - halfW, localY - halfH },
                { localX + halfW, localY - halfH },
                { localX + halfW, localY + halfH },
                { localX - halfW, localY + halfH }
        };
        drawPolygon(birdX, birdY, points, angle, 1.0f, r, g, b);
    }

    /**
     * Dibuja un triangulo transformado por la inclinacion del pajaro.
     *
     * Recibe: tres puntos locales, origen, angulo y color.
     * Modifica: VBO dinamico y framebuffer.
     * Devuelve: nada.
     * Momento: se usa para la cola y cualquier parte triangular del personaje.
     */
    private void drawTriangle(float birdX, float birdY, float x1, float y1, float x2, float y2, float x3, float y3,
            float angle, float r, float g, float b) {
        float[][] points = {
                { x1, y1 },
                { x2, y2 },
                { x3, y3 }
        };
        drawPolygon(birdX, birdY, points, angle, 1.0f, r, g, b);
    }

    /**
     * Dibuja un poligono convexo mediante triangulacion tipo fan.
     *
     * Recibe: puntos locales del poligono, centro del pajaro, angulo, escala y
     * color.
     * Modifica: VBO dinamico y framebuffer.
     * Devuelve: nada.
     * Momento: se usa para ala y cresta, figuras que no son rectangulos.
     */
    private void drawPolygon(float birdX, float birdY, float[][] localPoints, float angle, float scale,
            float r, float g, float b) {
        int triangleCount = localPoints.length - 2;
        float[] vertices = new float[triangleCount * 3 * 3];
        int index = 0;

        for (int i = 1; i < localPoints.length - 1; i++) {
            index = addTransformedVertex(vertices, index, birdX, birdY,
                    localPoints[0][0] * scale, localPoints[0][1] * scale, angle);
            index = addTransformedVertex(vertices, index, birdX, birdY,
                    localPoints[i][0] * scale, localPoints[i][1] * scale, angle);
            index = addTransformedVertex(vertices, index, birdX, birdY,
                    localPoints[i + 1][0] * scale, localPoints[i + 1][1] * scale, angle);
        }

        drawDynamicShape(GL11.GL_TRIANGLES, vertices, triangleCount * 3, r, g, b);
    }

    /**
     * Dibuja una elipse aproximada con GL_TRIANGLE_FAN.
     *
     * Recibe: centro local, radios, angulo y color.
     * Modifica: VBO dinamico y framebuffer.
     * Devuelve: nada.
     * Momento: se usa para cuerpo, cara, ojo y pupila.
     *
     * La elipse es un circulo escalado por radiusX/radiusY. ELLIPSE_SEGMENTS
     * define cuantos triangulos forman el borde; 24 es suficiente para verse
     * suave en este tamano sin complicar el proyecto.
     */
    private void drawEllipse(float birdX, float birdY, float localCenterX, float localCenterY, float radiusX,
            float radiusY, float angle, float r, float g, float b) {
        int vertexCount = ELLIPSE_SEGMENTS + 2;
        float[] vertices = new float[vertexCount * 3];
        int index = 0;

        index = addTransformedVertex(vertices, index, birdX, birdY, localCenterX, localCenterY, angle);
        for (int i = 0; i <= ELLIPSE_SEGMENTS; i++) {
            double theta = (Math.PI * 2.0 * i) / ELLIPSE_SEGMENTS;
            float px = localCenterX + (float) Math.cos(theta) * radiusX;
            float py = localCenterY + (float) Math.sin(theta) * radiusY;
            index = addTransformedVertex(vertices, index, birdX, birdY, px, py, angle);
        }

        drawDynamicShape(GL11.GL_TRIANGLE_FAN, vertices, vertexCount, r, g, b);
    }

    /**
     * Convierte un punto local del pajaro a coordenadas NDC finales.
     *
     * Recibe: arreglo destino, origen del pajaro, punto local y angulo.
     * Modifica: vertices, agregando x/y/z.
     * Devuelve: el siguiente indice libre.
     * Momento: lo usan las figuras dinamicas antes de enviarse a OpenGL.
     */
    private int addTransformedVertex(float[] vertices, int index, float originX, float originY, float localX,
            float localY, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        float rotatedX = localX * cos - localY * sin;
        float rotatedY = localX * sin + localY * cos;

        vertices[index++] = originX + rotatedX;
        vertices[index++] = originY + rotatedY;
        vertices[index++] = 0.0f;
        return index;
    }

    /**
     * Envia vertices dinamicos a OpenGL y dibuja la figura indicada.
     *
     * Recibe: modo OpenGL, vertices, cantidad de vertices y color.
     * Modifica: shapeVbo, uniforms y framebuffer.
     * Devuelve: nada.
     * Momento: lo usan triangulos, poligonos y elipses.
     */
    private void drawDynamicShape(int mode, float[] vertices, int vertexCount, float r, float g, float b) {
        if (vertexCount <= 0 || vertexCount > MAX_SHAPE_VERTICES) {
            throw new IllegalArgumentException("Cantidad de vertices invalida: " + vertexCount);
        }

        shapeBuffer.clear();
        shapeBuffer.put(vertices, 0, vertexCount * 3);
        shapeBuffer.flip();

        GL30.glBindVertexArray(shapeVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, shapeVbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, shapeBuffer);

        GL20.glUniform2f(offsetLocation, 0.0f, 0.0f);
        GL20.glUniform2f(scaleLocation, 1.0f, 1.0f);
        GL20.glUniform3f(colorLocation, r, g, b);
        GL11.glDrawArrays(mode, 0, vertexCount);
    }

    /**
     * Limita un valor entre minimo y maximo.
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
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
        if (shapeVao != 0) {
            GL30.glDeleteVertexArrays(shapeVao);
            shapeVao = 0;
        }
        if (shapeVbo != 0) {
            GL15.glDeleteBuffers(shapeVbo);
            shapeVbo = 0;
        }
        if (program != 0) {
            GL20.glDeleteProgram(program);
            program = 0;
        }
    }
}
