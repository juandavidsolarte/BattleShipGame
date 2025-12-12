package com.example.battleship.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.FontWeight;

/**
 * Esta clase pertenece a la VISTA porque maneja PIXELES y COLORES.
 * Renderiza los barcos de la forma más sencilla posible.
 */
public class CanvasShipRenderer implements ShipRenderer {

    // Colores
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
    /*
    // Colores
    private final Color shipColor = Color.DARKGRAY;
    private final Color deckColor = Color.SLATEGRAY;

    @Override
    public void render(Canvas canvas, int size) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // 1. Limpiar el canvas
        gc.clearRect(0, 0,w, h);

        // 2. Definir márgenes y dimensiones
        double padding = h * 0.15; //8
        double shipH = h - (padding * 2);
        double startX = padding;
        double startY = padding;
        double shipW = w - (padding * 2);

        switch (size) {
            case 4:
                drawCarrier(gc, startX, startY, shipW, shipH);
                break;
            case 3:
                drawSubmarine(gc, startX, startY, shipW, shipH);
                break;
            case 2:
                drawDestroyer(gc, startX, startY, shipW, shipH);
                break;
            default:
                drawFrigate(gc, startX, startY, shipW, shipH);
                break;
        }
*/
        /*double width = canvas.getWidth() - 2 * padding;
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

    /*
    private void drawCarrier(GraphicsContext gc, double x, double y, double w, double h) {
// Casco base gris medio
        gc.setFill(createMetalGradient(y, h, Color.rgb(150, 150, 160), Color.rgb(100, 100, 110)));
        double[] xBase = { x, x + w * 0.95, x + w, x + w * 0.95, x };
        double[] yBase = { y, y, y + h / 2, y + h, y + h };
        gc.fillPolygon(xBase, yBase, 5);
        gc.setStroke(Color.DARKSLATEGRAY);
        gc.strokePolygon(xBase, yBase, 5);

        // Cubierta de vuelo (Asfalto oscuro casi negro)
        gc.setFill(Color.rgb(50, 50, 55));
        double deckMargin = h * 0.1;
        double[] xDeck = { x + deckMargin, x + w * 0.9, x + w * 0.95, x + w * 0.9, x + deckMargin };
        double[] yDeck = { y + deckMargin, y + deckMargin, y + h/2, y + h - deckMargin, y + h - deckMargin };
        gc.fillPolygon(xDeck, yDeck, 5);

        // Pistas (Blancas)
        gc.setStroke(Color.WHITESMOKE);
        gc.setLineWidth(2);

        // --- SOLO PISTA RECTA ---
        // La he centrado un poco (0.6) para que se vea mejor alineada con el casco
        gc.strokeLine(x + w * 0.1, y + h * 0.6, x + w * 0.85, y + h * 0.6);

        // Isla (Torre) más grande y moderna atrás
        gc.setFill(Color.rgb(90, 90, 100));
        gc.fillRect(x + w * 0.7, y + h * 0.1, w * 0.15, h * 0.35);

        // Pequeño radar (círculo)
        gc.setFill(Color.DARKGRAY);
        gc.fillOval(x + w * 0.75, y + h * 0.15, h*0.15, h*0.15);
    }
    // --- SUBMARINO (Tamaño 3) ---
    // Forma ovalada/cigarro
    private void drawSubmarine(GraphicsContext gc, double x, double y, double w, double h) {
        Color darkHull = Color.rgb(40, 40, 45);
        Color darkerHull = Color.rgb(20, 20, 25);
        gc.setFill(createMetalGradient(y, h, darkHull, darkerHull));

        // Cuerpo principal largo
        gc.fillRoundRect(x, y + h*0.1, w, h*0.8, h*0.8, h*0.8);

        // Joroba de misiles (Detrás de la torre)
        gc.fillRoundRect(x + w*0.4, y, w*0.4, h*0.4, h*0.2, h*0.2);

        // Torreta (más adelantada)
        double towerW = w * 0.12;
        double towerH = h * 0.5;
        gc.fillOval(x + w*0.25, y + h*0.1, towerW, towerH);

        // Borde sutil
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y + h*0.1, w, h*0.8, h*0.8, h*0.8);
    }

    // --- DESTRUCTOR (Tamaño 2) ---
    // Barco de guerra con dos cañones
    private void drawDestroyer(GraphicsContext gc, double x, double y, double w, double h) {
        // Gris plano mate (sin mucho brillo para el sigilo)
        gc.setFill(Color.rgb(120, 125, 130));

        // Forma poligonal facetada (estilo Zumwalt)
        double[] xPoints = { x, x + w*0.8, x + w, x + w*0.8, x };
        double[] yPoints = { y + h*0.2, y, y + h/2, y + h, y + h*0.8 };
        gc.fillPolygon(xPoints, yPoints, 5);
        gc.setStroke(Color.rgb(80, 85, 90));
        gc.strokePolygon(xPoints, yPoints, 5);

        // Estructura superior piramidal integrada
        gc.setFill(Color.rgb(100, 105, 110));
        double[] xSup = { x + w*0.4, x + w*0.7, x + w*0.6, x + w*0.5 };
        double[] ySup = { y + h*0.5, y + h*0.5, y + h*0.2, y + h*0.2 };
        gc.fillPolygon(xSup, ySup, 4);

        // Silos VLS (Cuadraditos blancos en la proa)
        gc.setFill(Color.WHITESMOKE);
        double vlsSize = h * 0.1;
        for(int i=0; i<3; i++) {
            for(int j=0; j<2; j++) {
                gc.fillRect(x + w*0.75 + (i*vlsSize*1.5), y + h*0.3 + (j*vlsSize*1.5), vlsSize, vlsSize);
            }
        }
    }

    // --- FRAGATA (Tamaño 1) ---
    // Barco pequeño simple
    private void drawFrigate(GraphicsContext gc, double x, double y, double w, double h) {
        // Verde Oliva Militar Drab
        Color oliveLight = Color.rgb(100, 120, 80);
        Color oliveDark = Color.rgb(60, 80, 50);
        gc.setFill(createMetalGradient(y, h, oliveLight, oliveDark));

        // Forma rectangular redondeada en proa
        gc.fillRoundRect(x, y, w*0.9, h, h*0.3, h*0.3);
        // Popa cuadrada
        gc.fillRect(x, y, w*0.2, h);

        gc.setStroke(Color.DARKOLIVEGREEN);
        gc.strokeRoundRect(x, y, w*0.9, h, h*0.3, h*0.3);

        // Cabina blindada cuadrada atrás
        gc.setFill(Color.rgb(50, 60, 40));
        gc.fillRect(x + w * 0.1, y + h * 0.2, w * 0.3, h * 0.6);

        // Pequeño cañón en proa (círculo simple)
        gc.setFill(Color.BLACK);
        gc.fillOval(x + w*0.7, y + h*0.4, h*0.2, h*0.2);
    }

    // --- UTILIDADES ---

    //  Efecto de volumen
    private LinearGradient createMetalGradient(double startY, double height, Color light, Color dark) {
        return new LinearGradient(
                0, startY, 0, startY + height,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, dark),
                new Stop(0.5, light), // Brillo en el medio
                new Stop(1, dark)
        );
    }*/
}