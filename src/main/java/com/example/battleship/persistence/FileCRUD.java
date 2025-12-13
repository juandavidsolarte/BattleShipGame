package com.example.battleship.persistence;

import java.io.*;
import java.util.ArrayList;

/**
 * Provides basic CRUD (Create-Read-Update-Delete) operations for text files.
 * We use this utility class to manage game logs and simple text-based persistence,
 * handling file I/O with proper error management.
 */
public class FileCRUD {

    String filePath;

    /**
     * Creates a new FileCRUD instance for the specified file.
     * We initialize with a file path to work with a specific data file.
     */
    public FileCRUD(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Appends new content to the end of the file.
     * We use append mode to preserve existing data while adding new entries.
     */
    public void create(String content) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all lines from the file into a list.
     * We return an empty list if the file doesn't exist or can't be read.
     */
    public ArrayList read() {
        ArrayList<String> list = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return list;
    }
}

