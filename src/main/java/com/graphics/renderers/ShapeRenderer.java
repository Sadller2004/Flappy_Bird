package com.graphics.renderers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Esta clase centraliza las primitivas geometricas comunes para que Renderer.java
 * no repita codigo de VAO/VBO ni crezca con detalles de dibujo.
 *
 * Renderer coordina el frame y los renderizadores especializados deciden que se
 * dibuja; ShapeRenderer solo sabe dibujar rectangulos, triangulos, poligonos y
 * elipses usando OpenGL puro.
 */
public class ShapeRenderer {

    private static final int MAX_SHAPE_VERTICES = 256;
    private static final int ELLIPSE_SEGMENTS = 28;

    private int vao;
    private int vbo;
    private int shapeVao;
    private int shapeVbo;

    private int offsetLocation;
    private int scaleLocation;
    private int colorLocation;

    private final FloatBuffer shapeBuffer = BufferUtils.createFloatBuffer(MAX_SHAPE_VERTICES * 3);

    /**
     * Inicializa los buffers compartidos por todas las primitivas.
     *
     * Recibe: locations de uniforms del shader activo.
     * Modifica: VAO/VBO del quad base, VAO/VBO dinamico y uniforms guardados.
     * Devuelve: nada.
     * Momento: Renderer.init(), despues de crear y enlazar el shader.
     */
    public void init(int offsetLocation, int scaleLocation, int colorLocation) {
        this.offsetLocation = offsetLocation;
        this.scaleLocation = scaleLocation;
        this.colorLocation = colorLocation;
        createBaseQuad();
        createDynamicShapeBuffer();
    }

    /**
     * Dibuja un rectangulo en NDC usando uniforms sobre el quad base.
     */
    public void drawRect(float x, float y, float width, float height, float r, float g, float b) {
        GL30.glBindVertexArray(vao);
        GL20.glUniform2f(offsetLocation, x, y);
        GL20.glUniform2f(scaleLocation, width, height);
        GL20.glUniform3f(colorLocation, r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /**
     * Dibuja un rectangulo definido en coordenadas locales y rotado alrededor de
     * un origen. Se usa para detalles del pajaro, paneles y segmentos inclinados.
     */
    public void drawRotatedRect(float originX, float originY, float localX, float localY, float width, float height,
            float angle, float r, float g, float b) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float[][] points = {
                { localX - halfW, localY - halfH },
                { localX + halfW, localY - halfH },
                { localX + halfW, localY + halfH },
                { localX - halfW, localY + halfH }
        };
        drawPolygon(originX, originY, points, angle, 1.0f, r, g, b);
    }

    /**
     * Dibuja un triangulo definido en coordenadas locales y transformado por
     * origen, angulo y color.
     */
    public void drawTriangle(float originX, float originY, float x1, float y1, float x2, float y2, float x3, float y3,
            float angle, float r, float g, float b) {
        float[][] points = {
                { x1, y1 },
                { x2, y2 },
                { x3, y3 }
        };
        drawPolygon(originX, originY, points, angle, 1.0f, r, g, b);
    }

    /**
     * Dibuja un poligono convexo mediante triangulacion tipo fan.
     */
    public void drawPolygon(float originX, float originY, float[][] localPoints, float angle, float scale,
            float r, float g, float b) {
        int triangleCount = localPoints.length - 2;
        float[] vertices = new float[triangleCount * 3 * 3];
        int index = 0;

        for (int i = 1; i < localPoints.length - 1; i++) {
            index = addTransformedVertex(vertices, index, originX, originY,
                    localPoints[0][0] * scale, localPoints[0][1] * scale, angle);
            index = addTransformedVertex(vertices, index, originX, originY,
                    localPoints[i][0] * scale, localPoints[i][1] * scale, angle);
            index = addTransformedVertex(vertices, index, originX, originY,
                    localPoints[i + 1][0] * scale, localPoints[i + 1][1] * scale, angle);
        }

        drawDynamicShape(GL11.GL_TRIANGLES, vertices, triangleCount * 3, r, g, b);
    }

    /**
     * Dibuja una elipse aproximada con GL_TRIANGLE_FAN.
     */
    public void drawEllipse(float originX, float originY, float localCenterX, float localCenterY, float radiusX,
            float radiusY, float angle, float r, float g, float b) {
        int vertexCount = ELLIPSE_SEGMENTS + 2;
        float[] vertices = new float[vertexCount * 3];
        int index = 0;

        index = addTransformedVertex(vertices, index, originX, originY, localCenterX, localCenterY, angle);
        for (int i = 0; i <= ELLIPSE_SEGMENTS; i++) {
            double theta = (Math.PI * 2.0 * i) / ELLIPSE_SEGMENTS;
            float px = localCenterX + (float) Math.cos(theta) * radiusX;
            float py = localCenterY + (float) Math.sin(theta) * radiusY;
            index = addTransformedVertex(vertices, index, originX, originY, px, py, angle);
        }

        drawDynamicShape(GL11.GL_TRIANGLE_FAN, vertices, vertexCount, r, g, b);
    }

    /**
     * Envia vertices dinamicos a OpenGL y dibuja la figura indicada.
     */
    public void drawDynamicShape(int mode, float[] vertices, int vertexCount, float r, float g, float b) {
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
    public float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Libera los recursos OpenGL que pertenecen a las primitivas.
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
    }

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
}
