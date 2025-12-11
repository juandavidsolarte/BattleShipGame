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

         */
    }
    private void drawCarrier(GraphicsContext gc, double x, double y, double w, double h) {
        // Casco: "Haze Gray"
        Color lightGrey = Color.rgb(200, 200, 210);
        Color shadowGrey = Color.rgb(140, 140, 150);
        gc.setFill(createMetalGradient(y, h, lightGrey, shadowGrey));

        // Forma
        double bow = w * 0.1;
        double[] xPoints = { x, x + w - bow, x + w, x + w - bow, x };
        double[] yPoints = { y, y, y + h / 2, y + h, y + h };
        gc.fillPolygon(xPoints, yPoints, 5);

        // Borde fuerte para claridad
        gc.setStroke(Color.rgb(50, 50, 60));
        gc.setLineWidth(1.5);
        gc.strokePolygon(xPoints, yPoints, 5);

        // Pista de aterrizaje
        gc.setStroke(Color.WHITESMOKE);
        gc.setLineWidth(3);
        gc.setLineDashes(15, 8); // Líneas discontinuas largas
        gc.strokeLine(x + 15, y + h / 2, x + w - 20, y + h / 2);
        gc.setLineDashes(null);

        // Torre de control
        gc.setFill(Color.rgb(100, 100, 110));
        gc.fillRect(x + w * 0.65, y + h * 0.1, w * 0.12, h * 0.3);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x + w * 0.65, y + h * 0.1, w * 0.12, h * 0.3);
    }

    // --- SUBMARINO (Tamaño 3) ---
    // Forma ovalada/cigarro
    private void drawSubmarine(GraphicsContext gc, double x, double y, double w, double h) {
        // Degradado Azul Metalico
        Color lightBlue = Color.rgb(120, 140, 170); // Azul acero claro
        Color darkBlue = Color.rgb(60, 70, 90);     // Azul oscuro
        gc.setFill(createMetalGradient(y, h, lightBlue, darkBlue));

        // Cuerpo ovalado
        gc.fillRoundRect(x, y, w, h, h, h);
        gc.setStroke(Color.rgb(30, 40, 50));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, w, h, h, h);

        // Torreta central
        double towerW = w * 0.12;
        double towerH = h * 0.5;
        gc.setFill(Color.rgb(50, 60, 70)); // Gris oscuro para la torre
        gc.fillOval(x + (w/2) - (towerW/2), y + (h/2) - (towerH/2), towerW, towerH);
        gc.setStroke(Color.rgb(200, 180, 50));
        gc.setLineWidth(1);
        gc.strokeLine(x + 10, y + h/2, x + w - 10, y + h/2);
    }

    // --- DESTRUCTOR (Tamaño 2) ---
    // Barco de guerra con dos cañones
    private void drawDestroyer(GraphicsContext gc, double x, double y, double w, double h) {
        // Base Gris Medio
        gc.setFill(createMetalGradient(y, h, Color.rgb(180, 180, 180), Color.rgb(130, 130, 135)));

        // Forma afilada
        double bow = w * 0.25;
        double[] xPoints = { x, x + w - bow, x + w, x + w - bow, x };
        double[] yPoints = { y, y, y + h / 2, y + h, y + h };
        gc.fillPolygon(xPoints, yPoints, 5);
        gc.setStroke(Color.rgb(40, 40, 40));
        gc.setLineWidth(1.5);
        gc.strokePolygon(xPoints, yPoints, 5);

        double turretSize = h * 0.55;

        // Dos atras
        drawTurret(gc, x + w * 0.15, y + h/2, turretSize);
        drawTurret(gc, x + w * 0.35, y + h/2, turretSize);

        // Dos adelante
        drawTurret(gc, x + w * 0.65, y + h/2, turretSize);
        drawTurret(gc, x + w * 0.82, y + h/2, turretSize);

        // Puente de mando en el centro
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x + w * 0.48, y + h * 0.25, w * 0.1, h * 0.5);
    }

    // --- FRAGATA (Tamaño 1) ---
    // Barco pequeño simple
    private void drawFrigate(GraphicsContext gc, double x, double y, double w, double h) {
        // Casco casi blanco/plateado
        gc.setFill(createMetalGradient(y, h, Color.WHITE, Color.LIGHTGRAY));

        // Forma triangular agresiva
        double bow = w * 0.4;
        double[] xPoints = { x, x + w - bow, x + w, x + w - bow, x };
        double[] yPoints = { y, y, y + h / 2, y + h, y + h };
        gc.fillPolygon(xPoints, yPoints, 5);
        gc.setStroke(Color.DARKSLATEGRAY);
        gc.strokePolygon(xPoints, yPoints, 5);

        // Cabina con "vidrio" azul
        gc.setFill(Color.rgb(100, 200, 255)); // Cristal
        gc.fillRect(x + w * 0.2, y + h * 0.25, w * 0.25, h * 0.5);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x + w * 0.2, y + h * 0.25, w * 0.25, h * 0.5);
    }

    // --- UTILIDADES ---

    // Dibuja un círculo con un "cañón" saliendo
    private void drawTurret(GraphicsContext gc, double cx, double cy, double size) {
        // Base del cañon
        gc.setFill(Color.rgb(80, 80, 80));
        gc.fillOval(cx - size/2, cy - size/2, size, size);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(cx - size/2, cy - size/2, size, size);

        // Tubo del cañon
        gc.setLineWidth(3);
        gc.setStroke(Color.rgb(40, 40, 40));
        gc.strokeLine(cx, cy, cx + size * 0.9, cy); // Apunta a la derecha
    }

    //  Efecto de volumen
    private LinearGradient createMetalGradient(double startY, double height, Color light, Color dark) {
        return new LinearGradient(
                0, startY, 0, startY + height,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, dark),
                new Stop(0.5, light), // Brillo en el medio
                new Stop(1, dark)
        );
    }
}