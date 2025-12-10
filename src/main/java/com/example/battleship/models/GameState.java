package com.example.battleship.models;

import java.io.Serializable;

/**
 * Container class to store the entire game state.
 * Implements Serializable so it can be saved in binary.
 */
public class GameState implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Cell[][] playerBoard;
    private Cell[][] enemyBoard;
    private String playerName;
    private int shotsCounter;
    private boolean isPlayerTurn;

    public GameState(Cell[][] playerBoard, Cell[][] enemyBoard, String playerName, int shotsCounter, boolean isPlayerTurn)
    {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.playerName = playerName;
        this.shotsCounter = shotsCounter;
        this.isPlayerTurn = isPlayerTurn;
    }

    // Getters
    public Cell[][] getPlayerBoard() { return playerBoard; }
    public Cell[][] getEnemyBoard() { return enemyBoard; }
    public String getPlayerName() { return playerName; }
    public int getShotsCounter() { return shotsCounter; }
    public boolean isPlayerTurn() { return isPlayerTurn; }
}