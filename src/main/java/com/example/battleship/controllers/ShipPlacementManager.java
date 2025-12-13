package com.example.battleship.controllers;

import com.example.battleship.models.Cell;
import com.example.battleship.models.Ship;
import com.example.battleship.views.BoardVisualizer;
import com.example.battleship.views.CanvasShipRenderer;
import com.example.battleship.views.ShipRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Configure the event handlers (DragOver, DragDropped, DragExited)
 * in the dashboard panel to enable drop-in/drop-out.
 */
public class ShipPlacementManager
{

    //region 1. Variables and constants
    // --- Constants ---
    private final GameController controller;
    private final BoardVisualizer visualizer;
    private final Pane shipsPane;
    private final double cellSize;
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    // Internal state of the placement
    private boolean isHorizontal = true;

    //endregion

    //region 2. Constructors and initializers
    public ShipPlacementManager(GameController controller, BoardVisualizer visualizer, Pane shipsPane, double cellSize) {
        this.controller = controller;
        this.visualizer = visualizer;
        this.shipsPane = shipsPane;
        this.cellSize = cellSize;
    }

    //endregion

    //region 3. Event Config

    /**
     * Configures drag and drop functionality for ship placement on the player's board.
     * We handle three key interactions: highlighting potential placements during drag,
     * validating and executing ship drops, and cleaning up visuals when dragging stops.
     */
    public void setupBoardDragHandlers() {
        // Drag Over: Show placement preview as player moves shi
        shipsPane.setOnDragOver(event -> {
            if (!controller.isGameStarted() && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            if (!controller.isGameStarted() && event.getDragboard().hasString()) {
                try {
                    int shipSize = Integer.parseInt(event.getDragboard().getString());
                    int col = (int) (event.getX() / cellSize);
                    int row = (int) (event.getY() / cellSize);
                    updateHighlight(col, row, shipSize);
                } catch (NumberFormatException e) {
                    // Silently ignore invalid drag data}
                }
            }
            event.consume();
        });

        // Drag Dropped: Validate and place the ship
        shipsPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (!controller.isGameStarted() && db.hasString()) {
                try {
                    int shipSize = Integer.parseInt(db.getString());
                    int col = (int) (event.getX() / cellSize);
                    int row = (int) (event.getY() / cellSize);
                    if (isValidPlacement(col, row, shipSize, isHorizontal)) {
                        placeShipOnBoard(col, row, shipSize, isHorizontal);
                        success = true;
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            visualizer.getSelectionHighlight().setVisible(false);
            event.setDropCompleted(success);
            event.consume();
        });

        // Drag Exited: Clear visual preview when leaving board
        shipsPane.setOnDragExited(event -> {
            visualizer.getSelectionHighlight().setVisible(false);
            event.consume();
        });
    }
    //endregion

    //region 4. Control Logic

    /**
     * Toggles ship orientation between horizontal and vertical placement.
     * We update the orientation state and hide the current preview highlight,
     * which will automatically refresh when the player moves their mouse.
     * Called by GameController when the player presses the 'R' key.
     */
    public void toggleOrientation() {
        this.isHorizontal = !this.isHorizontal;
        System.out.println("Orientación cambiada a: " + (isHorizontal ? "Horizontal" : "Vertical"));

        // Hide current preview - it will update automatically on next mouse movement
        visualizer.getSelectionHighlight().setVisible(false);
    }

    /**
     * Generic method to make a Canvas draggable.
     * Makes a Canvas element draggable for ship placement.
     * We configure both drag initiation (with visual preview) and cleanup
     * after successful placement, giving players visual feedback throughout.
     */
    public void makeDraggable(Canvas sourceCanvas, int size) {
        // Handle drag initiation
        sourceCanvas.setOnDragDetected(event -> {
            if (controller.isGameStarted()) return;

            Dragboard db = sourceCanvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(size));
            db.setContent(content);

            // Use the actual canvas as drag preview
            WritableImage snapshot = sourceCanvas.snapshot(null, null);
            db.setDragView(snapshot);

            event.consume();
        });
        // Handle drag completion
        sourceCanvas.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
                // Hide ship after successful placement
                sourceCanvas.setVisible(false);
                sourceCanvas.setDisable(true);
            }
            event.consume();
        });
    }

    //endregion

    //region 5. Placement Logic

    /**
     * Places a player ship on the board, updating both the game model and visual display.
     * We handle ship creation, board position updates, and visual rendering,
     * then notify the controller that placement is complete.
     */
    private void placeShipOnBoard(int x, int y, int size, boolean horizontal) {
        String name = "";
        switch (size) {
            case 4: name = "Portaaviones"; break;
            case 3: name = "Submarino"; break;
            case 2: name = "Destructor"; break;
            case 1: name = "Fragata"; break;
        }
        Ship newShip = new Ship(size, name);

        Cell[][] board = controller.getBoardCells();

        // Update game model with ship placement
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            board[targetX][targetY].setOccupyingShip(newShip);
        }

        // Create and position visual representation
        Canvas newShipCanvas = new Canvas();
        newShipCanvas.setWidth(size * cellSize);
        newShipCanvas.setHeight(cellSize);
        shipRenderer.render(newShipCanvas, size);

        if (horizontal) {
            newShipCanvas.setLayoutX(x * cellSize);
            newShipCanvas.setLayoutY(y * cellSize);
        } else {
            newShipCanvas.setRotate(90);
            double offset = cellSize * (1 - size) / 2.0;
            newShipCanvas.setLayoutX((x * cellSize) + offset);
            newShipCanvas.setLayoutY((y * cellSize) - offset);
        }
        newShipCanvas.setMouseTransparent(true);
        shipsPane.getChildren().add(newShipCanvas);

        // Update game state and UI
        controller.notifyShipPlaced();
    }
    //endregion

    //region 6. Validations

    /**
     * Validates if a player ship can be placed at the given coordinates.
     */
    private boolean isValidPlacement(int x, int y, int size, boolean horizontal) {
        if (horizontal && x + size > 10) return false;
        if (!horizontal && y + size > 10) return false;
        if (x < 0 || y < 0 || x >= 10 || y >= 10) return false;

        // We asked the controller for the board.
        Cell[][] board = controller.getBoardCells();
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            if (board[targetX][targetY].getOccupyingShip() != null) return false;
        }
        return true;
    }
    //endregion

    //region 7. Visual Aids

    /**
     * Updates the visual highlight that shows potential ship placement.
     * We adjust the rectangle's position, size, and color based on whether
     * the current position is valid, giving players immediate placement feedback.
     */
    private void updateHighlight(int col, int row, int size) {
        Rectangle highlight = visualizer.getSelectionHighlight();

        // Hide if position is outside board
        if (col < 0 || row < 0 || col >= 10 || row >= 10) {
            highlight.setVisible(false);
            return;
        }

        // Set size based on orientation
        if (isHorizontal) {
            highlight.setWidth(size * cellSize);
            highlight.setHeight(cellSize);
        } else {
            highlight.setWidth(cellSize);
            highlight.setHeight(size * cellSize);
        }

        // Position highlight at current coordinates
        highlight.setLayoutX(col * cellSize);
        highlight.setLayoutY(row * cellSize);

        // Color indicates placement validity
        boolean valid = isValidPlacement(col, row, size, isHorizontal);
        if (valid) highlight.setFill(Color.rgb(0, 255, 0, 0.4)); // Green
        else highlight.setFill(Color.rgb(255, 0, 0, 0.4)); // Red
        highlight.setVisible(true);
        highlight.toFront();
    }

    private String getShipName(int size) {
        switch (size) {
            case 4: return "Portaaviones";
            case 3: return "Submarino";
            case 2: return "Destructor";
            case 1: return "Fragata";
            default: return "Barco";
        }
    }
    //endregion
}
