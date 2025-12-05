package com.example.battleship.views;

import javafx.scene.canvas.Canvas;

/**
 * Interface (Contrato).
 * Define que cualquier clase que implemente esto DEBE saber cómo
 * dibujar un barco ("render"), sin importar si usa Canvas, imágenes o texto.
 */
public interface ShipRenderer {

    /**
     * Metodo abstracto que define la acción de dibujar.
     * @param canvas El lienzo de JavaFX donde pintar.
     * @param size El tamaño del barco (número de celdas).
     */
    void render(Canvas canvas, int size);

}