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

    // Contadores de victoria
    private int enemyShipsSunkCount;
    private int playerShipsSunkCount;

    // Estado del juego
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

    public boolean isGameOver() {
        // Si cualquiera de los dos ha hundido 10 barcos, el juego termina
        return enemyShipsSunkCount >= 10 || playerShipsSunkCount >= 10;
    }
}