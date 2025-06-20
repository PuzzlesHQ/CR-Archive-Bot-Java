package dev.puzzleshq.CRArchiveBot.exception;

public class PuzzlesHQAccessException extends RuntimeException {

    public PuzzlesHQAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public PuzzlesHQAccessException(Throwable cause) {
        super("Failed to access or find PuzzlesHQ org", cause);
    }
}

