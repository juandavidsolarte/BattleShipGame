package com.example.battleship.controllers;

//  VISTA
import com.example.battleship.views.CanvasShipRenderer;
import com.example.battleship.views.ShipRenderer;

//  MODELOS
import com.example.battleship.models.Cell;
import com.example.battleship.models.Ship;
import com.example.battleship.models.CellState;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import java.util.ResourceBundle;

public class GameController implements Initializable {

    // --- Elementos del Layout  ---
    @FXML private Pane shipsPane;
    @FXML private GridPane playerBoard;
    @FXML private GridPane enemyBoard;

    // --- Canvas de los Barcos ---
    @FXML private Canvas carrierCanvas;
    @FXML private Canvas submarineCanvas1, submarineCanvas2;
    @FXML private Canvas destroyerCanvas1, destroyerCanvas2, destroyerCanvas3;
    @FXML private Canvas frigateCanvas1, frigateCanvas2, frigateCanvas3, frigateCanvas4;

    // --- Etiquetas Informativas ---
    @FXML private Label turnLabel;
    @FXML private Label shotsLabel;

    // --- Instancia del Renderer (VISTA) ---
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    // --- Lógica del Juego (MODELO) ---
    private final Cell[][] boardCells = new Cell[10][10];

    // Elemento visual para el resaltado (Highlight)
    private final Rectangle selectionHighlight = new Rectangle();

    private boolean isHorizontal = true; // Orientación por defecto

    // Tamaño de cada celda en la cuadrícula (40px)
    private final double cellSize = 40.0;

    // --- Datos del Jugador ---
    private String playerName = "Player"; //

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (turnLabel != null) turnLabel.setText("Turn: Player");
        if (shotsLabel != null) shotsLabel.setText("Shots: 0");

        // 1. Inicializar Modelo de Datos
        initializeDataModel();

        // 2. Dibujar la cuadrícula visualmente usando Canvas
        drawBoardGrid();

        // 3. Configurar eventos de arrastre en el tablero
        setupBoardDragHandlers();

        // 4. Dibujar la flota en la paleta
        drawFleet();

        // 5. Configurar eventos de arrastre en los barcos origen
        setupDraggableShips();
    }

    /**
     * Public method for receiving the name from the welcome window.
     */
    public void setPlayerName(String name) {
        this.playerName = name;
        updateTurnLabel();
    }

    private void updateTurnLabel() {
        if (turnLabel != null) {
            turnLabel.setText("Turno: " + playerName);
        }
    }

    private void initializeDataModel() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardCells[i][j] = new Cell(i, j);
            }
        }
    }

    private void drawBoardGrid() {
        double boardSize = cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();

        gc.setStroke(Color.web("#FFFFFF", 0.3)); // Líneas blancas semitransparentes
        gc.setLineWidth(1.0);

        for (int i = 0; i <= 10; i++) {
            double pos = i * cellSize;
            gc.strokeLine(pos, 0, pos, boardSize);
            gc.strokeLine(0, pos, boardSize, pos);
        }

        shipsPane.getChildren().add(0, gridCanvas);

        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5);
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true);
        shipsPane.getChildren().add(selectionHighlight);
    }

    private void setupBoardDragHandlers() {
        shipsPane.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            if (event.getDragboard().hasString()) {
                try {
                    int shipSize = Integer.parseInt(event.getDragboard().getString());
                    int col = (int) (event.getX() / cellSize);
                    int row = (int) (event.getY() / cellSize);
                    updateHighlight(col, row, shipSize);
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
            event.consume();
        });

        shipsPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                try {
                    int shipSize = Integer.parseInt(db.getString());
                    int col = (int) (event.getX() / cellSize);
                    int row = (int) (event.getY() / cellSize);

                    if (isValidPlacement(col, row, shipSize, isHorizontal)) {
                        placeShipOnBoard(col, row, shipSize, isHorizontal);
                        success = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            selectionHighlight.setVisible(false);
            event.setDropCompleted(success);
            event.consume();
        });

        shipsPane.setOnDragExited(event -> {
            selectionHighlight.setVisible(false);
            event.consume();
        });

        shipsPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                isHorizontal = !isHorizontal;
                System.out.println("Orientación: " + (isHorizontal ? "Horizontal" : "Vertical"));
            }
        });
    }

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
        if (valid) {
            selectionHighlight.setFill(Color.rgb(0, 255, 0, 0.4));
        } else {
            selectionHighlight.setFill(Color.rgb(255, 0, 0, 0.4));
        }

        selectionHighlight.setVisible(true);
        selectionHighlight.toFront();
    }

    private boolean isValidPlacement(int x, int y, int size, boolean horizontal) {
        if (horizontal) {
            if (x + size > 10) return false;
        } else {
            if (y + size > 10) return false;
        }

        if (x < 0 || y < 0 || x >= 10 || y >= 10) return false;

        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;

            if (boardCells[targetX][targetY].getOccupyingShip() != null) {
                return false;
            }
        }
        return true;
    }

    private void placeShipOnBoard(int x, int y, int size, boolean horizontal) {
        String name;
        switch (size) {
            case 4: name = "Portaaviones"; break;
            case 3: name = "Submarino"; break;
            case 2: name = "Destructor"; break;
            case 1: name = "Fragata"; break;
            default: name = "Barco"; break;
        }

        // --- A. ACTUALIZAR MODELO ---
        Ship newShip = new Ship(size, name);

        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;
            Cell cell = boardCells[targetX][targetY];
            cell.setOccupyingShip(newShip);
        }

        // --- B. ACTUALIZAR VISTA ---
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

        // --- C. IMPRIMIR POSICIÓN (DEBUG) ---
        // Convertimos índices 0-9 a formato "A-1" para facilitar la lectura
        System.out.println("\n------------------------------------------------");
        System.out.println(" BARCO COLOCADO: " + name.toUpperCase());
        System.out.println("    Orientación: " + (horizontal ? "Horizontal" : "Vertical"));
        System.out.println("    Coordenadas ocupadas:");

        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;

            // Convertir X (0,1,2...) a Letra (A,B,C...)
            char columnaLetra = (char) ('A' + targetX);
            // Convertir Y (0,1,2...) a Número (1,2,3...)
            int filaNumero = targetY + 1;

            System.out.print("      [" + columnaLetra + "-" + filaNumero + "]");
        }
        System.out.println("\n------------------------------------------------");
    }

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

    private void makeDraggable(Canvas sourceCanvas, int size) {
        sourceCanvas.setOnDragDetected(event -> {
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