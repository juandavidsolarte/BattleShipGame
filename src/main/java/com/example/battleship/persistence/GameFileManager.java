package com.example.battleship.persistence;

import com.example.battleship.models.GameState;
import java.io.*;

/**
 * Manages saving in flat files and serialized files.
 */
public class GameFileManager
{

    private static final String SERIAL_FILE = "game_save.ser"; // Binary file (Boards)
    private static final String FLAT_FILE = "game_stats.txt";  // Plain file (Readable text)

    // Serialization - Save complete state
    public static void saveGame(GameState state)
    {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SERIAL_FILE)))
        {
            out.writeObject(state);
            System.out.println("Juego guardado automaticamente.");
        }
        catch (IOException e)
        {
            System.out.println("Error al guardar el juego: " + e.getMessage());
        }
    }

    public static GameState loadGame()
    {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SERIAL_FILE)))
        {
            return (GameState) in.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            return null;
        }
    }

    public static boolean hasSavedGame()
    {
        File file = new File(SERIAL_FILE);
        return file.exists();
    }

    // Flat files - Save simple record
    // Save: Nickname;ShipsSunk
    public static void saveTextLog(String nickname, int sunkenShips)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FLAT_FILE, true)))
        { // true = append (does not delete the previous)
            writer.write("Jugador: " + nickname + " | Barcos Hundidos: " + sunkenShips);
            writer.newLine();
        }
        catch (IOException e)
        {
            System.out.println("Error al escribir archivo plano: " + e.getMessage());
        }
    }
}