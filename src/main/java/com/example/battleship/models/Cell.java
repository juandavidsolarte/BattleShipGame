package com.example.battleship.models;

import java.io.Serializable;

public class Cell implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int row, col;
    private CellState state;
    private Ship occupyingShip; // Referencia al barco si existe, null si es agua

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
