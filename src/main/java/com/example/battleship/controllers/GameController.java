package com.example.battleship.controllers;

// Importaciones de VISTA (Renderizado)
import com.example.battleship.views.CanvasShipRenderer;
import com.example.battleship.views.ShipRenderer;

// Importaciones de MODELOS (Lógica de negocio)
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

    // --- Elementos del Layout (FXML) ---
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

    // Elemento visual para el resaltado (Highlight) en lugar de pintar celdas
    private final Rectangle selectionHighlight = new Rectangle();

    private boolean isHorizontal = true; // Orientación por defecto

    // Tamaño de cada celda en la cuadrícula.
    private final double cellSize = 40.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (turnLabel != null) turnLabel.setText("Turn: Player");
        if (shotsLabel != null) shotsLabel.setText("Shots: 0");

        // 1. Inicializar Modelo de Datos
        initializeDataModel();

        // 2. Dibujar la cuadrícula visualmente usando Canvas (sin GridPane)
        drawBoardGrid();

        // 3. Configurar eventos de arrastre en el tablero (shipsPane)
        setupBoardDragHandlers();

        // 4. Dibujar la flota en la paleta
        drawFleet();

        // 5. Configurar eventos de arrastre en los barcos origen
        setupDraggableShips();
    }

    /**
     * Initialize the logic array.
     */
    private void initializeDataModel() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardCells[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     *Draw the grid of the board using a Canvas and add it to the panel.
     * This replaces using GridPane for displaying lines.
     */
    private void drawBoardGrid() {
        double boardSize = cellSize * 10;
        Canvas gridCanvas = new Canvas(boardSize, boardSize);
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();

        gc.setStroke(Color.web("#FFFFFF", 0.3)); // Líneas blancas semitransparentes
        gc.setLineWidth(1.0);

        // Dibujar líneas verticales y horizontales
        for (int i = 0; i <= 10; i++) {
            double pos = i * cellSize;
            // Vertical
            gc.strokeLine(pos, 0, pos, boardSize);
            // Horizontal
            gc.strokeLine(0, pos, boardSize, pos);
        }

        // Agregar el canvas de la cuadrícula al fondo del shipsPane
        shipsPane.getChildren().add(0, gridCanvas);

        // Configurar el rectángulo de resaltado (inicialmente invisible)
        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5); // Bordes un poco redondeados, ajustado al nuevo tamaño
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true); // Ignorar clics
        shipsPane.getChildren().add(selectionHighlight);
    }

    /**
     * Configure drag and drop events directly on the shipspane.
     * We calculate the cell based on the mouse coordinates.
     */
    private void setupBoardDragHandlers() {
        // Permitir arrastre sobre el tablero
        shipsPane.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            // Lógica de Previsualización (Highlight) en tiempo real
            if (event.getDragboard().hasString()) {
                try {
                    int shipSize = Integer.parseInt(event.getDragboard().getString());

                    // Convertir coordenadas del mouse a columnas/filas
                    int col = (int) (event.getX() / cellSize);
                    int row = (int) (event.getY() / cellSize);

                    // Actualizar el rectángulo de selección
                    updateHighlight(col, row, shipSize);

                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
            event.consume();
        });

        // Al soltar el barco
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

            // Ocultar highlight al soltar
            selectionHighlight.setVisible(false);

            event.setDropCompleted(success);
            event.consume();
        });

        // Ocultar highlight si el mouse sale del tablero
        shipsPane.setOnDragExited(event -> {
            selectionHighlight.setVisible(false);
            event.consume();
        });

        // Rotación con clic derecho en cualquier parte del tablero
        shipsPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                isHorizontal = !isHorizontal;
                System.out.println("Orientación: " + (isHorizontal ? "Horizontal" : "Vertical"));
            }
        });
    }

    /**
     * Updates the position, size, and color of the preview rectangle.
     */
    private void updateHighlight(int col, int row, int size) {
        // Validar límites visuales (para no dibujar fuera del array)
        if (col < 0 || row < 0 || col >= 10 || row >= 10) {
            selectionHighlight.setVisible(false);
            return;
        }

        // Configurar dimensiones
        if (isHorizontal) {
            selectionHighlight.setWidth(size * cellSize);
            selectionHighlight.setHeight(cellSize);
        } else {
            selectionHighlight.setWidth(cellSize);
            selectionHighlight.setHeight(size * cellSize);
        }

        // Configurar posición
        selectionHighlight.setLayoutX(col * cellSize);
        selectionHighlight.setLayoutY(row * cellSize);

        // Validar lógica para el color
        boolean valid = isValidPlacement(col, row, size, isHorizontal);
        if (valid) {
            selectionHighlight.setFill(Color.rgb(0, 255, 0, 0.4)); // Verde semitransparente
        } else {
            selectionHighlight.setFill(Color.rgb(255, 0, 0, 0.4)); // Rojo semitransparente
        }

        selectionHighlight.setVisible(true);
        selectionHighlight.toFront(); // Asegurar que se vea sobre la cuadrícula
    }

    /**
     * Check on the MODEL if the ship fits and the cells are free.
     */
    private boolean isValidPlacement(int x, int y, int size, boolean horizontal) {
        // 1. Validar límites del tablero
        if (horizontal) {
            if (x + size > 10) return false;
        } else {
            if (y + size > 10) return false;
        }

        // Validar coordenadas negativas (fuera del canvas)
        if (x < 0 || y < 0 || x >= 10 || y >= 10) return false;

        // 2. Validar superposición
        for (int i = 0; i < size; i++) {
            int targetX = horizontal ? x + i : x;
            int targetY = horizontal ? y : y + i;

            if (boardCells[targetX][targetY].getOccupyingShip() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Place the ship logically and visually.
     */
    private void placeShipOnBoard(int x, int y, int size, boolean horizontal) {
        String name;
        switch (size) {
            case 4: name = "Carrier"; break;
            case 3: name = "Submarine"; break;
            case 2: name = "Destroyer"; break;
            case 1: name = "Frigate"; break;
            default: name = "Ship"; break;
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
        // Siempre dibujamos horizontal y rotamos si es necesario
        newShipCanvas.setWidth(size * cellSize);
        newShipCanvas.setHeight(cellSize);

        shipRenderer.render(newShipCanvas, size);

        if (horizontal) {
            newShipCanvas.setLayoutX(x * cellSize);
            newShipCanvas.setLayoutY(y * cellSize);
        } else {
            newShipCanvas.setRotate(90);
            // Corrección de pivote para rotación
            double offset = cellSize * (1 - size) / 2.0;
            newShipCanvas.setLayoutX((x * cellSize) + offset);
            newShipCanvas.setLayoutY((y * cellSize) - offset);
        }

        newShipCanvas.setMouseTransparent(true);
        shipsPane.getChildren().add(newShipCanvas);
    }

    /**
     * Configure drag events for the paddle ships.
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