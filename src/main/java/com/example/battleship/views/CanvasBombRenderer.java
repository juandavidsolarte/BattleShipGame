package com.example.battleship.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Clase encargada exclusivamente de dibujar una bomba estilo "cartoon"
 * en un GraphicsContext dado.
 */
public class CanvasBombRenderer {

    public void render(GraphicsContext gc, double cellSize) {
        gc.clearRect(0, 0, cellSize, cellSize);

        double centerX = cellSize / 2;
        double centerY = cellSize / 2 + (cellSize * 0.05);
        //radio del cuerpo de la bomba
        double radius = cellSize * 0.30;

        // EL CUERPO
        // Se usa un degradado radial para que se vea esférica, no plana
        javafx.scene.paint.RadialGradient bombGradient = new javafx.scene.paint.RadialGradient(
                0, 0, centerX - (radius*0.3), centerY - (radius*0.3), radius * 1.3, false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.DARKGRAY), // Brillo
                new javafx.scene.paint.Stop(1, Color.BLACK)     // Sombra
        );

        gc.setFill(bombGradient);
        // Dibujo del círculo principal
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);


        // TAPA DE LA MECHA
        double capWidth = radius * 0.5;
        double capHeight = radius * 0.25;
        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(centerX - (capWidth / 2), centerY - radius - (capHeight / 2), capWidth, capHeight);


        // LA MECHA
        gc.beginPath();
        gc.moveTo(centerX, centerY - radius - (capHeight / 2)); // Empezar arriba de la tapa
        // Curva de Bezier hacia arriba y a la derecha
        gc.quadraticCurveTo(centerX + (radius*0.5), centerY - radius - (radius*0.8),
                centerX + (radius*0.8), centerY - radius - (radius*0.4));

        gc.setStroke(Color.SADDLEBROWN);
        gc.setLineWidth(cellSize * 0.05);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.stroke();
        gc.closePath();


        // LA LLAMA
        // La punta de la mecha
        double flameBaseX = centerX + (radius*0.8);
        double flameBaseY = centerY - radius - (radius*0.4);
        double flameSize = cellSize * 0.15;

        // Parte Roja (Exterior)
        gc.setFill(Color.RED);
        gc.fillOval(flameBaseX - (flameSize/2), flameBaseY - flameSize, flameSize, flameSize * 1.2);

        // Parte Naranja (Medio)
        gc.setFill(Color.ORANGE);
        gc.fillOval(flameBaseX - (flameSize*0.3), flameBaseY - (flameSize*0.8), flameSize*0.6, flameSize * 0.8);

        // Parte Amarilla (Centro)
        gc.setFill(Color.YELLOW);
        gc.fillOval(flameBaseX - (flameSize*0.15), flameBaseY - (flameSize*0.6), flameSize*0.3, flameSize * 0.5);
    }
}
