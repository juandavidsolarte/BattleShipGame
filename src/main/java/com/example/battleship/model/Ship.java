package model;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final String name;
    private final int size;
    private final List<int[]> positions; // [fila, columna] — 0-indexed
    private int hits = 0;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.positions = new ArrayList<>();
    }

    public void addPosition(int row, int col) {
        positions.add(new int[]{row, col});
    }

    public boolean occupies(int row, int col) {
        return positions.stream().anyMatch(p -> p[0] == row && p[1] == col);
    }

    public void hit() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= size;
    }

    public boolean isHitAt(int row, int col) {
        return occupies(row, col) && hits > 0; // no distingue parcial/hundido aquí
    }

    public List<int[]> getPositions() {
        return new ArrayList<>(positions);
    }

    public int getSize() { return size; }
    public String getName() { return name; }
    public int getHits() { return hits; }
}