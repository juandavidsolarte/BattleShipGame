package com.example.battleship.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import com.example.battleship.model.CellState;
import com.example.battleship.model.Game;

public class BatallaNavalController {

    // --------- IMÁGENES --------------
    @FXML private ImageView carrierImg;
    @FXML private ImageView submarine1Img;
    @FXML private ImageView submarine2Img;
    @FXML private ImageView destroyer1Img;
    @FXML private ImageView destroyer2Img;
    @FXML private ImageView destroyer3Img;
    @FXML private ImageView frigate1Img;
    @FXML private ImageView frigate2Img;
    @FXML private ImageView frigate3Img;
    @FXML private ImageView frigate4Img;

    @FXML private GridPane playerBoard;
    @FXML private GridPane enemyBoard;

    @FXML private Label turnLabel;
    @FXML private Label shotsLabel;
    @FXML private Label playerShipsLabel;
    @FXML private Label enemyShipsLabel;

    private Game game;

    // --------------------------- INIT ---------------------------
    @FXML
    public void initialize() {

        loadShipImages();

        Platform.runLater(() -> {
            scaleShipImage(carrierImg, 4, 0.8);
            scaleShipImage(submarine1Img, 3, 0.7);
            scaleShipImage(submarine2Img, 3, 0.7);
            scaleShipImage(destroyer1Img, 2, 0.85);
            scaleShipImage(destroyer2Img, 2, 0.85);
            scaleShipImage(destroyer3Img, 2, 0.85);
            scaleShipImage(frigate1Img, 1, 0.80);
            scaleShipImage(frigate2Img, 1, 0.80);
            scaleShipImage(frigate3Img, 1, 0.80);
            scaleShipImage(frigate4Img, 1, 0.80);

            // ⬅️ Hacerlos arrastrables
            makeShipDraggable(carrierImg, 4);
            makeShipDraggable(submarine1Img, 3);
            makeShipDraggable(submarine2Img, 3);
            makeShipDraggable(destroyer1Img, 2);
            makeShipDraggable(destroyer2Img, 2);
            makeShipDraggable(destroyer3Img, 2);
            makeShipDraggable(frigate1Img, 1);
            makeShipDraggable(frigate2Img, 1);
            makeShipDraggable(frigate3Img, 1);
            makeShipDraggable(frigate4Img, 1);
        });

        game = new Game();
        createCells(playerBoard, true);
        createCells(enemyBoard, false);
        updateUI();
    }

    private void loadShipImages() {
        carrierImg.setImage(load("/images/carrier.png"));
        submarine1Img.setImage(load("/images/submarine.png"));
        submarine2Img.setImage(load("/images/submarine.png"));
        destroyer1Img.setImage(load("/images/destroyer.png"));
        destroyer2Img.setImage(load("/images/destroyer.png"));
        destroyer3Img.setImage(load("/images/destroyer.png"));
        frigate1Img.setImage(load("/images/frigate.png"));
        frigate2Img.setImage(load("/images/frigate.png"));
        frigate3Img.setImage(load("/images/frigate.png"));
        frigate4Img.setImage(load("/images/frigate.png"));
    }

    private Image load(String path) {
        return new Image(getClass().getResource(path).toExternalForm());
    }

    // --------------------------- ESCALADO DE BARCOS ---------------------------
    private void scaleShipImage(ImageView imageView, int sizeInCells, double scaleFactor) {

        playerBoard.layout();

        double cellWidth  = playerBoard.getWidth() / 12;
        double cellHeight = playerBoard.getHeight() / 12;

        double shipHeight = (cellHeight * sizeInCells) * scaleFactor;
        double shipWidth  = (cellWidth * scaleFactor) * scaleFactor;

        imageView.setFitWidth(shipWidth);
        imageView.setFitHeight(shipHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
    }

    // =======================================================================
    //                      🚢 DRAG & DROP DE BARCOS
    // =======================================================================

    private static class Delta { double x, y; }

    private void makeShipDraggable(ImageView ship, int shipSize) {

        Delta drag = new Delta();

        ship.setOnMousePressed(event -> {
            drag.x = event.getSceneX() - ship.getLayoutX();
            drag.y = event.getSceneY() - ship.getLayoutY();
        });

        ship.setOnMouseDragged(event -> {
            ship.setLayoutX(event.getSceneX() - drag.x);
            ship.setLayoutY(event.getSceneY() - drag.y);
        });

        ship.setOnMouseReleased(event -> {
            tryPlaceShipOnGrid(ship, shipSize);
        });
    }

    private void tryPlaceShipOnGrid(ImageView ship, int size) {

        double cellW = playerBoard.getWidth() / 10;
        double cellH = playerBoard.getHeight() / 10;

        int col = (int)(ship.getLayoutX() / cellW);
        int row = (int)(ship.getLayoutY() / cellH);

        // fuera del tablero → volver atrás
        if (col < 0 || row < 0 || col >= 10 || row >= 10) {
            resetShipPosition(ship);
            return;
        }

        // se sale a la derecha por el tamaño
        if (row + size > 10) {
            resetShipPosition(ship);
            return;
        }

        // -------------------------------------------------
        // Colocar visualmente dentro del GridPane
        // -------------------------------------------------
        GridPane.setColumnIndex(ship, col);
        GridPane.setRowIndex(ship, row);

        ship.setLayoutX(0);
        ship.setLayoutY(0);
    }

    private void resetShipPosition(ImageView ship) {
        ship.setLayoutX(0);
        ship.setLayoutY(0);
    }

  //

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
