package com.example.battleship.models;

import java.io.Serializable;

/**
 * Represents a ship in the game with its size, name, and damage state.
 * We track hits received and determine when the ship is fully destroyed.
 */
public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int size;
    private final String name;
    // Number of hits received

    private int hits;

    public Ship(int size, String name) {
        this.size = size;
        this.name = name;
        this.hits = 0;
    }

    public void receiveShot() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}