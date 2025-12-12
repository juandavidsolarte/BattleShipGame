package com.example.battleship.controllers;

import com.example.battleship.models.Cell;
import com.example.battleship.views.BoardVisualizer;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Main Controller for the Battleship Game.
 * Handles user interaction, game logic, board rendering, and ship placement.
 */
public class GameController implements Initializable
{
    //regio
    // --- Constants --
    private final ShipRenderer shipRenderer = new CanvasShipRenderer(); // We use interface logic to create a new render
    private final double cellSize = 40.0; // Pixel size of a single grid cell
    private final int TOTAL_SHIPS = 10; // Total ships required to start (1 Carrier + 2 Subs + 3 Dest + 4 Frigates)

    // --- FXML Layout Elements ---
    @FXML private javafx.scene.layout.StackPane rootPane;
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
    @FXML private Label playerScoreLabel;
    @FXML private Label enemyScoreLabel;

    // --- Assistant and manager classes ---

    private BoardVisualizer boardVisualizer; // Class to view and manage graphic elements of the board
    private ShipPlacementManager placementManager; // Class for handling draggable elements

    // --- Game Logic (MODEL) ---
    // Logical representation of the boards (10x10 grids of Cells)
    private Cell[][] boardCells = new Cell[10][10];
    private Cell[][] enemyBoardCells = new Cell[10][10];
    private String playerName = "Jugador"; // Default player name
    private int shotsCounter = 0; // Shot counter for states

    // --- Flow Control ---
    private int shipsPlacedCount = 0; // Tracks how many ships the player has placed
    private boolean gameStarted = false; // Flag to indicate if the match is active
    private boolean machineTurn = false;// Enables the use and dynamism of the current shift

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

        // The parameters are passed to the helper classes
        boardVisualizer = new BoardVisualizer(shipsPane, enemyShipsPane, cellSize);
        placementManager = new ShipPlacementManager(this, boardVisualizer, shipsPane, cellSize);

        // Initialize logical data models
        initializeDataModel();

        // Visual grids are drawn
        boardVisualizer.drawPlayerBoardGrid();
        boardVisualizer.drawEnemyBoardGrid();

        // Setup interaction handlers
        placementManager.setupBoardDragHandlers();
        setupDraggableShips();
        drawFleet();
        placeEnemyShipsRandomly();

        // Setup enemy board interaction (Shooting)
        setupEnemyInteraction();

        // Initialize UI state
        if (debugCheckBox != null) debugCheckBox.setSelected(false);
        if (playButton != null) playButton.setDisable(true);
        rootPane.setFocusTraversable(true);

        // Listener for rotation with "R"
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                placementManager.toggleOrientation();
                System.out.println("Rotando barco...");
            }
        });
        rootPane.setOnMouseClicked(event -> rootPane.requestFocus());

        // The focus is given immediately upon opening the window.
        Platform.runLater(() -> rootPane.requestFocus());
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
     * Initializes drag events for the ships in the selection palette.
     */
    private void setupDraggableShips() {
        if (carrierCanvas != null) placementManager.makeDraggable(carrierCanvas, 4);
        if (submarineCanvas1 != null) placementManager.makeDraggable(submarineCanvas1, 3);
        if (submarineCanvas2 != null) placementManager.makeDraggable(submarineCanvas2, 3);
        if (destroyerCanvas1 != null) placementManager.makeDraggable(destroyerCanvas1, 2);
        if (destroyerCanvas2 != null) placementManager.makeDraggable(destroyerCanvas2, 2);
        if (destroyerCanvas3 != null) placementManager.makeDraggable(destroyerCanvas3, 2);
        if (frigateCanvas1 != null) placementManager.makeDraggable(frigateCanvas1, 1);
        if (frigateCanvas2 != null) placementManager.makeDraggable(frigateCanvas2, 1);
        if (frigateCanvas3 != null) placementManager.makeDraggable(frigateCanvas3, 1);
        if (frigateCanvas4 != null) placementManager.makeDraggable(frigateCanvas4, 1);
    }

    // --- Interface events ---

    /**
     * Controls the action of the "Play" button.
     * Starts the game, disables configuration controls,
     * blocks debugging functions, and saves the game state.
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
        saveGameAutomatic();
    }

    /**
     * Toggles visibility of enemy ships based on the debug CheckBox.
     * Does not affect the grid lines.
     */
    @FXML
    void onDebugModeChanged(ActionEvent event) {
        if (enemyShipsPane != null && boardVisualizer != null) {
            boolean show = debugCheckBox.isSelected();

            boardVisualizer.setDebugMode(debugCheckBox.isSelected());

            for (Node node : enemyShipsPane.getChildren()) {
                // Show/Hide ships
                if ("EnemyShip".equals(node.getId())) {
                    node.setVisible(show);
                }
            }
        }
    }

    // --- GAME LOGIC ----

    /**
     * Handles the player's shot on the enemy board.
     * This method processes a shot attempt at the specified coordinates,
     * updates the game state, visual feedback, and manages turn transitions.
     * We've implemented a step-by-step validation and feedback system:
     * 1. Checks if the cell was already targeted
     * 2. Processes hit/miss logic and updates the model
     * 3. Updates visual elements with appropriate animations
     * 4. Manages turn switching with a smooth transition delay
     *
     * @param col The column index (0-based) of the target cell
     * @param row The row index (0-based) of the target cell
     */
    private void handlePlayerShot(int col, int row) {
        Cell targetCell = enemyBoardCells[col][row];

        // Prevent shooting at already targeted cells
        if (targetCell.getState() == CellState.HIT ||
                targetCell.getState() == CellState.MISSED_SHOT ||
                targetCell.getState() == CellState.SUNK) {
            System.out.println("¡Ya disparaste aquí!");
            return;
        }

        boolean hit = (targetCell.getOccupyingShip() != null);

        // Process the shot result
        if (hit) {
            // Register a successful hit
            targetCell.setState(CellState.HIT);
            targetCell.getOccupyingShip().receiveShot();
            System.out.println("¡TOCADO!");

            if (targetCell.getOccupyingShip().isSunk()) {
                // Ship has been destroyed
                targetCell.setState(CellState.SUNK);
                System.out.println("¡HUNDIDO!");

                // The impactful animation is drawn
                boardVisualizer.drawShotResult(enemyShipsPane, col, row, true);

                // Then, highlight the entire sunken ship with fire effects.
                boardVisualizer.markShipAsSunk(enemyShipsPane, enemyBoardCells, targetCell.getOccupyingShip());

                enemyShipsSunkCount++;
                updateScoreLabels();
                checkWinCondition();
            } else {
                // Hit but not sunk - just show the impact
                boardVisualizer.drawShotResult(enemyShipsPane, col, row, true);
            }
        }
        else
        {
            // Missed shot
            targetCell.setState(com.example.battleship.models.CellState.MISSED_SHOT);
            System.out.println("AGUA.");
            // Show 'X' effect
            boardVisualizer.drawShotResult(enemyShipsPane, col, row, false);
        }

        // Update game statistics
        updateStats();

        // Auto-save the current game state
        saveGameAutomatic();

        // Handle turn transition with visual feedback
        // If player misses and game is still active, it's the enemy's turn
        if (!hit && gameStarted) {
            machineTurn = true;

            // Update turn indicator
            if (turnLabel != null) turnLabel.setText("Turno: Enemigo");

            // Temporarily disable player interaction during enemy turn
            enemyShipsPane.setDisable(true);

            // Add a brief pause for better gameplay flow
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

            pause.setOnFinished(e -> {
                // Execute enemy's move after pause
                enemyTurn();

                // Re-enable player controls if game is still active
                if (gameStarted) {
                    enemyShipsPane.setDisable(false);
                }
            });
            pause.play();
        }
    }

    /**
     * Executes the enemy's (machine) attack turn.
     * We've implemented a straightforward AI logic that randomly targets cells
     * until it misses. This creates a simple but effective challenge for the player.
     * The AI continues hitting when successful, but stops after a miss to give
     * the player their turn back.
     * Additional safety checks ensure the turn stops immediately if the game
     * ends during the enemy's turn sequence.
     */
    private void enemyTurn()
    {
        // Update UI to indicate enemy's turn
        if (turnLabel != null) turnLabel.setText("Turno: Enemigo...");

        boolean keepPlaying = true;
        Random random = new Random();

        // Continue attacking as long as the enemy keeps hitting ships
        // We added && gameStarted for security
        while (keepPlaying && gameStarted) {
            // Randomly select a target cell
            int col = random.nextInt(10);
            int row = random.nextInt(10);
            Cell target = boardCells[col][row];

            // Only process if cell hasn't been attacked yet
            if (target.getState() == CellState.WATER || target.getState() == CellState.SHIP) {

                Ship targetShip = target.getOccupyingShip();
                boolean hit = (target.getOccupyingShip() != null);

                if (hit) {
                    // Only process if cell hasn't been attacked yet
                    target.setState(CellState.HIT);
                    targetShip.receiveShot();
                    boardVisualizer.drawShotResult(shipsPane, col, row, true);

                    if (targetShip.isSunk()) {
                        // Player's ship has been destroyed
                        target.setState(CellState.SUNK);
                        boardVisualizer.markShipAsSunk(shipsPane, boardCells, targetShip);
                        playerShipsSunkCount++;
                        updateScoreLabels();
                        checkWinCondition();

                        // Exit if game ended with this sunk ship
                        if (!gameStarted) return;
                    }
                }   // Enemy continues attacking after a hit
                else
                {
                    // Enemy missed - turn ends
                    target.setState(CellState.MISSED_SHOT);
                    boardVisualizer.drawShotResult(shipsPane, col, row, false);
                    keepPlaying = false;
                    machineTurn = false;
                }
                // Auto-save after each attack
                saveGameAutomatic();
            }
        }
        // Restore player turn indicator if game is still active
        if (gameStarted && turnLabel != null) turnLabel.setText("Turno: " + playerName);
    }

    /**
     * Checks if either player has met the victory conditions.
     * We always recalculate the actual sunk ships from the game boards
     * to prevent any synchronization issues with the counters. This ensures
     * the win condition is based on the current state of the game rather
     * than potentially stale counter values.
     * The method handles both victory (player wins) and defeat (enemy wins)
     * scenarios, logging the result and triggering the game over sequence.
     */
    private void checkWinCondition()
    {
        // Recalculate from actual board state to avoid counter desynchronization
        enemyShipsSunkCount = countActualSunkShips(enemyBoardCells);
        playerShipsSunkCount = countActualSunkShips(boardCells);

        // Debug output to track progress in console
        System.out.println("VERIFICANDO: Enemigos hundidos = " + enemyShipsSunkCount + "/10");

        // Check for player victory
        if (enemyShipsSunkCount >= 10) {
            GameFileManager.saveTextLog(playerName, enemyShipsSunkCount, "GANADOR");
            handleGameOver("¡VICTORIA!", "¡Has hundido toda la flota enemiga!");
        }
        // Check for player defeat
        else if (playerShipsSunkCount >= 10) {
            GameFileManager.saveTextLog(playerName, enemyShipsSunkCount, "PERDEDOR");
            handleGameOver("DERROTA", "La maquina ha hundido tu flota.");
        }
    }

    // --- Persistance Section ---

    /**
     * Automatically saves the current game state.
     * We capture all essential game data into a GameState object and persist it
     * using our file management system. This creates regular save points during
     * gameplay without requiring player intervention.
     */
     private void saveGameAutomatic()
    {
        // The current state is saved
        GameState state = new GameState(
                boardCells,
                enemyBoardCells,
                playerName,
                shotsCounter,
                !machineTurn,
                enemyShipsSunkCount,
                playerShipsSunkCount,
                gameStarted
        );
        GameFileManager.saveGame(state);
    }

    /**
     * Loads and restores a previously saved game state.
     * We carefully reconstruct the game from a saved GameState object,
     * ensuring all visual elements, scores, and game logic are properly
     * synchronized. This allows players to resume their game exactly as
     * it was when saved, maintaining all progress and board configurations.
     * The restoration process includes:
     * 1. Restoring core game data and player info
     * 2. Rebuilding both game boards from saved state
     * 3. Updating UI elements to reflect loaded state
     * 4. Redrawing all visual components
     *
     * @param state The GameState object containing the saved game data
     */
    public void loadGameState(GameState state)
    {
        // Restore core game data
        this.playerName = state.getPlayerName();
        this.shotsCounter = state.getShotsCounter();
        this.gameStarted = state.isGameStarted();

        // Reconstruct game boards from saved state
        this.boardCells = state.getPlayerBoard();
        this.enemyBoardCells = state.getEnemyBoard();

        // Update player information display
        setPlayerName(this.playerName);
        if (shotsLabel != null) shotsLabel.setText("Disparos: " + shotsCounter);

        // Adjust UI based on game state
        if (gameStarted) {
            // Game is in progress - disable play button and hide fleet selection
            playButton.setDisable(true);
            playButton.setText("EN JUEGO");
            hideFleet();
        }

        // Recalculate sunk ship counts from actual board state
        this.playerShipsSunkCount = countActualSunkShips(this.boardCells);
        this.enemyShipsSunkCount = countActualSunkShips(this.enemyBoardCells);

        // Update score displays
        updateScoreLabels();

        // Restore visual ship representations on both boards
        boardVisualizer.restoreVisualShips(enemyBoardCells);
        boardVisualizer.drawPlayerShipsFromModel(this.boardCells);

        // Redraw complete board states
        redrawBoardsFromState();
        System.out.println("Juego cargado exitosamente.");
    }

    // --- Auxiliary methods ---

    /**
     * Updates the score display labels with current sunk ship counts.
     * We keep both players informed of their progress through clear,
     * real-time score indicators.
     */
    private void updateScoreLabels() {
        if (playerScoreLabel != null) {
            playerScoreLabel.setText("Mi Flota Perdida: " + playerShipsSunkCount + "/10");
        }
        if (enemyScoreLabel != null) {
            enemyScoreLabel.setText("Enemigos Hundidos: " + enemyShipsSunkCount + "/10");
        }
    }

    /**
     * Enables the Start button once all ships have been placed.
     * We check if the required number of ships are positioned and
     * the game hasn't started yet before activating the button.
     */
    public void checkStartButtonState() {
        if (shipsPlacedCount >= TOTAL_SHIPS && !gameStarted) {
            if (playButton == null) return;
            else
            {
                playButton.setDisable(false);
                playButton.setText("INICIAR JUEGO");
                playButton.requestLayout();
            }
        }
    }

    /**
     * Counts the actual number of unique sunk ships on the board.
     * We use a Set to ensure ships aren't counted multiple times,
     * giving us an accurate count of destroyed ships.
     */
    private int countActualSunkShips(Cell[][] board) {
        java.util.Set<Ship> sunkShips = new java.util.HashSet<>();

        // Scan the entire board for sunk ships
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Ship ship = board[i][j].getOccupyingShip();
                // Add to set if ship exists and is sunk
                if (ship != null && ship.isSunk()) {
                    sunkShips.add(ship);
                }
            }
        }
        // Set size gives us the unique count
        return sunkShips.size();
    }

    /**
     * Hides the selectable ship fleet from view.
     * We hide each ship canvas individually once the game starts,
     * preventing players from seeing or interacting with unplaced ships.
     */
    private void hideFleet()
    {
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

    /**
     * Handles the game over sequence and returns to the main menu.
     * We save the final result to the log, clear the save file,
     * show the outcome to the player, and transition back to the welcome screen.
     */
    private void handleGameOver(String title, String message) {
        gameStarted = false;

        // Log the final game result
        GameFileManager.saveTextLog(playerName, enemyShipsSunkCount, title);

        // Remove the save file since game is complete
        GameFileManager.deleteSaveFile();

        Platform.runLater(() -> {
            // Show game outcome dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            // Return to welcome screen
            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/battleship/views/welcome-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) playButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    // --- Getters and Setters ---

    /**
     * Sets the player's name received from the Welcome Controller.
     * @param name The nickname entered by the user.
     */
    public void setPlayerName(String name) {
        this.playerName = name;
        updateTurnLabel();
    }

    public Cell[][] getBoardCells() {
        return boardCells;
    }

    public boolean isGameStarted() {
        return gameStarted;
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
                boardVisualizer.getEnemySelectionHighlight().setLayoutX(col * cellSize);
                boardVisualizer.getEnemySelectionHighlight().setLayoutY(row * cellSize);
                boardVisualizer.getEnemySelectionHighlight().setVisible(true);
            }
            else
            {
                boardVisualizer.getEnemySelectionHighlight().setVisible(false);
            }
        });

        // Exit and hide lighting
        enemyShipsPane.setOnMouseExited(event ->
        {
            boardVisualizer.getEnemySelectionHighlight().setVisible(false);
        });
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



    // || --- Redrawn (Load Game) --- ||

    private void redrawBoardsFromState() {
        // 1. Redibujar MI tablero (Disparos recibidos por mí)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell c = boardCells[i][j];

                if (c.getState() == CellState.HIT) {
                    boardVisualizer.drawShotResult(shipsPane, i, j, true); // Bomba/Rojo
                } else if (c.getState() == CellState.SUNK) {
                    boardVisualizer.drawShotResult(shipsPane, i, j, true); // Bomba
                    boardVisualizer.markShipAsSunk(shipsPane, boardCells, c.getOccupyingShip()); // Fuego
                } else if (c.getState() == CellState.MISSED_SHOT) {
                    boardVisualizer.drawShotResult(shipsPane, i, j, false); // Agua/X
                }
            }
        }

        // 2. Redibujar TABLERO ENEMIGO (Mis disparos hacia la máquina)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell c = enemyBoardCells[i][j];

                if (c.getState() == CellState.HIT) {
                    boardVisualizer.drawShotResult(enemyShipsPane, i, j, true); // Bomba
                } else if (c.getState() == CellState.SUNK) {
                    boardVisualizer.drawShotResult(enemyShipsPane, i, j, true); // Bomba
                    boardVisualizer.markShipAsSunk(enemyShipsPane, enemyBoardCells, c.getOccupyingShip()); // Fuego
                } else if (c.getState() == CellState.MISSED_SHOT) {
                    boardVisualizer.drawShotResult(enemyShipsPane, i, j, false); // Agua
                }
            }
        }
    }

    /**
     * Updates the UI label showing whose turn it is.
     */
    private void updateTurnLabel() {
        if (turnLabel != null) turnLabel.setText("Turno: " + playerName);
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
        enemyShipCanvas.setId("EnemyShip");
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
        enemyShipsPane.getChildren().add(0,enemyShipCanvas);
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

    public void notifyShipPlaced() {
        shipsPlacedCount++;
        checkStartButtonState();
    }
}