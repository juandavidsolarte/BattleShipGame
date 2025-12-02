package com.example.battleship.models;

import java.io.Serializable;

/**
 * List the possible states of a cell on the board.
 */
public enum CellState implements Serializable {
    WATER, SHIP, HIT, SUNK, MISSED_SHOT
}