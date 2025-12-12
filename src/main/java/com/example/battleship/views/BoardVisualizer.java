
package com.example.battleship.views;

import com.example.battleship.models.Cell;
import com.example.battleship.models.CellState;
import com.example.battleship.models.Ship;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public class BoardVisualizer {

    private Pane shipsPane;
    private Pane enemyShipsPane;
    private double cellSize;

    private ShipRenderer shipRenderer;
    private final CanvasBombRenderer bombRenderer = new CanvasBombRenderer();
    private final CanvasSmokeRenderer smokeRenderer = new CanvasSmokeRenderer();

    //Variable interna para saber si mostrar barcos ocultos - en reemplazo de debug
    private boolean isDebugMode = false;


    // Visual feedback element for placing ships (Green/Red rectangle)
    private final Rectangle selectionHighlight = new Rectangle();

    // Visual feedback element for targeting enemy cells (Yellow rectangle)
    private final Rectangle enemySelectionHighlight = new Rectangle();

    public BoardVisualizer(Pane shipsPane, Pane enemyShipsPane, double cellSize) {
        this.shipsPane = shipsPane;
        this.enemyShipsPane = enemyShipsPane;
        this.cellSize = cellSize;
        this.shipRenderer = new CanvasShipRenderer();
    }

    // metodo para que el Controller nos avise si activar el modo debug ---
    public void setDebugMode(boolean enable) {
        this.isDebugMode = enable;
    }

    // --- AQUÍ PEGARÁS LOS MÉTODOS QUE TE DIGO EN EL PASO 2 ---

    /**
     * Draws the grid lines and initializes the selection highlight for the Player's board.
     */
    public void drawPlayerBoardGrid() {
        double boardSize = cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);

        gridCanvas.setId("Grid");

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();

        // Draw semi-transparent white lines
        gc.setStroke(Color.web("#FFFFFF", 0.3));
        gc.setLineWidth(1.0);
        for (int i = 0; i <= 10; i++) {
            double pos = i * cellSize;
            gc.strokeLine(pos, 0, pos, boardSize); // Vertical
            gc.strokeLine(0, pos, boardSize, pos); // Horizontal
        }

        // Add grid to the background
        if (!this.shipsPane.getChildren().isEmpty()) this.shipsPane.getChildren().add(0, gridCanvas);
        else this.shipsPane.getChildren().add(gridCanvas);

        // Initialize Player Highlight Rectangle
        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5);
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true);
        this.shipsPane.getChildren().add(selectionHighlight);
    }

    /**
     * Draws the grid lines and initializes the selection highlight for the Enemy's board.
     */
    public void drawEnemyBoardGrid() {
        if (this.enemyShipsPane == null) return;
        double boardSize = this.cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);
        gridCanvas.setId("Grid"); // ID used to prevent hiding this canvas when toggling debug mode

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#FFFFFF", 0.3));
        gc.setLineWidth(1.0);
        for (int i = 0; i <= 10; i++) {
            double pos = i * this.cellSize;
            gc.strokeLine(pos, 0, pos, boardSize);
            gc.strokeLine(0, pos, boardSize, pos);
        }

        if (!this.enemyShipsPane.getChildren().isEmpty()) this.enemyShipsPane.getChildren().add(0, gridCanvas);
        else this.enemyShipsPane.getChildren().add(gridCanvas);

        // --- ENEMY HIGHLIGHT CONFIGURATION ---
        enemySelectionHighlight.setWidth(cellSize);
        enemySelectionHighlight.setHeight(cellSize);
        enemySelectionHighlight.setFill(Color.rgb(255, 255, 0, 0.3)); // Semi-transparent yellow
        enemySelectionHighlight.setStroke(Color.YELLOW);
        enemySelectionHighlight.setStrokeWidth(2);
        enemySelectionHighlight.setVisible(false);
        enemySelectionHighlight.setMouseTransparent(true); // Must ignore clicks to allow pane underneath to catch them

        enemyShipsPane.getChildren().add(enemySelectionHighlight);
    }

    /**
     * Draw an 'X' or a blue circle if it was correct or not
     */
    public void drawShotResult(Pane pane, int col, int row, boolean hit) {
        Canvas shotCanvas = new Canvas(cellSize, cellSize);
        shotCanvas.setLayoutX(col * cellSize);
        shotCanvas.setLayoutY(row * cellSize);
        shotCanvas.setMouseTransparent(true);

        GraphicsContext gc = shotCanvas.getGraphicsContext2D();

        if (hit) {
            // --- TOCADO (BARCO) CON RENDERER---
            gc.setFill(Color.rgb(255, 0, 0, 0.3));
            gc.fillOval(2, 2, cellSize - 4, cellSize - 4);
            bombRenderer.render(gc, cellSize);
        } else {
            // --- AGUA (FALLO) ---
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(3);
            gc.strokeLine(10, 10, cellSize - 10, cellSize - 10);
            gc.strokeLine(cellSize - 10, 10, 10, cellSize - 10);
        }

        pane.getChildren().add(shotCanvas);
    }

    /**
     * Busca todas las celdas que pertenecen al barco hundido y les dibuja fuego.
     */
    public void markShipAsSunk(Pane pane, Cell[][] board, Ship sunkShip) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = board[i][j];

                // Si la celda contiene EXACTAMENTE el barco que se acaba de hundir
                if (cell.getOccupyingShip() == sunkShip) {
                    // Dibujamos fuego encima de la bomba que ya estaba ahí
                    drawFire(pane, i, j);
                }
            }
        }
    }

    /**
     * Método auxiliar para dibujar SOLO la parte visual de un barco (sin tocar la lógica).
     */
    private void placeEnemyShipVisualsOnly(int x, int y, int size, boolean horizontal) {
        Canvas enemyShipCanvas = new Canvas();
        enemyShipCanvas.setWidth(size * cellSize);
        enemyShipCanvas.setHeight(cellSize);
        enemyShipCanvas.setId("EnemyShip");

        shipRenderer.render(enemyShipCanvas, size);

        enemyShipCanvas.setLayoutX(x * cellSize);
        enemyShipCanvas.setLayoutY(y * cellSize);

        if (!horizontal) {
            // Rotación corregida (desde la esquina)
            enemyShipCanvas.getTransforms().add(new javafx.scene.transform.Rotate(90, cellSize / 2, cellSize / 2));
        }

        // Inicialmente oculto (respetando el checkbox)
        enemyShipCanvas.setVisible(this.isDebugMode);

        // Lo agregamos al fondo
        enemyShipsPane.getChildren().add(0, enemyShipCanvas);
    }

    /**
     * Reconstruye visualmente los barcos enemigos basándose en el tablero cargado.
     */
    public void restoreVisualShips(Cell[][] enemyBoardCells)
    {
        // 1. Borrar los barcos visuales aleatorios que se crearon al inicio
        // (Borramos todo nodo que tenga el ID "EnemyShip")
        enemyShipsPane.getChildren().removeIf(node -> "EnemyShip".equals(node.getId()));

        // 2. Usamos un Set para recordar qué barcos ya dibujamos (para no repetirlos)
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        // 3. Escanear el tablero lógico para encontrar los barcos guardados
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = enemyBoardCells[i][j];
                Ship ship = cell.getOccupyingShip();

                // Si hay un barco y NO lo hemos dibujado todavía...
                if (ship != null && !drawnShips.contains(ship)) {
                    // ...significa que estamos en la celda superior-izquierda de ese barco.

                    boolean isHorizontal = false;
                    // Verificamos si el barco sigue hacia la derecha para saber su orientación
                    if (i + 1 < 10 && enemyBoardCells[i + 1][j].getOccupyingShip() == ship) {
                        isHorizontal = true;
                    }

                    // Dibujamos el barco VISUALMENTE en esta posición
                    placeEnemyShipVisualsOnly(i, j, ship.getSize(), isHorizontal);

                    // Lo marcamos como "ya dibujado"
                    drawnShips.add(ship);
                }
            }
        }
    }

    /**
     * Dibuja la imagen de fuego en una celda específica.
     */
    private void drawFire(Pane pane, int col, int row) {
        Canvas smokeCanvas = new Canvas(cellSize, cellSize);
        smokeCanvas.setLayoutX(col * cellSize);
        smokeCanvas.setLayoutY(row * cellSize);
        smokeCanvas.setMouseTransparent(true);
        smokeRenderer.draw(smokeCanvas);
        pane.getChildren().add(smokeCanvas);

        /*Canvas fireCanvas = new Canvas(cellSize, cellSize);
        fireCanvas.setLayoutX(col * cellSize);
        fireCanvas.setLayoutY(row * cellSize);
        fireCanvas.setMouseTransparent(true);

        GraphicsContext gc = fireCanvas.getGraphicsContext2D();

        // Dibujamos el fuego un poco más grande para que se vea dramático
        if (fireImage != null) {
            gc.drawImage(fireImage, 2, 2, cellSize - 4, cellSize - 4);
        } else {
            // Fallback por si la imagen falla: Cuadrado naranja
            gc.setFill(Color.ORANGE);
            gc.fillOval(5, 5, cellSize - 10, cellSize - 10);
        }

        pane.getChildren().add(fireCanvas);*/
    }

    /**
     * redibuja la flota del jugador basándose en los datos lógicos
     *
     * 1. Recupera la partida guardada (Load Game).
     * 2. Asegura que los barcos no desaparezcan al iniciar.
     */
    public void drawPlayerShipsFromModel(Cell[][] boardCells) {
        // 1. Limpiamos cualquier rastro visual anterior para no duplicar
        // (Borramos solo los nodos que sean Canvas, preservando la rejilla si es otro tipo de nodo)
        shipsPane.getChildren().removeIf(node -> node instanceof Canvas && !"Grid".equals(node.getId()));

        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = boardCells[i][j];
                Ship ship = cell.getOccupyingShip();

                // Si hay barco y NO lo hemos dibujado aún
                if (ship != null && !drawnShips.contains(ship)) {

                    // Detectar orientación mirando la celda de la derecha
                    boolean isHorizontal = false;
                    // Si cabe a la derecha y la celda siguiente tiene EL MISMO barco -> es horizontal
                    if (i + 1 < 10 && boardCells[i + 1][j].getOccupyingShip() == ship) {
                        isHorizontal = true;
                    }
                    // Caso especial: Fragata (Tamaño 1). Asumimos horizontal por defecto o lógica propia.
                    if (ship.getSize() == 1) isHorizontal = true;

                    // --- LÓGICA DE DIBUJADO (Copiada del Manager) ---
                    Canvas newShipCanvas = new Canvas();
                    newShipCanvas.setWidth(ship.getSize() * cellSize);
                    newShipCanvas.setHeight(cellSize);

                    // Usamos tu nuevo renderer pro
                    shipRenderer.render(newShipCanvas, ship.getSize());

                    // Posicionamiento y Rotación
                    if (isHorizontal) {
                        newShipCanvas.setLayoutX(i * cellSize);
                        newShipCanvas.setLayoutY(j * cellSize);
                    } else {
                        // Lógica matemática para rotar sobre el centro correcto
                        newShipCanvas.setRotate(90);
                        double offset = cellSize * (1 - ship.getSize()) / 2.0;
                        newShipCanvas.setLayoutX((i * cellSize) + offset);
                        newShipCanvas.setLayoutY((j * cellSize) - offset);
                    }

                    newShipCanvas.setMouseTransparent(true); // Para que los clicks pasen al tablero
                    shipsPane.getChildren().add(newShipCanvas);

                    drawnShips.add(ship);
                }
            }
        }
    }

    public Rectangle getSelectionHighlight() {
        return selectionHighlight;
    }

    public Rectangle getEnemySelectionHighlight() {
        return enemySelectionHighlight;
    }
    
}