package com.example.battleship.persistence;

import com.example.battleship.models.GameState;
import java.io.*;

/**
 * Manages game data persistence through both binary serialization and text logging.
 * We provide two storage methods: binary files for complete game state restoration
 * and text files for human-readable game history and statistics.
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
            Serializer.serialize(SERIAL_FILE, state);
            //out.writeObject(state);
            System.out.println("Juego guardado automaticamente.");
        }
        catch (IOException e)
        {
            System.out.println("Error al guardar el juego: " + e.getMessage());
        }
    }

    /**
     * Loads a previously saved game state.
     * We attempt to restore from binary serialization, returning null if no save exists.
     */
    public static GameState loadGame()
    {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SERIAL_FILE)))
        {
            return (GameState) Serializer.deserialize(SERIAL_FILE); //in.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            return null;
        }
    }

    /**
     * Checks if a saved game exists on disk.
     * We verify the presence of the serialization file before attempting to load.
     */
    public static boolean hasSavedGame()
    {
        File file = new File(SERIAL_FILE);
        return file.exists();
    }

    /**
     * Deletes the saved game file.
     * We use this after game completion to ensure players start fresh next time.
     */
    public static void deleteSaveFile() {
        new File(SERIAL_FILE).delete();
    }

    /**
     * Saves a human-readable game result to the text log.
     * We use FileCRUD to append game statistics for historical tracking.
     */
    public static void saveTextLog(String nickname, int sunkenShips, String result) {
        FileCRUD fileCrud = new FileCRUD(FLAT_FILE);
        String record = "Jugador: " + nickname + " | Hundidos: " + sunkenShips + " | Resultado: " + result;
        fileCrud.create(record);
        System.out.println("Registro guardado usando FileCRUD en: " + FLAT_FILE);
    }
}