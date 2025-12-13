
package com.example.battleship.views;

import com.example.battleship.models.Cell;
import com.example.battleship.models.Ship;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardVisualizer {

    //region 1. Variables and Constants
    private final CanvasBombRenderer bombRenderer = new CanvasBombRenderer();
    private final CanvasSmokeRenderer smokeRenderer = new CanvasSmokeRenderer();
    // Visual feedback element for placing ships (Green/Red rectangle)
    private final Rectangle selectionHighlight = new Rectangle();

    // Visual feedback element for targeting enemy cells (Yellow rectangle)
    private final Rectangle enemySelectionHighlight = new Rectangle();

    private Pane shipsPane;
    private Pane enemyShipsPane;
    private double cellSize;
    private ShipRenderer shipRenderer;

    //Variable interna para saber si mostrar barcos ocultos - en reemplazo de debug
    private boolean isDebugMode = false;
    //endregion

    //region 2. Constructor and Initialization
    public BoardVisualizer(Pane shipsPane, Pane enemyShipsPane, double cellSize) {
        this.shipsPane = shipsPane;
        this.enemyShipsPane = enemyShipsPane;
        this.cellSize = cellSize;
        this.shipRenderer = new CanvasShipRenderer();
    }

    public void setDebugMode(boolean enable) {
        this.isDebugMode = enable;
    }
    //endregion

    //region 3. Boards Render

    /**
     * Draws the grid lines and initializes the selection highlight for the player's board.
     * We create a semi-transparent grid overlay and prepare the visual feedback
     * element that shows valid ship placement positions.
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

        // Initialize selection highlight for placement feedback
        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5);
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true);
        this.shipsPane.getChildren().add(selectionHighlight);
    }

    /**
     * Draws the grid lines and initializes the targeting highlight for the enemy's board.
     * We create a semi-transparent grid and a yellow highlight that shows which cell
     * the player is currently targeting for their next attack.
     */
    public void drawEnemyBoardGrid() {
        if (this.enemyShipsPane == null) return;

        double boardSize = this.cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);
        gridCanvas.setId("Grid"); // ID used to prevent hiding this canvas when toggling debug mode

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#FFFFFF", 0.3));
        gc.setLineWidth(1.0);

        // Draw grid line
        for (int i = 0; i <= 10; i++) {
            double pos = i * this.cellSize;
            gc.strokeLine(pos, 0, pos, boardSize);
            gc.strokeLine(0, pos, boardSize, pos);
        }

        // Add grid to background layer
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
     * Reconstructs enemy ship visuals based on a loaded game board.
     * We scan the logical board state and recreate the visual ship representations,
     * ensuring the display matches exactly what was saved in the game file.
     */
    public void restoreVisualShips(Cell[][] enemyBoardCells)
    {
        // Remove any existing enemy ship visuals from previous setup
        enemyShipsPane.getChildren().removeIf(node -> "EnemyShip".equals(node.getId()));

        // Track already-drawn ships to avoid duplicates
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        // Scan the logical board to locate saved ships
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = enemyBoardCells[i][j];
                Ship ship = cell.getOccupyingShip();

                // If we find an undrawn ship, this is its top-left cell
                if (ship != null && !drawnShips.contains(ship)) {
                    // Determine orientation by checking adjacent cells
                    boolean isHorizontal = false;
                    if (i + 1 < 10 && enemyBoardCells[i + 1][j].getOccupyingShip() == ship) {
                        isHorizontal = true;
                    }

                    // Create visual representation at this position
                    placeEnemyShipVisualsOnly(i, j, ship.getSize(), isHorizontal);

                    // Mark as drawn to prevent duplicates
                    drawnShips.add(ship);
                }
            }
        }
    }

    /**
     * Creates only the visual representation of an enemy ship without modifying game logic.
     * We use this helper when reconstructing the board from a saved state, ensuring
     * visual elements match the existing logical ship positions.
     */
    private void placeEnemyShipVisualsOnly(int x, int y, int size, boolean horizontal) {
        Canvas enemyShipCanvas = new Canvas();
        enemyShipCanvas.setWidth(size * cellSize);
        enemyShipCanvas.setHeight(cellSize);
        enemyShipCanvas.setId("EnemyShip");

        shipRenderer.render(enemyShipCanvas, size);

        // Position the canvas
        enemyShipCanvas.setLayoutX(x * cellSize);
        enemyShipCanvas.setLayoutY(y * cellSize);

        if (!horizontal) {
            // Apply corrected rotation from the corner
            enemyShipCanvas.getTransforms().add(new javafx.scene.transform.Rotate(90, cellSize / 2, cellSize / 2));
        }

        // Respect debug mode visibility setting
        enemyShipCanvas.setVisible(this.isDebugMode);

        // Add to background layer
        enemyShipsPane.getChildren().add(0, enemyShipCanvas);
    }
    //endregion

    //region 4. Ship Render
    /**
     * Reconstructs the player's fleet visuals from the logical board state.
     * We use this method when loading a saved game to ensure visual ships
     * match their logical positions, preventing ships from disappearing after load.
     */
    public void drawPlayerShipsFromModel(Cell[][] boardCells) {
        // Clear previous ship visuals while preserving the grid
        shipsPane.getChildren().removeIf(node -> node instanceof Canvas && !"Grid".equals(node.getId()));

        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = boardCells[i][j];
                Ship ship = cell.getOccupyingShip();

                // If we find an undrawn ship, this is its starting cell
                if (ship != null && !drawnShips.contains(ship)) {
                    // Detect orientation by checking adjacent cells
                    boolean isHorizontal = false;
                    // Si cabe a la derecha y la celda siguiente tiene EL MISMO barco -> es horizontal
                    if (i + 1 < 10 && boardCells[i + 1][j].getOccupyingShip() == ship) {
                        isHorizontal = true;
                    }
                    // Special case: Frigates (size 1) default to horizontal
                    if (ship.getSize() == 1) isHorizontal = true;

                    // Create visual ship representation
                    Canvas newShipCanvas = new Canvas();
                    newShipCanvas.setWidth(ship.getSize() * cellSize);
                    newShipCanvas.setHeight(cellSize);

                    // Use the ship renderer for consistent visuals
                    shipRenderer.render(newShipCanvas, ship.getSize());

                    // Position and rotate based on orientation
                    if (isHorizontal) {
                        newShipCanvas.setLayoutX(i * cellSize);
                        newShipCanvas.setLayoutY(j * cellSize);
                    } else {
                        // Apply rotation with mathematical correction
                        newShipCanvas.setRotate(90);
                        double offset = cellSize * (1 - ship.getSize()) / 2.0;
                        newShipCanvas.setLayoutX((i * cellSize) + offset);
                        newShipCanvas.setLayoutY((j * cellSize) - offset);
                    }

                    newShipCanvas.setMouseTransparent(true);
                    shipsPane.getChildren().add(newShipCanvas);
                    drawnShips.add(ship);
                }
            }
        }
    }
    //endregion

    //region 5. Effects Renderer
    /**
     * Draws visual feedback for a shot result on the game board.
     * We display either a hit marker (red circle with bomb graphic) or
     * a miss marker (gray X) to give players clear feedback on their attacks.
     */
    public void drawShotResult(Pane pane, int col, int row, boolean hit) {
        Canvas shotCanvas = new Canvas(cellSize, cellSize);
        shotCanvas.setLayoutX(col * cellSize);
        shotCanvas.setLayoutY(row * cellSize);
        shotCanvas.setMouseTransparent(true);

        GraphicsContext gc = shotCanvas.getGraphicsContext2D();

        if (hit) {
            // Hit marker: red circle with bomb graphic
            gc.setFill(Color.rgb(255, 0, 0, 0.3));
            gc.fillOval(2, 2, cellSize - 4, cellSize - 4);
            bombRenderer.render(gc, cellSize);
        } else {
            // Miss marker: gray X
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(3);
            gc.strokeLine(10, 10, cellSize - 10, cellSize - 10);
            gc.strokeLine(cellSize - 10, 10, 10, cellSize - 10);
        }
        pane.getChildren().add(shotCanvas);
    }

    /**
     * Highlights all cells belonging to a sunk ship with fire effects.
     * We scan the entire board to find every cell occupied by the destroyed ship
     * and overlay fire visuals to emphasize the ship's destruction.
     */
    public void markShipAsSunk(Pane pane, Cell[][] board, Ship sunkShip) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = board[i][j];

                // Check if this cell contains the recently sunk ship
                if (cell.getOccupyingShip() == sunkShip) {
                    // Overlay fire effect on top of existing hit markers
                    drawFire(pane, i, j);
                }
            }
        }
    }

    /**
     * Draw the fire image in a specific cell.
     */
    private void drawFire(Pane pane, int col, int row) {
        Canvas smokeCanvas = new Canvas(cellSize, cellSize);
        smokeCanvas.setLayoutX(col * cellSize);
        smokeCanvas.setLayoutY(row * cellSize);
        smokeCanvas.setMouseTransparent(true);
        smokeRenderer.draw(smokeCanvas);
        pane.getChildren().add(smokeCanvas);
    }
    //endregion

    //region 5. Getters

    public Rectangle getSelectionHighlight() {
        return selectionHighlight;
    }

    public Rectangle getEnemySelectionHighlight() {
        return enemySelectionHighlight;
    }
    //endregion
}