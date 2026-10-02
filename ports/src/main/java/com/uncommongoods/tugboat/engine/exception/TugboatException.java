package com.uncommongoods.tugboat.engine.exception;

public class TugboatException extends Exception {
    public TugboatException(String message) {
        super(message);
    }

    public TugboatException(String message, Throwable cause) {
        super(message, cause);
    }
}
