package com.graphics.renderers;

import org.lwjgl.opengl.GL11;

/**
 * R4.
 * Dibuja el fondo mejorado del juego inspirado en una ciudad nocturna.
 *
 * Recibe: nada.
 * Modifica: el framebuffer activo de OpenGL.
 * Devuelve: nada.
 * Momento: se llama al inicio de cada frame antes de dibujar tuberias y jugadores.
 *
 * Esta mejora cumple el requerimiento 2.4 porque reemplaza el fondo plano por
 * una escena con degradado, estrellas, edificios y ventanas, usando solo
 * primitivas de OpenGL y sin afectar la logica de colision ni la jugabilidad.
 */
public class BackgroundRenderer {

    private final ShapeRenderer shapes;

    public BackgroundRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    /**
     * R4.
     * Coordina todas las capas del fondo nocturno.
     *
     * Recibe: nada.
     * Modifica: limpia y redibuja el framebuffer activo.
     * Devuelve: nada.
     * Momento: primer paso de Renderer.render().
     */
    public void renderEnhancedBackground() {
        GL11.glClearColor(0.02f, 0.03f, 0.10f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        renderSkyGradient();
        renderStars();
        renderMoon();
        renderLeftCloud();
        renderCloudDetail();
        renderCitySkyline();
    }

    /**
     * R4.
     * Dibuja un degradado por bandas horizontales de azul oscuro a morado/rosado.
     *
     * Recibe: nada.
     * Modifica: framebuffer con rectangulos de color interpolado.
     * Devuelve: nada.
     * Momento: antes de estrellas y edificios para que sea la capa de fondo.
     */
    public void renderSkyGradient() {
        int bands = 28;
        float bandHeight = 2.0f / bands;

        for (int i = 0; i < bands; i++) {
            float t = i / (float) (bands - 1);
            float r = lerp(0.03f, 0.82f, t);
            float g = lerp(0.08f, 0.20f, t);
            float b = lerp(0.24f, 0.48f, t);
            float y = 1.0f - bandHeight * (i + 0.5f);
            shapes.drawRect(0.0f, y, 2.0f, bandHeight + 0.002f, r, g, b);
        }

        // R4. Franja rosada del horizonte, inspirada en la referencia nocturna.
        shapes.drawRect(0.0f, -0.56f, 2.0f, 0.32f, 0.55f, 0.12f, 0.38f);
        shapes.drawRect(0.0f, -0.75f, 2.0f, 0.28f, 0.24f, 0.10f, 0.30f);
    }

    /**
     * R4.
     * Dibuja estrellas con rectangulos pequenos y elipses.
     *
     * Recibe: nada.
     * Modifica: framebuffer agregando puntos brillantes en el cielo.
     * Devuelve: nada.
     * Momento: despues del degradado y antes de la ciudad.
     */
    public void renderStars() {
        float[][] stars = {
                { -0.92f, 0.90f, 0.006f }, { -0.76f, 0.82f, 0.007f }, { -0.61f, 0.94f, 0.005f },
                { -0.40f, 0.76f, 0.006f }, { -0.22f, 0.88f, 0.008f }, { -0.05f, 0.70f, 0.005f },
                { 0.14f, 0.96f, 0.006f }, { 0.33f, 0.82f, 0.005f }, { 0.55f, 0.91f, 0.007f },
                { 0.72f, 0.73f, 0.006f }, { 0.89f, 0.84f, 0.005f }, { -0.82f, 0.56f, 0.005f },
                { -0.54f, 0.62f, 0.004f }, { -0.31f, 0.49f, 0.006f }, { 0.03f, 0.55f, 0.004f },
                { 0.25f, 0.63f, 0.005f }, { 0.49f, 0.50f, 0.004f }, { 0.79f, 0.57f, 0.006f }
        };

        for (float[] star : stars) {
            shapes.drawEllipse(star[0], star[1], 0.0f, 0.0f, star[2], star[2], 0.0f, 0.86f, 0.90f, 1.0f);
        }
    }

    /**
     * R4.
     * Agrega una nube geometrica tenue para enriquecer el cielo sin usar imagenes.
     *
     * Recibe: nada.
     * Modifica: framebuffer dibujando elipses azules semioscuras.
     * Devuelve: nada.
     * Momento: despues de las estrellas, antes de los edificios.
     */
    public void renderCloudDetail() {
        shapes.drawEllipse(0.50f, 0.56f, 0.0f, 0.0f, 0.13f, 0.020f, 0.0f, 0.28f, 0.30f, 0.55f);
        shapes.drawEllipse(0.62f, 0.58f, 0.0f, 0.0f, 0.11f, 0.025f, 0.0f, 0.35f, 0.36f, 0.62f);
        shapes.drawEllipse(0.76f, 0.55f, 0.0f, 0.0f, 0.14f, 0.018f, 0.0f, 0.25f, 0.28f, 0.50f);
        shapes.drawRect(0.50f, 0.525f, 0.22f, 0.008f, 0.18f, 0.22f, 0.44f);
        shapes.drawRect(0.68f, 0.515f, 0.34f, 0.006f, 0.18f, 0.22f, 0.44f);
    }

    /**
     * R4.
     * Dibuja una luna creciente con tonos suaves, no un circulo blanco plano.
     *
     * Recibe: nada.
     * Modifica: framebuffer con elipses superpuestas para formar la luna.
     * Devuelve: nada.
     * Momento: despues de estrellas y antes de nubes/ciudad.
     */
    public void renderMoon() {
        shapes.drawEllipse(-0.70f, 0.66f, 0.0f, 0.0f, 0.080f, 0.080f, 0.0f, 0.90f, 0.86f, 0.68f);
        shapes.drawEllipse(-0.67f, 0.68f, 0.0f, 0.0f, 0.070f, 0.075f, 0.0f, 0.74f, 0.22f, 0.44f);
        shapes.drawEllipse(-0.72f, 0.69f, 0.0f, 0.0f, 0.014f, 0.010f, 0.0f, 0.72f, 0.68f, 0.55f);
        shapes.drawEllipse(-0.75f, 0.62f, 0.0f, 0.0f, 0.011f, 0.008f, 0.0f, 0.74f, 0.70f, 0.58f);
    }

    /**
     * R4.
     * Agrega una nube hacia la izquierda para equilibrar el paisaje nocturno.
     *
     * Recibe: nada.
     * Modifica: framebuffer con elipses y una base rectangular tenue.
     * Devuelve: nada.
     * Momento: antes de la ciudad, lejos del HUD y de las tuberias principales.
     */
    public void renderLeftCloud() {
        shapes.drawEllipse(-0.58f, 0.46f, 0.0f, 0.0f, 0.12f, 0.019f, 0.0f, 0.22f, 0.25f, 0.48f);
        shapes.drawEllipse(-0.47f, 0.48f, 0.0f, 0.0f, 0.10f, 0.024f, 0.0f, 0.30f, 0.32f, 0.58f);
        shapes.drawEllipse(-0.36f, 0.45f, 0.0f, 0.0f, 0.13f, 0.018f, 0.0f, 0.20f, 0.24f, 0.46f);
        shapes.drawRect(-0.48f, 0.425f, 0.34f, 0.010f, 0.18f, 0.22f, 0.44f);
    }

    /**
     * R4.
     * Dibuja siluetas de edificios y sus ventanas iluminadas.
     *
     * Recibe: nada.
     * Modifica: framebuffer con la ciudad nocturna.
     * Devuelve: nada.
     * Momento: ultima capa del fondo, detras de tuberias y jugadores.
     */
    public void renderCitySkyline() {
        float[][] buildings = {
                { -0.92f, -0.67f, 0.15f, 0.58f },
                { -0.75f, -0.70f, 0.12f, 0.38f },
                { -0.56f, -0.64f, 0.22f, 0.62f },
                { -0.30f, -0.72f, 0.16f, 0.34f },
                { -0.08f, -0.69f, 0.12f, 0.42f },
                { 0.15f, -0.63f, 0.18f, 0.52f },
                { 0.38f, -0.71f, 0.20f, 0.36f },
                { 0.63f, -0.65f, 0.16f, 0.55f },
                { 0.84f, -0.69f, 0.22f, 0.45f }
        };

        for (int i = 0; i < buildings.length; i++) {
            float[] b = buildings[i];
            float x = b[0];
            float y = b[1];
            float w = b[2];
            float h = b[3];
            float tone = 0.05f + (i % 3) * 0.025f;

            shapes.drawRect(x + 0.018f, y - 0.015f, w, h, 0.02f, 0.02f, 0.08f);
            shapes.drawRect(x, y, w, h, tone, 0.05f, 0.14f + tone);
            renderBuildingWindows(x, y, w, h, i);
        }

        // R4. Antenas y techos para que la silueta no sea una fila plana.
        shapes.drawRect(-0.56f, -0.32f, 0.15f, 0.035f, 0.03f, 0.03f, 0.09f);
        shapes.drawRect(-0.48f, -0.27f, 0.020f, 0.13f, 0.03f, 0.03f, 0.09f);
        shapes.drawTriangle(0.15f, -0.39f, -0.065f, 0.020f, 0.0f, 0.080f, 0.065f, 0.020f,
                0.0f, 0.03f, 0.03f, 0.09f);
        shapes.drawRect(0.63f, -0.36f, 0.018f, 0.16f, 0.03f, 0.03f, 0.09f);
        shapes.drawTriangle(0.63f, -0.28f, -0.030f, 0.000f, 0.0f, 0.065f, 0.030f, 0.000f,
                0.0f, 0.03f, 0.03f, 0.09f);

        shapes.drawRect(0.0f, -0.94f, 2.0f, 0.16f, 0.02f, 0.02f, 0.07f);
    }

    /**
     * R4.
     * Dibuja ventanas iluminadas dentro de un edificio.
     *
     * Recibe: centro, tamano del edificio y semilla visual.
     * Modifica: framebuffer con pequenos rectangulos amarillos, rosados y azules.
     * Devuelve: nada.
     * Momento: lo llama renderCitySkyline() por cada edificio.
     */
    public void renderBuildingWindows(float buildingX, float buildingY, float width, float height, int seed) {
        int columns = Math.max(2, (int) (width / 0.045f));
        int rows = Math.max(2, (int) (height / 0.095f));
        float startX = buildingX - width * 0.34f;
        float startY = buildingY - height * 0.36f;
        float stepX = width * 0.68f / Math.max(1, columns - 1);
        float stepY = height * 0.68f / Math.max(1, rows - 1);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (((row * 3 + col * 5 + seed) % 4) == 0) {
                    continue;
                }

                float x = startX + col * stepX;
                float y = startY + row * stepY;
                float warm = ((row + col + seed) % 3 == 0) ? 0.35f : 0.15f;
                shapes.drawRect(x, y, 0.018f, 0.038f, 0.92f, 0.70f + warm * 0.20f, 0.38f + warm);
            }
        }
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
