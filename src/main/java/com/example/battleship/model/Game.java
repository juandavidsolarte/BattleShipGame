package com.example.battleship.model;

// Game.java (resumen)
public class Game {
    private Player human;
    private Player machine;
    private boolean playerTurn = true;
    private int playerShots;

    public Game() {
        reset();
    }

    public void reset() {
        human = new Player();
        machine = new Player();
        playerTurn = true;
        playerShots = 0;

        // Colocar barcos de ambos jugadores
        machine.placeShipsRandomly();
        human.placeShipsRandomly(); // ← por ahora aleatorio; luego puedes hacerlo manual
    }

    // ---------- GETTERS ----------
    public boolean isPlayerTurn() { return playerTurn; }
    public int getPlayerShots() { return playerShots; }
    public int getPlayerRemainingShips() { return human.getRemainingShips(); }
    public int getEnemyRemainingShips() { return machine.getRemainingShips(); }

    public boolean isGameActive() {
        return !human.hasLost() && !machine.hasLost();
    }

    public CellState getPlayerCellState(int row, int col) {
        return human.getCellState(row, col);
    }

    public CellState getEnemyCellState(int row, int col) {
        // Para el tablero enemigo: ocultar SHIP, mostrar solo HIT/MISS/SUNK
        CellState state = machine.getCellState(row, col);
        if (state == CellState.SHIP) return CellState.WATER;
        return state;
    }

    // ---------- LÓGICA DE JUEGO ----------
    public boolean canPlayerShoot() {
        return playerTurn && isGameActive();
    }

    public boolean playerShoot(int row, int col) {
        boolean hit = machine.receiveShot(row, col);
        playerShots++;
        playerTurn = false;
        return hit;
    }

    public void machinePlay() {
        // IA simple: disparo aleatorio en celdas no usadas
        machineAIPlay();
        playerTurn = true;
    }

    private void machineAIPlay() {
        java.util.Random rand = new java.util.Random();
        int attempts = 0;
        while (attempts < 100) { // evitar bucle infinito
            int r = rand.nextInt(10);
            int c = rand.nextInt(10);
            CellState state = human.getCellState(r, c);
            if (state != CellState.HIT && state != CellState.MISS && state != CellState.SUNK) {
                human.receiveShot(r, c);
                return;
            }
            attempts++;
        }
        // Si no encuentra, dispara en la primera celda disponible
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellState state = human.getCellState(r, c);
                if (state != CellState.HIT && state != CellState.MISS && state != CellState.SUNK) {
                    human.receiveShot(r, c);
                    return;
                }
            }
        }
    }
}