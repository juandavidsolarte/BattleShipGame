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
import javafx.stage.Stage;
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

    private BoardVisualizer boardVisualizer;
    private ShipPlacementManager placementManager;

    // --- Renderer Instance (VIEW) ---
    // Uses the Strategy/Adapter pattern to delegate ship drawing
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    // --- Game Logic (MODEL) ---
    // Logical representation of the boards (10x10 grids of Cells)
    private Cell[][] boardCells = new Cell[10][10];
    private Cell[][] enemyBoardCells = new Cell[10][10];
    private Pane[][] playerGridPanes = new Pane[10][10];

    // Visual feedback element for placing ships (Green/Red rectangle)
    //private final Rectangle selectionHighlight = new Rectangle();

    // Visual feedback element for targeting enemy cells (Yellow rectangle)
    //private final Rectangle enemySelectionHighlight = new Rectangle();
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

        boardVisualizer = new BoardVisualizer(shipsPane, enemyShipsPane, cellSize);
        placementManager = new ShipPlacementManager(this, boardVisualizer, shipsPane, cellSize);
        // 1. Initialize logical data models
        initializeDataModel();

        // 2. Draw visual grids
        boardVisualizer.drawPlayerBoardGrid();
        boardVisualizer.drawEnemyBoardGrid();

        // 3. Setup interaction handlers
        placementManager.setupBoardDragHandlers();
        setupDraggableShips();
        drawFleet();
        placeEnemyShipsRandomly();

        // 4. Setup enemy board interaction (Shooting)
        setupEnemyInteraction();

        // 5. Initialize UI state
        if (debugCheckBox != null) debugCheckBox.setSelected(false);
        if (playButton != null) playButton.setDisable(true);
    }

    // -------------------------------------------------------------------------
    // --- LOGIC ---
    // -------------------------------------------------------------------------


    private void checkWinCondition()
    {
        // 1. RECALCULAR SIEMPRE para evitar errores de contadores desincronizados
        enemyShipsSunkCount = countActualSunkShips(enemyBoardCells);
        playerShipsSunkCount = countActualSunkShips(boardCells);

        // Debug para que veas en la consola cuántos lleva contados
        System.out.println("VERIFICANDO: Enemigos hundidos = " + enemyShipsSunkCount + "/10");

        // 2. Verificar Victoria
        if (enemyShipsSunkCount >= 10) {
            handleGameOver("¡VICTORIA!", "¡Has hundido toda la flota enemiga!");
        }
        // 3. Verificar Derrota
        else if (playerShipsSunkCount >= 10) {
            handleGameOver("DERROTA", "La máquina ha hundido tu flota.");
        }
    }

    private void handleGameOver(String title, String message) {
        gameStarted = false;

        //Guardar en el TXT usando la clase FileCRUD
        GameFileManager.saveTextLog(playerName, enemyShipsSunkCount, title);

        // Borrar el archivo de guardado
        GameFileManager.deleteSaveFile();

        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            //VOLVER AL INICIO
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

    private void saveGameAutomatic()
    {
        // The current state is saved
        GameState state = new GameState(
                boardCells,
                enemyBoardCells,
                playerName,
                shotsCounter,
                true,
                enemyShipsSunkCount,
                playerShipsSunkCount,
                gameStarted
        );
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
        this.gameStarted = state.isGameStarted();
        this.boardCells = state.getPlayerBoard();
        this.enemyBoardCells = state.getEnemyBoard();

        setPlayerName(this.playerName);
        if (shotsLabel != null) shotsLabel.setText("Disparos: " + shotsCounter);
        if (gameStarted) {
            playButton.setDisable(true);
            playButton.setText("EN JUEGO");
            // Ocultar flota seleccionable (ya estamos jugando)
            hideFleet();
        }

        boardVisualizer.restoreVisualShips(enemyBoardCells);

        boardVisualizer.drawPlayerShipsFromModel(this.boardCells);

        redrawBoardsFromState();

        System.out.println("Juego cargado exitosamente.");
        // 1. Restaurar visualmente mis barcos


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

    /**
     * helps the human interpret actions once triggered
     */
    private void handlePlayerShot(int col, int row) {
        Cell targetCell = enemyBoardCells[col][row];

        // 1. Validar si ya se disparó en esa celda
        if (targetCell.getState() == CellState.HIT ||
                targetCell.getState() == CellState.MISSED_SHOT ||
                targetCell.getState() == CellState.SUNK) {
            System.out.println("¡Ya disparaste aquí!");
            return;
        }

        boolean hit = (targetCell.getOccupyingShip() != null);

        // 2. Actualizar el Modelo (Lógica de impacto)
        // --- SECCIÓN CORREGIDA ---
        if (hit) {
            targetCell.setState(CellState.HIT);
            targetCell.getOccupyingShip().receiveShot();
            System.out.println("¡TOCADO!");

            if (targetCell.getOccupyingShip().isSunk()) {
                targetCell.setState(CellState.SUNK);
                System.out.println("¡HUNDIDO!");

                // 1. PRIMERO: Dibujamos la bomba del disparo actual
                boardVisualizer.drawShotResult(enemyShipsPane, col, row, true);

                // 2. SEGUNDO: Dibujamos el fuego sobre el barco (incluida la bomba recién puesta)
                boardVisualizer.markShipAsSunk(enemyShipsPane, enemyBoardCells, targetCell.getOccupyingShip());

                //enemyShipsSunkCount++;
                checkWinCondition();
            } else {
                // Es un toque, pero no se hundió. Solo dibujamos la bomba.
                boardVisualizer.drawShotResult(enemyShipsPane, col, row, true);
            }

        } else {
            // Es Agua.
            targetCell.setState(com.example.battleship.models.CellState.MISSED_SHOT);
            System.out.println("AGUA.");
            // Dibujamos la X de agua
            boardVisualizer.drawShotResult(enemyShipsPane, col, row, false);
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

                Ship targetShip = target.getOccupyingShip();
                boolean hit = (target.getOccupyingShip() != null);

                if (hit) {
                    target.setState(CellState.HIT);
                    targetShip.receiveShot();
                    boardVisualizer.drawShotResult(shipsPane, col, row, true);

                    if (targetShip.isSunk()) {
                        target.setState(CellState.SUNK);

                        // --- CAMBIO 3: Aumentar contador enemigo y verificar derrota ---
                        boardVisualizer.markShipAsSunk(shipsPane, boardCells, targetShip);

                        playerShipsSunkCount++;
                        checkWinCondition();

                        // Si el juego termina aquí, salimos del bucle
                        if (!gameStarted) return;
                    }
                } else {
                    target.setState(CellState.MISSED_SHOT);
                    boardVisualizer.drawShotResult(shipsPane, col, row, false);
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

    /**
     * Cuenta REALMENTE cuántos barcos únicos están hundidos en el tablero.
     * Usa un Set para no contar el mismo barco dos veces.
     */
    private int countActualSunkShips(Cell[][] board) {
        java.util.Set<Ship> sunkShips = new java.util.HashSet<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Ship ship = board[i][j].getOccupyingShip();
                // Si hay barco y está hundido, lo añadimos al conjunto
                if (ship != null && ship.isSunk()) {
                    sunkShips.add(ship);
                }
            }
        }
        // El tamaño del conjunto es la cantidad exacta de barcos muertos
        return sunkShips.size();
    }

    public void notifyShipPlaced() {
        shipsPlacedCount++;
        checkStartButtonState();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public Cell[][] getBoardCells() {
        return boardCells;
    }

    /**
     * Revisa si ya se colocaron todos los barcos para habilitar el botón de Jugar.
     */
    public void checkStartButtonState() {
        // Solo habilita si hay 10 barcos y el juego no ha empezado
        if (shipsPlacedCount >= TOTAL_SHIPS && !gameStarted) {
            if (playButton != null) {
                playButton.setDisable(false);
                playButton.setText("INICIAR JUEGO");
            }
        }
    }
}