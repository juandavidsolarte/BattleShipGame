package com.example.battleship.persistence;

import java.io.*;

/**
 * Utility class for object serialization and deserialization.
 * We provide simple methods to save and restore Java objects to/from files,
 * enabling persistent game state storage across application sessions.
 */
public class Serializer
{
    /**
     * Serializes an object to a binary file.
     * We use this to persist complex game states for later restoration.
     *
     * @param fileName The target file path
     * @param object The object to serialize
     * @throws IOException If file operations fail
     */
    public static void serialize(String fileName, Object object) throws IOException
    {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName)))
        {
            out.writeObject(object);
        }
    }

    /**
     * Deserializes an object from a binary file.
     * We use this to restore previously saved game states.
     *
     * @param fileName The source file path
     * @return The deserialized object
     * @throws IOException If file operations fail
     * @throws ClassNotFoundException If the object class isn't available
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException
    {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName)))
        {
            return in.readObject();
        }
    }
}