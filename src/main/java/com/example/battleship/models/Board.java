package com.example.battleship.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    // CHANGE: We use a 2D array as a standard grid.
    private Cell[][] cells;
    private List<Ship> ships;

    public Board() {
        // Initialize a 10x10 grid
        cells = new Cell[10][10];
        ships = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        // Fills the grid cell by cell
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    /**
     * Gets a cell based on coordinates (row, col) directly.
     * No need for complex math anymore.
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) return null;

        // Direct access to the grid
        return cells[row][col];
    }

    /**
     * Attempts to place a ship on the board.
     * @return true if placed successfully, false if it doesn't fit or there is a collision.
     */
    public boolean placeShip(Ship ship, int row, int col, boolean isHorizontal) {
        int size = ship.getSize();

        // 1. Check board limits
        if (isHorizontal) {
            if (col + size > 10) return false;
        } else {
            if (row + size > 10) return false;
        }

        // 2. Check for collision (no other ship must be there)
        if (isHorizontal) {
            for (int i = 0; i < size; i++) {
                if (getCell(row, col + i).getState() == CellState.SHIP) return false;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (getCell(row + i, col).getState() == CellState.SHIP) return false;
            }
        }

        // 3. If valid, place the ship
        if (isHorizontal) {
            for (int i = 0; i < size; i++) {
                getCell(row, col + i).setOccupyingShip(ship);
            }
        } else {
            for (int i = 0; i < size; i++) {
                getCell(row + i, col).setOccupyingShip(ship);
            }
        }

        ships.add(ship);
        return true;
    }

    /**
     * Processes a shot received at the given coordinates.
     */
    public CellState receiveShot(int row, int col) throws InvalidMoveException {
        Cell cell = getCell(row, col);

        if (cell.getState() == CellState.MISSED_SHOT ||
                cell.getState() == CellState.HIT ||
                cell.getState() == CellState.SUNK) {
            throw new InvalidMoveException("You already shot here (" + row + "," + col + ")");
        }

        if (cell.getState() == CellState.SHIP) {
            cell.setState(CellState.HIT);
            cell.getOccupyingShip().receiveShot();

            if (cell.getOccupyingShip().isSunk()) {
                cell.setState(CellState.SUNK);
                return CellState.SUNK;
            }
            return CellState.HIT;
        } else {
            cell.setState(CellState.MISSED_SHOT);
            return CellState.WATER;
        }
    }
}

}
