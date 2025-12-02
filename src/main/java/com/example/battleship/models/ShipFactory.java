package com.example.battleship.models;

/**
 * Factory Method
 */
public class ShipFactory
{

    public static Ship createShip(String type)
    {
        if (type == null)
        {
            return null;
        }

        switch (type.toLowerCase())
        {
            case "carrier":      // Antes portaaviones
            case "portaaviones": // Soporte para ambos idiomas en el string
                return new Ship(4, "Carrier");
            case "submarine":
            case "submarino":
                return new Ship(3, "Submarine");
            case "destroyer":
            case "destructor":
                return new Ship(2, "Destroyer");
            case "frigate":
            case "fragata":
                return new Ship(1, "Frigate");
            default:
                throw new IllegalArgumentException("Unknown ship type: " + type);
        }
    }
}