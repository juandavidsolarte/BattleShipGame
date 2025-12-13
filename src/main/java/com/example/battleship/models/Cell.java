package com.example.battleship.models;

import java.io.Serializable;

/**
 * Represents a single cell on the game board.
 * We track the cell's position, current state (water, ship, hit, etc.),
 * and any ship that occupies this cell. This class forms the foundation
 * of our game board model.
 */
public class Cell implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int row, col;
    private CellState state;
    // Ship reference if occupied, null if water
    private Ship occupyingShip;

    public Cell(int row, int col)
    {
        this.row = row;
        this.col = col;
        this.state = CellState.WATER;
        this.occupyingShip = null;
    }

    public CellState getState()
    {
        return state;
    }

    public void setState(CellState state)
    {
        this.state = state;
    }

    public Ship getOccupyingShip()
    {
        return occupyingShip;
    }

    public void setOccupyingShip(Ship ship)
    {
        this.occupyingShip = ship;
        this.state = CellState.SHIP;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
}
