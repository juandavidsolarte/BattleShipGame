package com.example.battleship.views;

import javafx.scene.canvas.Canvas;

/**
 * Interface (Contract).
 **  Defines that any class implementing this MUST know how to
 ** draw a ship ("render"), regardless of whether it uses Canvas, images, or text.
 */
public interface ShipRenderer {

    /**
     * Abstract method that defines the drawing action.
     * @param canvas The JavaFX canvas where you paint.
     * @param size The size of the ship (number of cells).
     */
    void render(Canvas canvas, int size);

}