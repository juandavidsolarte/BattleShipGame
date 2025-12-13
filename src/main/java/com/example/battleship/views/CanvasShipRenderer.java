package com.example.battleship.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.FontWeight;

/**
 *This class belongs to the view because
 *  it handles pixels and renders ships in two ways,
 *  the first (the way we did it, the simplest way) and
 *  the second, a way supported by AI assistance.
 */
public class CanvasShipRenderer implements ShipRenderer {
    //region 1. First Model

    // Color scheme for the original ship design
    private final Color shipColor = Color.DARKGRAY;
    private final Color deckColor = Color.SLATEGRAY;

    @Override
    public void render(Canvas canvas, int size) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear any existing content
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Define dimensions and margins
        double padding = 8;
        double width = canvas.getWidth() - 2 * padding;
        double height = canvas.getHeight() - 2 * padding;
        double startX = padding;
        double startY = padding;
        double cellWidth = width / size;

        // --- DRAW HULL (Base shape) ---
        gc.setFill(shipColor);
        gc.setStroke(deckColor);
        gc.setLineWidth(2);

        // Calculate bow (front) length
        double bowLength = Math.min(cellWidth, height * 0.8);

       // Define polygon coordinates for ship shape
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

        // Draw solid hull with outline
        gc.fillPolygon(xPoints, yPoints, nPoints);
        gc.strokePolygon(xPoints, yPoints, nPoints);

        // DRAW DIVISION LINES (Segmentation detail)
        gc.setStroke(deckColor);
        gc.setLineWidth(1);

        for (int i = 1; i < size; i++) {
            double lineX = startX + i * cellWidth;

            // Calculate line endpoints with bow taper
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
    //endregion
    //region 2. Second Model with AI assistance
    /*
    @Override
    public void render(Canvas canvas, int size) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Clear previous content
        gc.clearRect(0, 0,w, h);

        // Calculate dimensions with proportional margins
        double padding = h * 0.15; //8
        double shipH = h - (padding * 2);
        double startX = padding;
        double startY = padding;
        double shipW = w - (padding * 2);

        // Select ship type based on size
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
    }

    private void drawCarrier(GraphicsContext gc, double x, double y, double w, double h) {
        // Hull with metallic gradient
        gc.setFill(createMetalGradient(y, h, Color.rgb(150, 150, 160), Color.rgb(100, 100, 110)));
        double[] xBase = { x, x + w * 0.95, x + w, x + w * 0.95, x };
        double[] yBase = { y, y, y + h / 2, y + h, y + h };
        gc.fillPolygon(xBase, yBase, 5);
        gc.setStroke(Color.DARKSLATEGRAY);
        gc.strokePolygon(xBase, yBase, 5);

        // Flight deck (dark asphalt-like surface)
        gc.setFill(Color.rgb(50, 50, 55));
        double deckMargin = h * 0.1;
        double[] xDeck = { x + deckMargin, x + w * 0.9, x + w * 0.95, x + w * 0.9, x + deckMargin };
        double[] yDeck = { y + deckMargin, y + deckMargin, y + h/2, y + h - deckMargin, y + h - deckMargin };
        gc.fillPolygon(xDeck, yDeck, 5);

        // Flight line marking
        gc.setStroke(Color.WHITESMOKE);
        gc.setLineWidth(2);
        gc.strokeLine(x + w * 0.1, y + h * 0.6, x + w * 0.85, y + h * 0.6);

        // Island structure (control tower)
        gc.setFill(Color.rgb(90, 90, 100));
        gc.fillRect(x + w * 0.7, y + h * 0.1, w * 0.15, h * 0.35);

        // Radar dome
        gc.setFill(Color.DARKGRAY);
        gc.fillOval(x + w * 0.75, y + h * 0.15, h*0.15, h*0.15);
    }
    // SUBMARINE
    private void drawSubmarine(GraphicsContext gc, double x, double y, double w, double h) {
        Color darkHull = Color.rgb(40, 40, 45);
        Color darkerHull = Color.rgb(20, 20, 25);
        gc.setFill(createMetalGradient(y, h, darkHull, darkerHull));

        // Main cylindrical hull
        gc.fillRoundRect(x, y + h*0.1, w, h*0.8, h*0.8, h*0.8);

        // Missile hump (behind tower)
        gc.fillRoundRect(x + w*0.4, y, w*0.4, h*0.4, h*0.2, h*0.2);

        // Conning tower
        double towerW = w * 0.12;
        double towerH = h * 0.5;
        gc.fillOval(x + w*0.25, y + h*0.1, towerW, towerH);

        // Subtle hull outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y + h*0.1, w, h*0.8, h*0.8, h*0.8);
    }

    // DESTROYER
    private void drawDestroyer(GraphicsContext gc, double x, double y, double w, double h) {
        // Matte gray for stealth appearance
        gc.setFill(Color.rgb(120, 125, 130));

        // Faceted polygonal shape (Zumwalt-style)
        double[] xPoints = { x, x + w*0.8, x + w, x + w*0.8, x };
        double[] yPoints = { y + h*0.2, y, y + h/2, y + h, y + h*0.8 };
        gc.fillPolygon(xPoints, yPoints, 5);
        gc.setStroke(Color.rgb(80, 85, 90));
        gc.strokePolygon(xPoints, yPoints, 5);

        // Integrated superstructure
        gc.setFill(Color.rgb(100, 105, 110));
        double[] xSup = { x + w*0.4, x + w*0.7, x + w*0.6, x + w*0.5 };
        double[] ySup = { y + h*0.5, y + h*0.5, y + h*0.2, y + h*0.2 };
        gc.fillPolygon(xSup, ySup, 4);

        // VLS missile cells (white squares on bow)
        gc.setFill(Color.WHITESMOKE);
        double vlsSize = h * 0.1;
        for(int i=0; i<3; i++) {
            for(int j=0; j<2; j++) {
                gc.fillRect(x + w*0.75 + (i*vlsSize*1.5), y + h*0.3 + (j*vlsSize*1.5), vlsSize, vlsSize);
            }
        }
    }

    // FRIGATE
    private void drawFrigate(GraphicsContext gc, double x, double y, double w, double h) {
        // Military olive drab color scheme
        Color oliveLight = Color.rgb(100, 120, 80);
        Color oliveDark = Color.rgb(60, 80, 50);
        gc.setFill(createMetalGradient(y, h, oliveLight, oliveDark));

        // Rounded bow with rectangular stern
        gc.fillRoundRect(x, y, w*0.9, h, h*0.3, h*0.3);
        gc.fillRect(x, y, w*0.2, h);
        gc.setStroke(Color.DARKOLIVEGREEN);
        gc.strokeRoundRect(x, y, w*0.9, h, h*0.3, h*0.3);

        // Armored wheelhouse
        gc.setFill(Color.rgb(50, 60, 40));
        gc.fillRect(x + w * 0.1, y + h * 0.2, w * 0.3, h * 0.6);

        // Small forward gun
        gc.setFill(Color.BLACK);
        gc.fillOval(x + w*0.7, y + h*0.4, h*0.2, h*0.2);
    }*/

    // --- UTILITY METHODS ---

    /**
     * Creates a metallic gradient for 3D volume effect.
     * We use a vertical gradient with darker edges and a lighter center
     * to simulate curved metal surfaces.
     */
    /*
    private LinearGradient createMetalGradient(double startY, double height, Color light, Color dark) {
        return new LinearGradient(
                0, startY, 0, startY + height,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, dark),
                new Stop(0.5, light),
                new Stop(1, dark)
        );
    }*/
    //endregion
}