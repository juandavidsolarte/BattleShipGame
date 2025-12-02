package com.example.battleship.models;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;
    private int size;
    private String name;
    private int hits; // Cantidad de impactos recibidos

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