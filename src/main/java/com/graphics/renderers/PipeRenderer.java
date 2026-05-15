package com.graphics.renderers;

import java.util.List;

import com.graphics.Pipe;

/**
 * R4.
 * Dibuja las tuberias con mas detalle visual sin cambiar Pipe ni PipeManager.
 *
 * Recibe: ShapeRenderer compartido.
 * Modifica: solo el framebuffer activo de OpenGL.
 * Devuelve: nada.
 * Momento: Renderer lo usa despues del fondo y antes de los jugadores.
 *
 * La colision sigue usando las dimensiones de Pipe. Los bordes, brillos y tapas
 * son solamente una capa visual para cumplir el requerimiento 2.4.
 */
public class PipeRenderer {

    private static final float CAP_HEIGHT = 0.075f;
    private static final float CAP_EXTRA_WIDTH = 0.080f;

    private final ShapeRenderer shapes;

    public PipeRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    /**
     * R4.
     * Recorre las tuberias activas y dibuja sus dos tramos mejorados.
     *
     * Recibe: lista actual de Pipe.
     * Modifica: framebuffer dibujando cuerpo, borde, sombra, brillo y tapas.
     * Devuelve: nada.
     * Momento: en cada frame, antes de dibujar los pajaros.
     */
    public void renderEnhancedPipes(List<Pipe> pipes) {
        for (Pipe pipe : pipes) {
            if (pipe.getUpperHeight() > 0.0f) {
                renderPipeBody(pipe.getX(), pipe.getUpperCenterY(), pipe.getWidth(), pipe.getUpperHeight());
                renderPipeCap(pipe.getX(), pipe.getGapTop() + CAP_HEIGHT * 0.5f, pipe.getWidth(), true);
            }

            if (pipe.getLowerHeight() > 0.0f) {
                renderPipeBody(pipe.getX(), pipe.getLowerCenterY(), pipe.getWidth(), pipe.getLowerHeight());
                renderPipeCap(pipe.getX(), pipe.getGapBottom() - CAP_HEIGHT * 0.5f, pipe.getWidth(), false);
            }
        }
    }

    /**
     * R4.
     * Dibuja el cuerpo principal de la tuberia con borde, sombra y brillo.
     *
     * Recibe: centro y tamano visual del tramo.
     * Modifica: framebuffer con rectangulos superpuestos.
     * Devuelve: nada.
     * Momento: lo llama renderEnhancedPipes() por cada tramo.
     */
    public void renderPipeBody(float x, float centerY, float width, float height) {
        shapes.drawRect(x + 0.020f, centerY - 0.012f, width + 0.030f, height + 0.020f, 0.02f, 0.05f, 0.05f);
        shapes.drawRect(x, centerY, width + 0.024f, height + 0.020f, 0.03f, 0.12f, 0.08f);
        shapes.drawRect(x, centerY, width, height, 0.12f, 0.63f, 0.24f);
        shapes.drawRect(x - width * 0.28f, centerY, width * 0.16f, height * 0.94f, 0.36f, 0.88f, 0.40f);
        shapes.drawRect(x + width * 0.30f, centerY, width * 0.20f, height * 0.96f, 0.06f, 0.36f, 0.18f);

        int stripeCount = Math.max(1, (int) (height / 0.24f));
        float stripeStep = height / (stripeCount + 1);
        for (int i = 1; i <= stripeCount; i++) {
            float y = centerY - height * 0.5f + stripeStep * i;
            shapes.drawRect(x, y, width * 0.88f, 0.012f, 0.08f, 0.43f, 0.18f);
        }
    }

    /**
     * R4.
     * Dibuja una tapa mas ancha en el borde del gap de la tuberia.
     *
     * Recibe: centro horizontal, centro vertical, ancho base y si pertenece arriba.
     * Modifica: framebuffer con una tapa visual mas elaborada.
     * Devuelve: nada.
     * Momento: despues del cuerpo de cada tramo.
     */
    public void renderPipeCap(float x, float y, float pipeWidth, boolean upperPipe) {
        float capWidth = pipeWidth + CAP_EXTRA_WIDTH;
        float bevelY = upperPipe ? y - CAP_HEIGHT * 0.34f : y + CAP_HEIGHT * 0.34f;

        shapes.drawRect(x + 0.018f, y - 0.010f, capWidth + 0.028f, CAP_HEIGHT + 0.024f, 0.02f, 0.05f, 0.05f);
        shapes.drawRect(x, y, capWidth + 0.020f, CAP_HEIGHT + 0.018f, 0.03f, 0.13f, 0.08f);
        shapes.drawRect(x, y, capWidth, CAP_HEIGHT, 0.16f, 0.73f, 0.27f);
        shapes.drawRect(x - capWidth * 0.30f, y, capWidth * 0.14f, CAP_HEIGHT * 0.76f, 0.45f, 0.95f, 0.45f);
        shapes.drawRect(x + capWidth * 0.31f, y, capWidth * 0.20f, CAP_HEIGHT * 0.82f, 0.07f, 0.39f, 0.18f);
        shapes.drawRect(x, bevelY, capWidth * 0.92f, 0.015f, 0.08f, 0.43f, 0.18f);
    }
}
