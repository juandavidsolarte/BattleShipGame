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
            Serializer.serialize(SERIAL_FILE, state);
            //out.writeObject(state);
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
            return (GameState) Serializer.deserialize(SERIAL_FILE); //in.readObject();
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

    public static void deleteSaveFile() {
        new File(SERIAL_FILE).delete();
    }

    // Flat files - Save simple record
    // Save: Nickname;ShipsSunk
    public static void saveTextLog(String nickname, int sunkenShips, String result) {
        // Se instancia la clase FileCRUD
        FileCRUD fileCrud = new FileCRUD(FLAT_FILE);

        // Se formatea el texto
        String record = "Jugador: " + nickname + " | Hundidos: " + sunkenShips + " | Resultado: " + result;

        // Metodo create de FileCRUD
        fileCrud.create(record);

        System.out.println("Registro guardado usando FileCRUD en: " + FLAT_FILE);
    }
}