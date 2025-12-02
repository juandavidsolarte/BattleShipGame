package com.example.battleship.persistence;

import java.io.*;

/**
 * Utility class for serializing objects (HU-5 and HU-6).
 */
public class Serializer
{

    public static void serialize(String fileName, Object object) throws IOException
    {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName)))
        {
            out.writeObject(object);
        }
    }

    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException
    {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName)))
        {
            return in.readObject();
        }
    }
}