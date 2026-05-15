package com.graphics.renderers;

/**
 * R4.
 * Dibuja HUD, pantalla de inicio y pantalla de game over usando solo
 * primitivas de OpenGL.
 *
 * Recibe: ShapeRenderer compartido.
 * Modifica: framebuffer activo con paneles, indicadores y numeros digitales.
 * Devuelve: nada.
 * Momento: Renderer lo llama al final del frame, encima del juego.
 *
 * No usa fuentes, texturas ni sprites. Los puntajes y el nivel se dibujan con
 * rectangulos estilo display digital de siete segmentos.
 */
public class HudRenderer {

    private static final boolean[][] DIGITS = {
            { true, true, true, true, true, true, false },
            { false, true, true, false, false, false, false },
            { true, true, false, true, true, false, true },
            { true, true, true, true, false, false, true },
            { false, true, true, false, false, true, true },
            { true, false, true, true, false, true, true },
            { true, false, true, true, true, true, true },
            { true, true, true, false, false, false, false },
            { true, true, true, true, true, true, true },
            { true, true, true, true, false, true, true }
    };

    private final ShapeRenderer shapes;

    public HudRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    /**
     * R4.
     * Dibuja puntajes, nivel y pequenos indicadores de dificultad.
     *
     * Recibe: puntajes, nivel, velocidad e intervalo actual de aparicion.
     * Modifica: framebuffer con HUD sobre la escena.
     * Devuelve: nada.
     * Momento: en cada frame, despues de jugadores y tuberias.
     */
    public void renderHud(int scorePlayer1, int scorePlayer2, int currentLevel, float currentPipeSpeed,
            float currentSpawnInterval) {
        renderPlayerBadge(-0.88f, 0.88f, 0.90f, 0.17f, 0.31f);
        renderNumber(scorePlayer1, -0.78f, 0.88f, 0.72f, 1.0f, 0.24f, 0.34f);

        renderPlayerBadge(0.52f, 0.88f, 0.10f, 0.28f, 1.00f);
        renderNumber(scorePlayer2, 0.63f, 0.88f, 0.72f, 0.20f, 0.42f, 1.0f);

        shapes.drawRect(-0.08f, 0.88f, 0.25f, 0.12f, 0.04f, 0.06f, 0.14f);
        shapes.drawRect(-0.17f, 0.88f, 0.030f, 0.070f, 0.50f, 0.94f, 0.60f);
        renderNumber(currentLevel, -0.05f, 0.88f, 0.62f, 0.50f, 0.94f, 0.60f);

        renderDifficultyBars(0.15f, 0.91f, currentPipeSpeed, currentSpawnInterval);
    }

    /**
     * R4.
     * Dibuja la pantalla inicial con panel central e instrucciones visuales.
     *
     * Recibe: nada.
     * Modifica: framebuffer con panel e indicadores de controles.
     * Devuelve: nada.
     * Momento: cuando started == false.
     */
    public void renderStartScreen() {
        shapes.drawRect(0.0f, 0.03f, 1.12f, 0.62f, 0.03f, 0.04f, 0.12f);
        shapes.drawRect(0.0f, 0.03f, 1.04f, 0.54f, 0.10f, 0.09f, 0.22f);

        // R4. Titulo geometrico: barras grandes que sugieren una pantalla de inicio.
        shapes.drawRect(0.0f, 0.25f, 0.56f, 0.045f, 0.96f, 0.44f, 0.56f);
        shapes.drawRect(0.0f, 0.17f, 0.40f, 0.035f, 0.25f, 0.62f, 1.0f);
        shapes.drawRect(0.0f, 0.10f, 0.24f, 0.025f, 0.82f, 0.88f, 1.0f);

        renderStartControlRow(-0.27f, -0.13f, 0.90f, 0.17f, 0.31f, true);
        renderStartControlRow(0.27f, -0.13f, 0.10f, 0.28f, 1.00f, false);
    }

    /**
     * R4.
     * Dibuja la pantalla de game over con overlay oscuro y pista visual de R.
     *
     * Recibe: nada.
     * Modifica: framebuffer agregando overlay y panel central.
     * Devuelve: nada.
     * Momento: cuando gameOver == true.
     */
    public void renderGameOverScreen() {
        shapes.drawRect(0.0f, 0.0f, 2.0f, 2.0f, 0.02f, 0.02f, 0.06f);
        shapes.drawRect(0.0f, 0.02f, 0.92f, 0.42f, 0.04f, 0.05f, 0.13f);
        shapes.drawRect(0.0f, 0.02f, 0.82f, 0.32f, 0.15f, 0.09f, 0.18f);

        renderGameOverSkull(0.0f, 0.02f, 0.86f);
    }

    /**
     * R4.
     * Dibuja un numero completo usando digitos digitales.
     *
     * Recibe: numero, posicion, escala y color.
     * Modifica: framebuffer con rectangulos de siete segmentos.
     * Devuelve: nada.
     * Momento: lo usan el HUD y cualquier indicador numerico.
     */
    public void renderNumber(int number, float x, float y, float scale, float r, float g, float b) {
        String text = Integer.toString(Math.max(0, number));
        float spacing = 0.078f * scale;

        for (int i = 0; i < text.length(); i++) {
            int digit = text.charAt(i) - '0';
            renderDigit(digit, x + i * spacing, y, scale, r, g, b);
        }
    }

    /**
     * R4.
     * Dibuja un digito con siete segmentos rectangulares.
     *
     * Recibe: digito, posicion central, escala y color.
     * Modifica: framebuffer con los segmentos encendidos.
     * Devuelve: nada.
     * Momento: lo llama renderNumber().
     */
    public void renderDigit(int digit, float x, float y, float scale, float r, float g, float b) {
        if (digit < 0 || digit > 9) {
            return;
        }

        boolean[] segments = DIGITS[digit];
        if (segments[0]) {
            drawSegment(x, y + 0.050f * scale, scale, true, r, g, b);
        }
        if (segments[1]) {
            drawSegment(x + 0.033f * scale, y + 0.025f * scale, scale, false, r, g, b);
        }
        if (segments[2]) {
            drawSegment(x + 0.033f * scale, y - 0.025f * scale, scale, false, r, g, b);
        }
        if (segments[3]) {
            drawSegment(x, y - 0.050f * scale, scale, true, r, g, b);
        }
        if (segments[4]) {
            drawSegment(x - 0.033f * scale, y - 0.025f * scale, scale, false, r, g, b);
        }
        if (segments[5]) {
            drawSegment(x - 0.033f * scale, y + 0.025f * scale, scale, false, r, g, b);
        }
        if (segments[6]) {
            drawSegment(x, y, scale, true, r, g, b);
        }
    }

    /**
     * R4.
     * Dibuja un segmento horizontal o vertical del display digital.
     *
     * Recibe: centro, escala, orientacion y color.
     * Modifica: framebuffer con un rectangulo.
     * Devuelve: nada.
     * Momento: lo llama renderDigit().
     */
    public void drawSegment(float x, float y, float scale, boolean horizontal, float r, float g, float b) {
        float width = horizontal ? 0.052f * scale : 0.012f * scale;
        float height = horizontal ? 0.012f * scale : 0.045f * scale;
        shapes.drawRect(x, y, width, height, r, g, b);
    }

    private void renderPlayerBadge(float x, float y, float r, float g, float b) {
        shapes.drawRect(x, y, 0.13f, 0.12f, 0.04f, 0.05f, 0.12f);
        shapes.drawEllipse(x, y, 0.0f, 0.0f, 0.032f, 0.025f, 0.0f, r, g, b);
        shapes.drawTriangle(x, y, -0.034f, 0.000f, -0.066f, -0.020f, -0.044f, 0.025f, 0.0f, r, g, b);
        shapes.drawRect(x + 0.032f, y - 0.004f, 0.043f, 0.014f, 0.04f, 0.05f, 0.12f);
        shapes.drawRect(x + 0.033f, y - 0.004f, 0.031f, 0.008f, 0.88f, 0.90f, 0.92f);
        shapes.drawEllipse(x + 0.018f, y + 0.011f, 0.0f, 0.0f, 0.006f, 0.006f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderDifficultyBars(float x, float y, float currentPipeSpeed, float currentSpawnInterval) {
        float speedFill = shapes.clamp((currentPipeSpeed - 0.62f) / 0.38f, 0.0f, 1.0f);
        float spawnFill = shapes.clamp((1.50f - currentSpawnInterval) / 0.50f, 0.0f, 1.0f);

        shapes.drawRect(x, y, 0.20f, 0.018f, 0.05f, 0.07f, 0.16f);
        shapes.drawRect(x - 0.10f + speedFill * 0.10f, y, 0.20f * speedFill, 0.018f, 0.50f, 0.94f, 0.60f);
        shapes.drawRect(x, y - 0.035f, 0.20f, 0.018f, 0.05f, 0.07f, 0.16f);
        shapes.drawRect(x - 0.10f + spawnFill * 0.10f, y - 0.035f, 0.20f * spawnFill, 0.018f, 0.96f, 0.44f, 0.56f);
    }

    private void renderStartControlRow(float x, float y, float r, float g, float b, boolean spaceKey) {
        renderPlayerBadge(x - 0.12f, y, r, g, b);

        if (spaceKey) {
            shapes.drawRect(x + 0.10f, y, 0.27f, 0.082f, 0.04f, 0.05f, 0.12f);
            shapes.drawRect(x + 0.10f, y, 0.22f, 0.035f, r, g, b);
        } else {
            shapes.drawRect(x + 0.10f, y, 0.15f, 0.12f, 0.04f, 0.05f, 0.12f);
            shapes.drawTriangle(x + 0.10f, y, -0.045f, -0.020f, 0.0f, 0.055f, 0.045f, -0.020f,
                    0.0f, r, g, b);
            shapes.drawRect(x + 0.10f, y - 0.040f, 0.028f, 0.070f, r, g, b);
        }
    }

    /**
     * R4.
     * Dibuja la calavera centrada del game over usando piezas simples tipo pixel art.
     *
     * Recibe: centro y escala visual.
     * Modifica: framebuffer dentro del panel de game over.
     * Devuelve: nada.
     * Momento: lo llama renderGameOverScreen().
     */
    private void renderGameOverSkull(float x, float y, float scale) {
        //renderBone(x - 0.25f * scale, y + 0.07f * scale, -0.58f, scale);
        //renderBone(x + 0.25f * scale, y + 0.07f * scale, 0.58f, scale);
        renderSkull(x, y, scale);
    }

    private void renderSkull(float x, float y, float scale) {
        float darkR = 0.03f;
        float darkG = 0.04f;
        float darkB = 0.06f;
        float boneR = 0.88f;
        float boneG = 0.90f;
        float boneB = 0.88f;
        float shadeR = 0.68f;
        float shadeG = 0.72f;
        float shadeB = 0.72f;

        shapes.drawRect(x, y + 0.035f * scale, 0.33f * scale, 0.21f * scale, darkR, darkG, darkB);
        shapes.drawRect(x, y - 0.095f * scale, 0.20f * scale, 0.13f * scale, darkR, darkG, darkB);

        shapes.drawRect(x, y + 0.045f * scale, 0.27f * scale, 0.17f * scale, boneR, boneG, boneB);
        shapes.drawRect(x, y - 0.075f * scale, 0.15f * scale, 0.10f * scale, boneR, boneG, boneB);
        shapes.drawRect(x - 0.105f * scale, y + 0.080f * scale, 0.035f * scale, 0.055f * scale,
                shadeR, shadeG, shadeB);

        shapes.drawRect(x - 0.070f * scale, y + 0.020f * scale, 0.060f * scale, 0.055f * scale, darkR, darkG, darkB);
        shapes.drawRect(x + 0.070f * scale, y + 0.020f * scale, 0.060f * scale, 0.055f * scale, darkR, darkG, darkB);
        shapes.drawRect(x, y - 0.025f * scale, 0.026f * scale, 0.045f * scale, darkR, darkG, darkB);

        shapes.drawRect(x - 0.050f * scale, y - 0.105f * scale, 0.018f * scale, 0.085f * scale,
                darkR, darkG, darkB);
        shapes.drawRect(x, y - 0.105f * scale, 0.018f * scale, 0.085f * scale, darkR, darkG, darkB);
        shapes.drawRect(x + 0.050f * scale, y - 0.105f * scale, 0.018f * scale, 0.085f * scale,
                darkR, darkG, darkB);
    }

    // private void renderBone(float x, float y, float angle, float scale) {
    //     float darkR = 0.03f;
    //     float darkG = 0.04f;
    //     float darkB = 0.06f;
    //     float boneR = 0.88f;
    //     float boneG = 0.90f;
    //     float boneB = 0.88f;

    //     shapes.drawRotatedRect(x, y, 0.0f, 0.0f, 0.22f * scale, 0.045f * scale, angle, darkR, darkG, darkB);
    //     shapes.drawEllipse(x, y, -0.100f * scale, 0.020f * scale, 0.045f * scale, 0.040f * scale,
    //             angle, darkR, darkG, darkB);
    //     shapes.drawEllipse(x, y, 0.100f * scale, -0.020f * scale, 0.045f * scale, 0.040f * scale,
    //             angle, darkR, darkG, darkB);

    //     shapes.drawRotatedRect(x, y, 0.0f, 0.0f, 0.18f * scale, 0.026f * scale, angle, boneR, boneG, boneB);
    //     shapes.drawEllipse(x, y, -0.086f * scale, 0.017f * scale, 0.031f * scale, 0.027f * scale,
    //             angle, boneR, boneG, boneB);
    //     shapes.drawEllipse(x, y, 0.086f * scale, -0.017f * scale, 0.031f * scale, 0.027f * scale,
    //             angle, boneR, boneG, boneB);
    // }
}
