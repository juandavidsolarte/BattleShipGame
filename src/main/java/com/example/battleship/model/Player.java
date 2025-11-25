package com.example.battleship.model;

import java.util.*;
import model.Ship;

public class Player {
    private static final int BOARD_SIZE = 10;

    private final CellState[][] board;
    private final List<Ship> ships;
    private boolean allShipsPlaced = false;

    public Player() {
        this.board = new CellState[BOARD_SIZE][BOARD_SIZE];
        this.ships = new ArrayList<>();
        clearBoard();
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], CellState.WATER);
        }
        ships.clear();
        allShipsPlaced = false;
    }

    // Define la flota: 1×4, 2×3, 3×2, 4×1 → total 10 barcos
    public List<int[]> getShipSizes() {
        return Arrays.asList(
                new int[]{4},                          // 1 portaaviones
                new int[]{3}, new int[]{3},          // 2 submarinos
                new int[]{2}, new int[]{2}, new int[]{2}, // 3 destructores
                new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1} // 4 fragatas
        );
    }

    // Intenta colocar un barco
    // Retorna `true` si se colocó correctamente
    public boolean placeShip(int row, int col, int size, boolean isVertical) {
        if (!canPlaceShip(row, col, size, isVertical)) return false;

        Ship ship = new Ship("Barco", size);
        for (int i = 0; i < size; i++) {
            int r = isVertical ? row + i : row;
            int c = isVertical ? col : col + i;
            board[r][c] = CellState.SHIP;
            ship.addPosition(r, c);
        }
        ships.add(ship);
        return true;
    }

    // 🔍 Verifica si un barco cabe en la posición dada (sin salirse ni chocar)
    private boolean canPlaceShip(int startRow, int startCol, int size, boolean isVertical) {
        if (startRow < 0 || startCol < 0) return false;

        for (int i = 0; i < size; i++) {
            int r = isVertical ? startRow + i : startRow;
            int c = isVertical ? startCol : startCol + i;

            if (r >= BOARD_SIZE || c >= BOARD_SIZE) return false;
            if (board[r][c] != CellState.WATER) return false;

            // Verifica celdas adyacentes (incluyendo diagonales) para evitar contacto
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = r + dr;
                    int nc = c + dc;
                    if (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE) {
                        if (board[nr][nc] == CellState.SHIP) return false;
                    }
                }
            }
        }
        return true;
    }

    //  Coloca TODOS los barcos aleatoriamente (para la máquina)
    public void placeShipsRandomly() {
        clearBoard();
        Random rand = new Random();
        List<int[]> sizes = getShipSizes();

        for (int[] sizeInfo : sizes) {
            int size = sizeInfo[0];
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 100) {
                int row = rand.nextInt(BOARD_SIZE);
                int col = rand.nextInt(BOARD_SIZE);
                boolean vertical = rand.nextBoolean();
                placed = placeShip(row, col, size, vertical);
                attempts++;
            }
            // Si falla tras muchos intentos, reintenta con nuevo shuffle
            if (!placed) {
                placeShipsRandomly(); // recursivo (poco probable, pero seguro)
                return;
            }
        }
        allShipsPlaced = true;
    }

    // Recibe un disparo en (row, col)
    // Retorna `true` si fue un impacto (HIT o SUNK), `false` si MISS
    public boolean receiveShot(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false; // fuera del tablero → no cuenta (o puedes tirar excepción)
        }

        CellState current = board[row][col];
        if (current == CellState.HIT || current == CellState.MISS || current == CellState.SUNK) {
            return false; // ya disparado aquí
        }

        if (current == CellState.SHIP) {
            board[row][col] = CellState.HIT;

            // Busca qué barco fue golpeado y actualiza
            for (Ship ship : ships) {
                if (ship.occupies(row, col)) {
                    ship.hit();
                    if (ship.isSunk()) {
                        // Marcar todas las posiciones del barco como SUNK
                        for (int[] pos : ship.getPositions()) {
                            board[pos[0]][pos[1]] = CellState.SUNK;
                        }
                    }
                    return true;
                }
            }
        } else {
            board[row][col] = CellState.MISS;
        }
        return false;
    }

    //  Obtiene el estado *visible* de una celda (para el tablero del jugador humano)
    // - En su propio tablero: muestra SHIP, HIT, MISS, SUNK
    // - En el tablero enemigo: solo WATER, HIT, MISS, SUNK (nunca SHIP)
    public CellState getCellState(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return CellState.WATER;
        }
        return board[row][col];
    }

    public int getRemainingShips() {
        int count = 0;
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasLost() {
        return getRemainingShips() == 0;
    }


}