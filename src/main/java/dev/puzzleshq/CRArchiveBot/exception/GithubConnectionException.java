package dev.puzzleshq.CRArchiveBot.exception;

public class GithubConnectionException extends RuntimeException {

    public GithubConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubConnectionException(Throwable cause) {
        super("Failed to connect to Github", cause);
    }
}

