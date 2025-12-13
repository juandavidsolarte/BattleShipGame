package com.example.battleship.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Renders a cartoon-style bomb graphic for hit markers.
 * We create a detailed, visually appealing bomb with gradient body,
 * fuse, and flame effects to make hits feel impactful and satisfying.
 */
public class CanvasBombRenderer {

    /**
     * Draws a bomb graphic centered in the specified cell.
     * We use gradients and layered shapes to create a 3D-like appearance
     * with realistic lighting and fire effects.
     */
    public void render(GraphicsContext gc, double cellSize) {
        gc.clearRect(0, 0, cellSize, cellSize);

        double centerX = cellSize / 2;
        double centerY = cellSize / 2 + (cellSize * 0.05);
        double radius = cellSize * 0.30;

        // BOMB BODY - Radial gradient for spherical appearance
        javafx.scene.paint.RadialGradient bombGradient = new javafx.scene.paint.RadialGradient(
                0, 0, centerX - (radius*0.3), centerY - (radius*0.3), radius * 1.3, false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.DARKGRAY),
                new javafx.scene.paint.Stop(1, Color.BLACK)
        );
        gc.setFill(bombGradient);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // FUSE CAP
        double capWidth = radius * 0.5;
        double capHeight = radius * 0.25;
        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(centerX - (capWidth / 2), centerY - radius - (capHeight / 2), capWidth, capHeight);


        // FUSE - Curved Bezier path
        gc.beginPath();
        gc.moveTo(centerX, centerY - radius - (capHeight / 2));
        gc.quadraticCurveTo(centerX + (radius*0.5), centerY - radius - (radius*0.8),
                centerX + (radius*0.8), centerY - radius - (radius*0.4));
        gc.setStroke(Color.SADDLEBROWN);
        gc.setLineWidth(cellSize * 0.05);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.stroke();
        gc.closePath();



        // FLAME - Layered gradient effect
        double flameBaseX = centerX + (radius*0.8);
        double flameBaseY = centerY - radius - (radius*0.4);
        double flameSize = cellSize * 0.15;

        // Outer red layer
        gc.setFill(Color.RED);
        gc.fillOval(flameBaseX - (flameSize/2), flameBaseY - flameSize, flameSize, flameSize * 1.2);

        // Middle orange layer
        gc.setFill(Color.ORANGE);
        gc.fillOval(flameBaseX - (flameSize*0.3), flameBaseY - (flameSize*0.8), flameSize*0.6, flameSize * 0.8);

        // Inner yellow core
        gc.setFill(Color.YELLOW);
        gc.fillOval(flameBaseX - (flameSize*0.15), flameBaseY - (flameSize*0.6), flameSize*0.3, flameSize * 0.5);
    }
}
