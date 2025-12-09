package com.example.battleship.controllers;

import com.example.battleship.models.Cell;
import com.example.battleship.models.Ship;
import com.example.battleship.views.CanvasShipRenderer;
import com.example.battleship.views.ShipRenderer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Main Controller for the Battleship Game.
 * Handles user interaction, game logic, board rendering, and ship placement.
 */
public class GameController implements Initializable {

    // --- FXML Layout Elements ---
    @FXML private Pane shipsPane;       // Pane for player's ships and interaction
    @FXML private Pane enemyShipsPane;  // Pane for enemy's ships (visuals/debug)
    @FXML private GridPane playerBoard; // Background grid for player
    @FXML private GridPane enemyBoard;  // Background grid for enemy
    @FXML private CheckBox debugCheckBox; // Debug mode toggle
    @FXML private Button playButton;    // Button to start the game

    // --- Ship Canvases (Draggable Sources) ---
    @FXML private Canvas carrierCanvas;
    @FXML private Canvas submarineCanvas1, submarineCanvas2;
    @FXML private Canvas destroyerCanvas1, destroyerCanvas2, destroyerCanvas3;
    @FXML private Canvas frigateCanvas1, frigateCanvas2, frigateCanvas3, frigateCanvas4;

    // --- UI Labels ---
    @FXML private Label turnLabel;
    @FXML private Label shotsLabel;

    private String playerName;

    // --- Renderer Instance (VIEW) ---
    // Uses the Strategy/Adapter pattern to delegate ship drawing
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    // --- Game Logic (MODEL) ---
    // Logical representation of the boards (10x10 grids of Cells)
    private final Cell[][] boardCells = new Cell[10][10];
    private final Cell[][] enemyBoardCells = new Cell[10][10];
    private final Pane[][] playerGridPanes = new Pane[10][10];

    // Visual feedback element for placing ships (Green/Red rectangle)
    private final Rectangle selectionHighlight = new Rectangle();

    // Visual feedback element for targeting enemy cells (Yellow rectangle)
    private final Rectangle enemySelectionHighlight = new Rectangle();

    private boolean isHorizontal = true; // Current orientation for ship placement
    private final double cellSize = 40.0; // Pixel size of a single grid cell


    // --- Flow Control ---
    private int shipsPlacedCount = 0; // Tracks how many ships the player has placed
    private final int TOTAL_SHIPS = 10; // Total ships required to start (1 Carrier + 2 Subs + 3 Dest + 4 Frigates)
    private boolean gameStarted = false; // Flag to indicate if the match is active




    // ========GETTERS

    /**
     * Called to initialize a controller after its root element has been completely processed.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateTurnLabel();
        if (shotsLabel != null) shotsLabel.setText("Disparos: 0");

        // 1. Initialize logical data models
        initializeDataModel();

        // 2. Draw visual grids
        drawPlayerBoardGrid();
        drawEnemyBoardGrid();

        // 3. Setup interaction handlers
        setupBoardDragHandlers();
        setupDraggableShips();
        drawFleet();
        placeEnemyShipsRandomly();

        // 4. Setup enemy board interaction (Shooting)
        setupEnemyInteraction();

        // 5. Initialize UI state
        if (debugCheckBox != null) debugCheckBox.setSelected(false);
        if (playButton != null) playButton.setDisable(true);
    }

    /**
     * Configures mouse events for the enemy board.
     * Handles shooting (click) and visual targeting feedback (mouse move).
     */
    private void setupEnemyInteraction() {
        if (enemyShipsPane == null) return;

        // 1. CLICK EVENT (Shooting logic placeholder)
        enemyShipsPane.setOnMouseClicked(event -> {
            if (!gameStarted) {
                System.out.println("¡Debes iniciar el juego primero!");
                return;
            }

            // Calculate grid coordinates based on mouse position
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            // Validate bounds
            if (col >= 0 && col < 10 && row >= 0 && row < 10) {
                System.out.println("------------------------------------------------");
                System.out.println("DISPARO A:");
                System.out.println("    COLUMNA: " + col);
                System.out.println("    FILA:    " + row);
                System.out.println("------------------------------------------------");
                // Logic for checking hit/miss on enemyBoardCells would go here
            }
        });

        // 2. MOUSE MOVE EVENT (Visual Highlight)
        enemyShipsPane.setOnMouseMoved(event -> {
            // Only show highlight if the game has started
            if (!gameStarted) return;

            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            // Update highlight position if within bounds
            if (col >= 0 && col < 10 && row >= 0 && row < 10) {
                enemySelectionHighlight.setLayoutX(col * cellSize);
                enemySelectionHighlight.setLayoutY(row * cellSize);
                enemySelectionHighlight.setVisible(true);
            } else {
                enemySelectionHighlight.setVisible(false);
            }
        });

        // 3. MOUSE EXIT EVENT (Hide Highlight)
        enemyShipsPane.setOnMouseExited(event -> {
            enemySelectionHighlight.setVisible(false);
        });
    }

    // -------------------------------------------------------------------------
    // --- DRAWING METHODS ---
    // -------------------------------------------------------------------------

    /**
     * Draws the grid lines and initializes the selection highlight for the Player's board.
     */
    private void drawPlayerBoardGrid() {
        double boardSize = cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);
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
        if (!shipsPane.getChildren().isEmpty()) shipsPane.getChildren().add(0, gridCanvas);
        else shipsPane.getChildren().add(gridCanvas);

        // Initialize Player Highlight Rectangle
        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5);
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true);
        shipsPane.getChildren().add(selectionHighlight);
    }

    /**
     * Draws the grid lines and initializes the selection highlight for the Enemy's board.
     */
    private void drawEnemyBoardGrid() {
        if (enemyShipsPane == null) return;
        double boardSize = cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);
        gridCanvas.setId("Grid"); // ID used to prevent hiding this canvas when toggling debug mode

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#FFFFFF", 0.3));
        gc.setLineWidth(1.0);
        for (int i = 0; i <= 10; i++) {
            double pos = i * cellSize;
            gc.strokeLine(pos, 0, pos, boardSize);
            gc.strokeLine(0, pos, boardSize, pos);
        }

        if (!enemyShipsPane.getChildren().isEmpty()) enemyShipsPane.getChildren().add(0, gridCanvas);
        else enemyShipsPane.getChildren().add(gridCanvas);

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
     * Handles the "Play" button action.
     * Starts the game, disables setup controls, and locks debug features.
     */
    @FXML
    void onPlayButton(ActionEvent event) {
        gameStarted = true;
        playButton.setDisable(true);
        playButton.setText("EN JUEGO");

        // Force hide enemy ships and disable debug checkbox for fairness
        if (debugCheckBox != null) {
            debugCheckBox.setSelected(false);
            onDebugModeChanged(null);
            debugCheckBox.setDisable(true);
        }
        turnLabel.setText("¡Ataque! Turno de " + playerName);
    }

    /**
     * Sets the player's name received from the Welcome Controller.
     * @param name The nickname entered by the user.
     */
    public void setPlayerName(String name) {
        this.playerName = name;
        updateTurnLabel();
    }

    /**
     * Updates the UI label showing whose turn it is.
     */
    private void updateTurnLabel() {
        if (turnLabel != null) turnLabel.setText("Turno: " + playerName);
    }

    /**
     * Initializes the 2D arrays for board logic with empty Cells.
     */
    private void initializeDataModel() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardCells[i][j] = new Cell(i, j);
                enemyBoardCells[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * Toggles visibility of enemy ships based on the debug CheckBox.
     * Does not affect the grid lines.
     */
    @FXML
    void onDebugModeChanged(ActionEvent event) {
        if (enemyShipsPane != null) {
            boolean show = debugCheckBox.isSelected();
            for (Node node : enemyShipsPane.getChildren()) {
                // Show/Hide ships (Canvases that are not the Grid)
                if (node instanceof Canvas && (node.getId() == null || !node.getId().equals("Grid"))) {
                    node.setVisible(show);
                }
            }
        }
    }

    /**
     * Randomly places the enemy fleet on the board.
     * Used for AI setup.
     */
    private void placeEnemyShipsRandomly() {
        // Ship sizes: 1 Carrier (4), 2 Subs (3), 3 Destroyers (2), 4 Frigates (1)
        int[] shipSizes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        Random random = new Random();
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(10);
                int y = random.nextInt(10);
                boolean horizontal = random.nextBoolean();

                if (isValidEnemyPlacement(x, y, size, horizontal)) {
                    placeEnemyShip(x, y, size, horizontal);
                    placed = true;
                }
            }
        }
    }

    /**
     * Checks if an enemy ship can be placed at the specified coordinates.
     */
    private boolean isValidEnemyPlacement(int x, int y, int size, boolean horizontal) {
        // Check boundaries
        if (horizontal) { if (x + size > 10) return false; }
        else { if (y + size > 10) return false; }

        // Check overlap
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            if (enemyBoardCells[targetX][targetY].getOccupyingShip() != null) return false;
        }
        return true;
    }

    /**
     * Places an enemy ship both logically and visually.
     */
    private void placeEnemyShip(int x, int y, int size, boolean horizontal) {
        Ship enemyShip = new Ship(size, "Enemigo");
        // Update Model
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            enemyBoardCells[targetX][targetY].setOccupyingShip(enemyShip);
        }

        // Update View (Visual Canvas)
        Canvas enemyShipCanvas = new Canvas();
        enemyShipCanvas.setWidth(size * cellSize);
        enemyShipCanvas.setHeight(cellSize);
        shipRenderer.render(enemyShipCanvas, size);

        if (horizontal) {
            enemyShipCanvas.setLayoutX(x * cellSize);
            enemyShipCanvas.setLayoutY(y * cellSize);
        } else {
            enemyShipCanvas.setRotate(90);
            // Visual offset correction for rotation
            double offset = cellSize * (1 - size) / 2.0;
            enemyShipCanvas.setLayoutX((x * cellSize) + offset);
            enemyShipCanvas.setLayoutY((y * cellSize) - offset);
        }

        // Initially hide enemy ships unless debug is already on
        enemyShipCanvas.setVisible(debugCheckBox != null && debugCheckBox.isSelected());
        enemyShipsPane.getChildren().add(enemyShipCanvas);
    }

    /**
     * Sets up Drag & Drop handlers for the Player's board.
     * Handles preview (highlight) and placement (drop).
     */
    private void setupBoardDragHandlers() {
        // Drag Over: Update highlight position
        shipsPane.setOnDragOver(event -> {
            if (!gameStarted && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            if (!gameStarted && event.getDragboard().hasString()) {
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
            if (!gameStarted && db.hasString()) {
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
            selectionHighlight.setVisible(false);
            event.setDropCompleted(success);
            event.consume();
        });

        // Drag Exited: Hide highlight
        shipsPane.setOnDragExited(event -> {
            selectionHighlight.setVisible(false);
            event.consume();
        });

        // Click: Rotate orientation (Right Click)
        shipsPane.setOnMouseClicked(event -> {
            if (!gameStarted && event.getButton() == MouseButton.SECONDARY) {
                isHorizontal = !isHorizontal;
                System.out.println("Orientación: " + (isHorizontal ? "Horizontal" : "Vertical"));
            }
        });
    }

    /**
     * Updates the position and color of the placement highlight rectangle.
     */
    private void updateHighlight(int col, int row, int size) {
        if (col < 0 || row < 0 || col >= 10 || row >= 10) {
            selectionHighlight.setVisible(false);
            return;
        }
        if (isHorizontal) {
            selectionHighlight.setWidth(size * cellSize);
            selectionHighlight.setHeight(cellSize);
        } else {
            selectionHighlight.setWidth(cellSize);
            selectionHighlight.setHeight(size * cellSize);
        }
        selectionHighlight.setLayoutX(col * cellSize);
        selectionHighlight.setLayoutY(row * cellSize);

        boolean valid = isValidPlacement(col, row, size, isHorizontal);
        if (valid) selectionHighlight.setFill(Color.rgb(0, 255, 0, 0.4)); // Green
        else selectionHighlight.setFill(Color.rgb(255, 0, 0, 0.4)); // Red

        selectionHighlight.setVisible(true);
        selectionHighlight.toFront();
    }

    /**
     * Validates if a player ship can be placed at the given coordinates.
     */
    private boolean isValidPlacement(int x, int y, int size, boolean horizontal) {
        if (horizontal && x + size > 10) return false;
        if (!horizontal && y + size > 10) return false;
        if (x < 0 || y < 0 || x >= 10 || y >= 10) return false;

        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            if (boardCells[targetX][targetY].getOccupyingShip() != null) return false;
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
        // Update Model
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            boardCells[targetX][targetY].setOccupyingShip(newShip);
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

        // Update Game State
        shipsPlacedCount++;
        if (shipsPlacedCount == TOTAL_SHIPS) {
            if (playButton != null) playButton.setDisable(false);
        }
    }

    /**
     * Initializes drag events for the ships in the selection palette.
     */
    private void setupDraggableShips() {
        if (carrierCanvas != null) makeDraggable(carrierCanvas, 4);
        if (submarineCanvas1 != null) makeDraggable(submarineCanvas1, 3);
        if (submarineCanvas2 != null) makeDraggable(submarineCanvas2, 3);
        if (destroyerCanvas1 != null) makeDraggable(destroyerCanvas1, 2);
        if (destroyerCanvas2 != null) makeDraggable(destroyerCanvas2, 2);
        if (destroyerCanvas3 != null) makeDraggable(destroyerCanvas3, 2);
        if (frigateCanvas1 != null) makeDraggable(frigateCanvas1, 1);
        if (frigateCanvas2 != null) makeDraggable(frigateCanvas2, 1);
        if (frigateCanvas3 != null) makeDraggable(frigateCanvas3, 1);
        if (frigateCanvas4 != null) makeDraggable(frigateCanvas4, 1);
    }

    /**
     * Generic method to make a Canvas draggable.
     */
    private void makeDraggable(Canvas sourceCanvas, int size) {
        sourceCanvas.setOnDragDetected(event -> {
            if (gameStarted) return;
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

    /**
     * Draws the initial fleet on the selection palette using the renderer.
     */
    private void drawFleet() {
        if (carrierCanvas != null) shipRenderer.render(carrierCanvas, 4);
        if (submarineCanvas1 != null) shipRenderer.render(submarineCanvas1, 3);
        if (submarineCanvas2 != null) shipRenderer.render(submarineCanvas2, 3);
        if (destroyerCanvas1 != null) shipRenderer.render(destroyerCanvas1, 2);
        if (destroyerCanvas2 != null) shipRenderer.render(destroyerCanvas2, 2);
        if (destroyerCanvas3 != null) shipRenderer.render(destroyerCanvas3, 2);
        if (frigateCanvas1 != null) shipRenderer.render(frigateCanvas1, 1);
        if (frigateCanvas2 != null) shipRenderer.render(frigateCanvas2, 1);
        if (frigateCanvas3 != null) shipRenderer.render(frigateCanvas3, 1);
        if (frigateCanvas4 != null) shipRenderer.render(frigateCanvas4, 1);
    }
}