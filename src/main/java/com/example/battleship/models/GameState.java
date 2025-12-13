package com.example.battleship.models;

import java.io.Serializable;

/**
 * Container class that holds the complete game state for saving and loading.
 * We implement Serializable to enable binary file persistence, capturing
 * everything needed to restore a game exactly where the player left off.
 */
public class GameState implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Cell[][] playerBoard;
    private Cell[][] enemyBoard;
    private String playerName;
    private int shotsCounter;
    private boolean isPlayerTurn;

    // Victory tracking - Counters
    private int enemyShipsSunkCount;
    private int playerShipsSunkCount;

    // Game status
    private boolean gameStarted;

    public GameState(Cell[][] playerBoard, Cell[][] enemyBoard, String playerName, int shotsCounter, boolean isPlayerTurn, int enemyShipsSunkCount,int playerShipsSunkCount,boolean gameStarted)
    {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.playerName = playerName;
        this.shotsCounter = shotsCounter;
        this.isPlayerTurn = isPlayerTurn;
        this.enemyShipsSunkCount = enemyShipsSunkCount;
        this.playerShipsSunkCount = playerShipsSunkCount;
        this.gameStarted = gameStarted;
    }

    // Getters
    public Cell[][] getPlayerBoard() { return playerBoard; }
    public Cell[][] getEnemyBoard() { return enemyBoard; }
    public String getPlayerName() { return playerName; }
    public int getShotsCounter() { return shotsCounter; }
    public boolean isPlayerTurn() { return isPlayerTurn; }
    public int getEnemyShipsSunkCount() { return enemyShipsSunkCount; }
    public int getPlayerShipsSunkCount() { return playerShipsSunkCount; }
    public boolean isGameStarted() { return gameStarted; }

    /**
     * Determines if the game has reached a conclusion.
     * We check if either player has sunk the required number of ships.
     */
    public boolean isGameOver() {
        return enemyShipsSunkCount >= 10 || playerShipsSunkCount >= 10;
    }
}