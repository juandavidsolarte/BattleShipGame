package com.example.battleship.controllers;


import com.example.battleship.views.CanvasShipRenderer;
import com.example.battleship.views.ShipRenderer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    // --- Elementos del Layout (FXML) ---
    @FXML
    private Pane shipsPane;

    @FXML
    private GridPane playerBoard;

    @FXML
    private GridPane enemyBoard;

    // --- Canvas de los Barcos (Vinculados al FXML) ---
    // Portaaviones (Tamaño 4)
    @FXML private Canvas carrierCanvas;

    // Submarinos (Tamaño 3)
    @FXML private Canvas submarineCanvas1;
    @FXML private Canvas submarineCanvas2;

    // Destructores (Tamaño 2)
    @FXML private Canvas destroyerCanvas1;
    @FXML private Canvas destroyerCanvas2;
    @FXML private Canvas destroyerCanvas3;

    // Fragatas (Tamaño 1)
    @FXML private Canvas frigateCanvas1;
    @FXML private Canvas frigateCanvas2;
    @FXML private Canvas frigateCanvas3;
    @FXML private Canvas frigateCanvas4;

    // --- Etiquetas Informativas ---
    @FXML private Label turnLabel;
    @FXML private Label shotsLabel;
    @FXML private Label playerShipsLabel;
    @FXML private Label enemyShipsLabel;

    // --- INSTANCIA DEL RENDERER ---
    // Creamos el objeto que sabe dibujar (CanvasShipRenderer)
    // Lo guardamos como la interfaz (ShipRenderer) para seguir el patrón Adapter.
    private final ShipRenderer shipRenderer = new CanvasShipRenderer();

    /**
     * Method that is automatically executed when the FXML view is loaded.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Configurar textos iniciales
        if (turnLabel != null) turnLabel.setText("Turno: Jugador");
        if (shotsLabel != null) shotsLabel.setText("Disparos: 0");

        // 2. LLAMADA AL METODO PARA DIBUJAR TODOS LOS BARCOS
        drawFleet();
    }

    /**
     * Auxiliary method for drawing the entire fleet using the Renderer.
     */
    private void drawFleet() {
        // Renderizamos el Portaaviones (4 celdas)
        // Verificamos si es null para evitar errores si el FXML no ha cargado bien
        if (carrierCanvas != null) shipRenderer.render(carrierCanvas, 4);

        // Renderizamos los Submarinos (3 celdas)
        if (submarineCanvas1 != null) shipRenderer.render(submarineCanvas1, 3);
        if (submarineCanvas2 != null) shipRenderer.render(submarineCanvas2, 3);

        // Renderizamos los Destructores (2 celdas)
        if (destroyerCanvas1 != null) shipRenderer.render(destroyerCanvas1, 2);
        if (destroyerCanvas2 != null) shipRenderer.render(destroyerCanvas2, 2);
        if (destroyerCanvas3 != null) shipRenderer.render(destroyerCanvas3, 2);

        // Renderizamos las Fragatas (1 celda)
        if (frigateCanvas1 != null) shipRenderer.render(frigateCanvas1, 1);
        if (frigateCanvas2 != null) shipRenderer.render(frigateCanvas2, 1);
        if (frigateCanvas3 != null) shipRenderer.render(frigateCanvas3, 1);
        if (frigateCanvas4 != null) shipRenderer.render(frigateCanvas4, 1);
    }
}