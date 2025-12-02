package com.example.battleship.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Bounds;

import com.example.battleship.model.CellState;
import com.example.battleship.model.Game;

public class BatallaNavalController {

    // --------- IMÁGENES --------------


    @FXML private GridPane playerBoard;
    @FXML private GridPane enemyBoard;
    @FXML private Pane shipsPane;

    @FXML private Label turnLabel;
    @FXML private Label shotsLabel;
    @FXML private Label playerShipsLabel;
    @FXML private Label enemyShipsLabel;

    private Game game;

    // Control de rotación
    private boolean isHorizontal = true;

    // --------------------------- INIT ---------------------------
    @FXML
    public void initialize() {


    }





    // =======================================================================
    //                    LÓGICA ORIGINAL DEL JUEGO
    // =======================================================================

    private void createCells(GridPane board, boolean readOnly) {
        board.getChildren().clear();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {

                StackPane cell = new StackPane();
                cell.getStyleClass().add("cell");
                cell.setPrefSize(70, 70);

                if (!readOnly) {
                    int finalCol = col;
                    int finalRow = row;

                    cell.setOnMouseClicked(e -> {
                        if (game.canPlayerShoot()) {
                            boolean hit = game.playerShoot(finalRow, finalCol);
                            updateCell(enemyBoard, finalRow, finalCol,
                                    hit ? CellState.HIT : CellState.MISS);
                            updateUI();

                            if (game.isGameActive()) {
                                game.machinePlay();
                                refreshPlayerBoard();
                                updateUI();
                            }
                        }
                    });
                }

                board.add(cell, col, row);
            }
        }
    }

    private void updateCell(GridPane board, int row, int col, CellState state) {

        StackPane cell = (StackPane) board.getChildren().stream()
                .filter(n -> GridPane.getRowIndex(n) == row && GridPane.getColumnIndex(n) == col)
                .findFirst().orElse(null);

        if (cell == null) return;

        cell.getStyleClass().removeAll("hit", "miss", "ship", "sunk");
        cell.getChildren().removeIf(n -> n instanceof Circle);

        switch (state) {
            case HIT -> {
                cell.getStyleClass().add("hit");
                cell.getChildren().add(new Circle(8, Color.WHITE));
            }
            case MISS -> {
                cell.getStyleClass().add("miss");
                cell.getChildren().add(new Circle(8, Color.GRAY));
            }
            case SHIP -> cell.getStyleClass().add("ship");
            case SUNK -> {
                cell.getStyleClass().add("sunk");
                cell.getChildren().add(new Circle(8, Color.ORANGE));
            }
            default -> {}
        }
    }

    private void refreshPlayerBoard() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                updateCell(playerBoard, row, col, game.getPlayerCellState(row, col));
            }
        }
    }

    private void updateUI() {
        shotsLabel.setText("Disparos: " + game.getPlayerShots());
        playerShipsLabel.setText("Tus barcos: " + game.getPlayerRemainingShips() + "/10");
        enemyShipsLabel.setText("Enemigo: " + game.getEnemyRemainingShips() + "/10");
        turnLabel.setText(game.isPlayerTurn() ? "Estado: Tu turno" : "Estado: Turno enemigo...");
    }

    @FXML
    private void onNewGame() {
        game.reset();
        createCells(playerBoard, true);
        createCells(enemyBoard, false);
        updateUI();


    }
}