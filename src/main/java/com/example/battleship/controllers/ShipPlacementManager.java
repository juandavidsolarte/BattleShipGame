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
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class ShipPlacementManager {

    private final GameController controller;
    private final BoardVisualizer visualizer;
    private final Pane shipsPane;
    private final double cellSize;

    // Estado interno de la colocación
    private boolean isHorizontal = true;
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    public ShipPlacementManager(GameController controller, BoardVisualizer visualizer, Pane shipsPane, double cellSize) {
        this.controller = controller;
        this.visualizer = visualizer;
        this.shipsPane = shipsPane;
        this.cellSize = cellSize;
    }

    // --- MÉTODOS PÚBLICOS DE CONFIGURACIÓN ---

    /**
     * Sets up Drag & Drop handlers for the Player's board.
     * Handles preview (highlight) and placement (drop).
     */
    public void setupBoardDragHandlers() {
        // Drag Over: Update highlight position
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
                } catch (NumberFormatException e) {}
            }
            event.consume();
        });

        // Drag Dropped: Attempt to place ship
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

        // Drag Exited: Hide highlight
        shipsPane.setOnDragExited(event -> {
            visualizer.getSelectionHighlight().setVisible(false);
            event.consume();
        });

        // Click: Rotate orientation (Right Click)
        shipsPane.setOnMouseClicked(event -> {
            if (!controller.isGameStarted() && event.getButton() == MouseButton.SECONDARY) {
                isHorizontal = !isHorizontal;
                System.out.println("Orientación: " + (isHorizontal ? "Horizontal" : "Vertical"));
            }
        });
    }

    /**
     * Generic method to make a Canvas draggable.
     */
    public void makeDraggable(Canvas sourceCanvas, int size) {
        sourceCanvas.setOnDragDetected(event -> {
            if (controller.isGameStarted()) return;
            Dragboard db = sourceCanvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(size));
            db.setContent(content);
            WritableImage snapshot = sourceCanvas.snapshot(null, null);
            db.setDragView(snapshot);
            event.consume();
        });
        sourceCanvas.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
                sourceCanvas.setVisible(false);
                sourceCanvas.setDisable(true);
            }
            event.consume();
        });
    }

    // --- MÉTODOS PRIVADOS DE LÓGICA ---

    /**
     * Updates the position and color of the placement highlight rectangle.
     */
    private void updateHighlight(int col, int row, int size) {

        Rectangle highlight = visualizer.getSelectionHighlight();

        if (col < 0 || row < 0 || col >= 10 || row >= 10) {
            highlight.setVisible(false);
            return;
        }
        if (isHorizontal) {
            highlight.setWidth(size * cellSize);
            highlight.setHeight(cellSize);
        } else {
            highlight.setWidth(cellSize);
            highlight.setHeight(size * cellSize);
        }
        highlight.setLayoutX(col * cellSize);
        highlight.setLayoutY(row * cellSize);

        boolean valid = isValidPlacement(col, row, size, isHorizontal);
        if (valid) highlight.setFill(Color.rgb(0, 255, 0, 0.4)); // Green
        else highlight.setFill(Color.rgb(255, 0, 0, 0.4)); // Red

        highlight.setVisible(true);
        highlight.toFront();
    }

    /**
     * Validates if a player ship can be placed at the given coordinates.
     */
    private boolean isValidPlacement(int x, int y, int size, boolean horizontal) {
        if (horizontal && x + size > 10) return false;
        if (!horizontal && y + size > 10) return false;
        if (x < 0 || y < 0 || x >= 10 || y >= 10) return false;

        // Pedimos el tablero al controlador
        Cell[][] board = controller.getBoardCells();

        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            if (board[targetX][targetY].getOccupyingShip() != null) return false;
        }
        return true;
    }

    /**
     * Places a player ship on the board (Model + View).
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

        // Update Model
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            board[targetX][targetY].setOccupyingShip(newShip);
        }

        // Update View
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

        // Update Game State - (Esto reemplaza la lógica de shipsPlacedCount y playButton)
        controller.notifyShipPlaced();
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
}
