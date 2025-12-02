package com.example.battleship.exceptions;

/**
 * Exception thrown when a player attempts to make a prohibited move.
 */
public class InvalidMoveException extends Exception
{
    public InvalidMoveException(String message)
    {
        super(message);
    }
}