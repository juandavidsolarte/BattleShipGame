package com.example.battleship.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Esta clase pertenece a la VISTA porque maneja PIXELES y COLORES.
 * Renderiza los barcos de la forma más sencilla posible.
 */
public class CanvasShipRenderer implements ShipRenderer {

    // Colores solicitados (En JavaFX las constantes suelen terminar en GRAY)
    private final Color shipColor = Color.DARKGRAY;
    private final Color deckColor = Color.SLATEGRAY;

    @Override
    public void render(Canvas canvas, int size) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Limpiar el canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Definir márgenes y dimensiones
        double padding = 8;
        double width = canvas.getWidth() - 2 * padding;
        double height = canvas.getHeight() - 2 * padding;
        double startX = padding;
        double startY = padding;
        double cellWidth = width / size;

        // --- DIBUJAR CASCO (Forma base) ---
        gc.setFill(shipColor);
        gc.setStroke(deckColor);
        gc.setLineWidth(2);

        // La punta del barco (proa)
        double bowLength = Math.min(cellWidth, height * 0.8);

        // Coordenadas del polígono simple
        double[] xPoints = {
                startX,                       // Atrás Arriba
                startX + width - bowLength,   // Inicio Punta Arriba
                startX + width,               // Punta Final
                startX + width - bowLength,   // Inicio Punta Abajo
                startX                        // Atrás Abajo
        };
        double[] yPoints = {
                startY,
                startY,
                startY + height / 2,          // Centro vertical (punta)
                startY + height,
                startY + height
        };
        int nPoints = xPoints.length;

        // Dibujar la forma sólida y su borde
        gc.fillPolygon(xPoints, yPoints, nPoints);
        gc.strokePolygon(xPoints, yPoints, nPoints);

        // --- DIBUJAR LÍNEAS DIVISORIAS (Único detalle) ---
        gc.setStroke(deckColor);
        gc.setLineWidth(1);

        for (int i = 1; i < size; i++) {
            double lineX = startX + i * cellWidth;

            // Calculamos el recorte de la línea si cae en la zona de la punta triangular
            double lineTopY = startY;
            double lineBottomY = startY + height;

            if (lineX > startX + width - bowLength) {
                double ratio = (startX + width - lineX) / bowLength;
                double deltaY = (height / 2) * (1 - ratio);
                lineTopY += deltaY;
                lineBottomY -= deltaY;
            }

            gc.strokeLine(lineX, lineTopY, lineX, lineBottomY);
        }
    }
}