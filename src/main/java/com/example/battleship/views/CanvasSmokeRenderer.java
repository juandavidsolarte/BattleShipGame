package com.example.battleship.views;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * Renders smoke and fire effects for damaged ship cells.
 * We use layered radial gradients to create realistic smoke clouds
 * and fire effects that visually communicate ship damage intensity.
 */
public class CanvasSmokeRenderer {

    /**
     * Draws a smoke and fire cloud effect on the provided canvas.
     * We build the effect with overlapping ellipses using radial gradients
     * to achieve volume, transparency, and dynamic visual appeal.
     *
     * @param canvas The canvas where the effect will be rendered
     */
    public void draw(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Clear previous content
        gc.clearRect(0, 0, w, h);

        // Fire color palette - warm tones for burning effect
        Color fireCore = Color.rgb(255, 200, 50, 0.9);  // Amarillo naranja brillante centro
        Color fireMid = Color.rgb(255, 100, 0, 0.8);    // Naranja intenso
        Color fireEdge = Color.rgb(200, 50, 0, 0.0);    // Rojo transparente borde

        // Smoke color palette - cool grays for ash and smoke
        Color ashCore = Color.rgb(40, 40, 40, 0.95);   // Gris casi negro denso
        Color ashMid = Color.rgb(80, 80, 80, 0.8);     // Gris medio
        Color ashGrey = Color.rgb(150, 150, 150, 0.5); // Gris ceniza claro
        Color transparent = Color.TRANSPARENT;

        // Base fire layer - main combustion at impact point
        RadialGradient fireBaseGrad = new RadialGradient(
                0, 0, 0.5, 0.7, 0.4, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, fireCore),
                new Stop(0.5, fireMid),
                new Stop(1.0, fireEdge)
        );
        gc.setFill(fireBaseGrad);
        gc.fillOval(w * 0.1, h * 0.4, w * 0.8, h * 0.6);

        // Fire hot spot - intense central flames
        RadialGradient fireSpotGrad = new RadialGradient(
                0, 0, 0.3, 0.6, 0.25, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 80, 0, 0.8)),
                new Stop(1.0, fireEdge)
        );
        gc.setFill(fireSpotGrad);
        gc.fillOval(w * 0.05, h * 0.45, w * 0.5, h * 0.5);

        // Main smoke cloud - dense ash rising from fire
        RadialGradient ashMainGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, ashCore), // Centro denso que tapa la bomba
                new Stop(0.7, ashMid),
                new Stop(1.0, transparent)
        );
        gc.setFill(ashMainGrad);
        gc.fillOval(w * 0.05, h * 0.1, w * 0.9, h * 0.8);

        // Top smoke layer - lighter, rising smoke
        RadialGradient ashTopGrad = new RadialGradient(
                0, 0, 0.5, 0.2, 0.4, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, ashGrey),
                new Stop(1.0, transparent)
        );
        gc.setFill(ashTopGrad);
        gc.fillOval(w * 0.2, h * -0.1, w * 0.6, h * 0.5);

        // Side smoke detail - additional smoke volume
        RadialGradient ashSideGrad = new RadialGradient(
                0, 0, 0.8, 0.4, 0.3, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(60, 60, 60, 0.8)),
                new Stop(1.0, transparent)
        );
        gc.setFill(ashSideGrad);
        gc.fillOval(w * 0.6, h * 0.2, w * 0.4, h * 0.5);
    }
}