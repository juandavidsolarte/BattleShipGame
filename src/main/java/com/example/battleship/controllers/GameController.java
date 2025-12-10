package com.example.battleship.controllers;

import com.example.battleship.models.Cell;
import com.example.battleship.models.Ship;
import com.example.battleship.views.CanvasShipRenderer;
import com.example.battleship.models.GameState;
import com.example.battleship.models.CellState;
import com.example.battleship.views.ShipRenderer;
import com.example.battleship.persistence.GameFileManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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

    // --- Renderer Instance (VIEW) ---
    // Uses the Strategy/Adapter pattern to delegate ship drawing
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    // --- Game Logic (MODEL) ---
    // Logical representation of the boards (10x10 grids of Cells)
    private Cell[][] boardCells = new Cell[10][10];
    private Cell[][] enemyBoardCells = new Cell[10][10];
    private Pane[][] playerGridPanes = new Pane[10][10];

    // Visual feedback element for placing ships (Green/Red rectangle)
    private final Rectangle selectionHighlight = new Rectangle();

    // Visual feedback element for targeting enemy cells (Yellow rectangle)
    private final Rectangle enemySelectionHighlight = new Rectangle();
    // image bomb
    private Image bombImage = new Image(getClass().getResourceAsStream("/com/example/battleship/views/images/bomb.png"));
    private Image fireImage = new Image(getClass().getResourceAsStream("/com/example/battleship/views/images/fuego.gif"));

    private boolean isHorizontal = true; // Current orientation for ship placement
    private final double cellSize = 40.0; // Pixel size of a single grid cell
    private String playerName = "Jugador"; // Default player name
    private int shotsCounter = 0; // Shot counter for states

    // --- Flow Control ---
    private int shipsPlacedCount = 0; // Tracks how many ships the player has placed
    private final int TOTAL_SHIPS = 10; // Total ships required to start (1 Carrier + 2 Subs + 3 Dest + 4 Frigates)
    private boolean gameStarted = false; // Flag to indicate if the match is active

    // Counters for victory condition
    private int enemyShipsSunkCount = 0; // How many enemy ships did the player sink
    private int playerShipsSunkCount = 0; // How many of the player's ships did the AI sink?

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

  //DEBUGG
    /*
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
    }*/

    // -------------------------------------------------------------------------
    // --- LOGIC ---
    // -------------------------------------------------------------------------


    private void checkWinCondition()
    {
        if (enemyShipsSunkCount >= 10)
        {
            showAlert("¡VICTORIA!", "¡Has hundido toda la flota enemiga!");
            gameStarted = false;
            // Save final record in a flat file
            GameFileManager.saveTextLog(playerName, enemyShipsSunkCount);
        }
        else if (playerShipsSunkCount >= 10)
        {
            showAlert("DERROTA", "La maquina ha hundido tu flota. ¡Intentalo de nuevo!");
            gameStarted = false;
            GameFileManager.saveTextLog(playerName, enemyShipsSunkCount);
        }
    }

    private void saveGameAutomatic()
    {
        // The current state is saved
        GameState state = new GameState(boardCells, enemyBoardCells, playerName, shotsCounter, true);
        GameFileManager.saveGame(state);
    }

    private void showAlert(String title, String message)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Load Game - Restore State
    public void loadGameState(GameState state)
    {
        this.boardCells = state.getPlayerBoard();
        this.enemyBoardCells = state.getEnemyBoard();
        this.playerName = state.getPlayerName();
        this.shotsCounter = state.getShotsCounter();
        this.gameStarted = true;

        setPlayerName(this.playerName);
        if (shotsLabel != null) shotsLabel.setText("Disparos: " + shotsCounter);
        playButton.setDisable(true);
        playButton.setText("EN JUEGO");

        // Ocultar flota seleccionable (ya estamos jugando)
        hideFleet();

        // Redibujar tableros con el estado cargado
        redrawBoardsFromState();

        System.out.println("Juego cargado exitosamente.");
    }

    /**
     * Configures mouse events for the enemy board.
     * Handles shooting (click) and visual targeting feedback (mouse move).
     */
    private void setupEnemyInteraction()
    {
        if (enemyShipsPane == null) return;

        // shoot
        enemyShipsPane.setOnMouseClicked(event ->
        {
            if (!gameStarted) return;

            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            // Validate limits
            if (col >= 0 && col < 10 && row >= 0 && row < 10)
            {
                try
                {
                    handlePlayerShot(col, row);
                }
                catch (Exception e)
                {
                    System.out.println("Error al disparar: " + e.getMessage());
                }
            }
        });

        // Move mouse and highlight box
        enemyShipsPane.setOnMouseMoved(event ->
        {
            if (!gameStarted) return;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            if (col >= 0 && col < 10 && row >= 0 && row < 10)
            {
                enemySelectionHighlight.setLayoutX(col * cellSize);
                enemySelectionHighlight.setLayoutY(row * cellSize);
                enemySelectionHighlight.setVisible(true);
            }
            else
            {
                enemySelectionHighlight.setVisible(false);
            }
        });

        // Exit and hide lighting
        enemyShipsPane.setOnMouseExited(event ->
        {
            enemySelectionHighlight.setVisible(false);
        });
    }



    /**
     * helps the human interpret actions once triggered
     */
    private void handlePlayerShot(int col, int row) {
        Cell targetCell = enemyBoardCells[col][row];

        // 1. Validar si ya se disparó en esa celda
        if (targetCell.getState() == com.example.battleship.models.CellState.HIT ||
                targetCell.getState() == com.example.battleship.models.CellState.MISSED_SHOT ||
                targetCell.getState() == com.example.battleship.models.CellState.SUNK) {
            System.out.println("¡Ya disparaste aquí!");
            return;
        }

        boolean hit = (targetCell.getOccupyingShip() != null);

        // 2. Actualizar el Modelo (Lógica de impacto)
        // --- SECCIÓN CORREGIDA ---
        if (hit) {
            targetCell.setState(com.example.battleship.models.CellState.HIT);
            targetCell.getOccupyingShip().receiveShot();
            System.out.println("¡TOCADO!");

            if (targetCell.getOccupyingShip().isSunk()) {
                targetCell.setState(com.example.battleship.models.CellState.SUNK);
                System.out.println("¡HUNDIDO!");

                // 1. PRIMERO: Dibujamos la bomba del disparo actual
                drawShotResult(enemyShipsPane, col, row, true);

                // 2. SEGUNDO: Dibujamos el fuego sobre el barco (incluida la bomba recién puesta)
                markShipAsSunk(enemyShipsPane, enemyBoardCells, targetCell.getOccupyingShip());

                enemyShipsSunkCount++;
                checkWinCondition();
            } else {
                // Es un toque, pero no se hundió. Solo dibujamos la bomba.
                drawShotResult(enemyShipsPane, col, row, true);
            }

        } else {
            // Es Agua.
            targetCell.setState(com.example.battleship.models.CellState.MISSED_SHOT);
            System.out.println("AGUA.");
            // Dibujamos la X de agua
            drawShotResult(enemyShipsPane, col, row, false);
        }
        // 3. Actualizar la Vista (Dibujar X o Círculo)

        updateStats();

        // 4. Gestión del cambio de turno con PAUSA
        // Si fallaste (agua) y el juego sigue activo, le toca a la máquina.
        if (!hit && gameStarted) {

            // A) Actualizar etiqueta visualmente
            if (turnLabel != null) turnLabel.setText("Turno: Enemigo");

            // B) Bloquear el tablero enemigo para evitar clicks rápidos durante la espera
            enemyShipsPane.setDisable(true);

            // C) Crear una pausa de 1.5 segundos (ajustable)
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

            pause.setOnFinished(e -> {
                // D) Ejecutar el turno enemigo tras la pausa
                enemyTurn();

                // E) Desbloquear el tablero para que el jugador pueda volver a jugar
                // (Solo si el juego sigue activo tras el ataque enemigo)
                if (gameStarted) {
                    enemyShipsPane.setDisable(false);
                }
            });

            // F) Iniciar el contador
            pause.play();
        }
    }

    /**
     * simulate the attack of the (rival) machine
     */
    private void enemyTurn()
    {
        if (turnLabel != null) turnLabel.setText("Turno: Enemigo...");

        // Simple AI - Shoot at random until it misses
        boolean keepPlaying = true;
        Random random = new Random();

        while (keepPlaying && gameStarted) { // Agregamos && gameStarted por seguridad
            int col = random.nextInt(10);
            int row = random.nextInt(10);
            Cell target = boardCells[col][row];

            if (target.getState() == CellState.WATER || target.getState() == CellState.SHIP) {

                boolean hit = (target.getOccupyingShip() != null);

                if (hit) {
                    target.setState(com.example.battleship.models.CellState.HIT);
                    target.getOccupyingShip().receiveShot();
                    drawShotResult(shipsPane, col, row, true);

                    if (target.getOccupyingShip().isSunk()) {
                        target.setState(com.example.battleship.models.CellState.SUNK);

                        // --- CAMBIO 3: Aumentar contador enemigo y verificar derrota ---
                        playerShipsSunkCount++;
                        checkWinCondition();

                        // Si el juego termina aquí, salimos del bucle
                        if (!gameStarted) return;
                    }
                } else {
                    target.setState(com.example.battleship.models.CellState.MISSED_SHOT);
                    drawShotResult(shipsPane, col, row, false);
                    keepPlaying = false;
                }
                saveGameAutomatic();
            }
        }

        // Solo restaurar el label si el juego sigue activo
        if (gameStarted && turnLabel != null) turnLabel.setText("Turno: " + playerName);
    }

    // || --- Utilities --- ||

    /**
     * Updates and displays the number of shots made by the player
     */
    private void updateStats()
    {
        shotsCounter++;
        if (shotsLabel != null)
        {
            shotsLabel.setText("Disparos: " + shotsCounter);
        }
    }

    /**
     * Draw an 'X' or a blue circle if it was correct or not
     */
    private void drawShotResult(Pane pane, int col, int row, boolean hit) {
        Canvas shotCanvas = new Canvas(cellSize, cellSize);
        shotCanvas.setLayoutX(col * cellSize);
        shotCanvas.setLayoutY(row * cellSize);
        shotCanvas.setMouseTransparent(true);

        GraphicsContext gc = shotCanvas.getGraphicsContext2D();

        if (hit) {
            // --- TOCADO (BARCO) CON IMAGEN ---

            // 1. Fondo rojo (opcional, si quieres que resalte más)
            gc.setFill(Color.RED);
            gc.fillOval(5, 5, cellSize - 10, cellSize - 10);

            // 2. Imagen de la bomba
            // Los parámetros son: imagen, x, y, ancho, alto
            if (bombImage != null) {
                gc.drawImage(bombImage, 5, 5, cellSize - 10, cellSize - 10);
            }

        } else {
            // --- AGUA (FALLO) ---
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(3);
            gc.strokeLine(10, 10, cellSize - 10, cellSize - 10);
            gc.strokeLine(cellSize - 10, 10, 10, cellSize - 10);
        }

        pane.getChildren().add(shotCanvas);
    }

    // || --- Redrawn (Load Game) --- ||

    private void redrawBoardsFromState() {
        // Redraw shots
        for(int i=0; i<10; i++){
            for(int j=0; j<10; j++){
                Cell c = boardCells[i][j];
                if(c.getState() == CellState.HIT || c.getState() == CellState.SUNK) drawShotResult(shipsPane, i, j, true);
                else if(c.getState() == CellState.MISSED_SHOT) drawShotResult(shipsPane, i, j, false);
                else if(c.getState() == CellState.SHIP) {
                    //for complete
                }
            }
        }
        // Redraw shots on enemy board
        for(int i=0; i<10; i++){
            for(int j=0; j<10; j++){
                Cell c = enemyBoardCells[i][j];
                if(c.getState() == CellState.HIT || c.getState() == CellState.SUNK) drawShotResult(enemyShipsPane, i, j, true);
                else if(c.getState() == CellState.MISSED_SHOT) drawShotResult(enemyShipsPane, i, j, false);
            }
        }
    }

    private void hideFleet() {
        if(carrierCanvas!=null) carrierCanvas.setVisible(false);
        if(submarineCanvas1!=null) submarineCanvas1.setVisible(false);
        if(submarineCanvas2!=null) submarineCanvas2.setVisible(false);
        if(destroyerCanvas1!=null) destroyerCanvas1.setVisible(false);
        if(destroyerCanvas2!=null) destroyerCanvas2.setVisible(false);
        if(destroyerCanvas3!=null) destroyerCanvas3.setVisible(false);
        if(frigateCanvas1!=null) frigateCanvas1.setVisible(false);
        if(frigateCanvas2!=null) frigateCanvas2.setVisible(false);
        if(frigateCanvas3!=null) frigateCanvas3.setVisible(false);
        if(frigateCanvas4!=null) frigateCanvas4.setVisible(false);
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

    // Other methods

    /**
     * Dibuja la imagen de fuego en una celda específica.
     */
    private void drawFire(Pane pane, int col, int row) {
        Canvas fireCanvas = new Canvas(cellSize, cellSize);
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

        pane.getChildren().add(fireCanvas);
    }

    /**
     * Busca todas las celdas que pertenecen al barco hundido y les dibuja fuego.
     */
    private void markShipAsSunk(Pane pane, Cell[][] board, Ship sunkShip) {
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



}