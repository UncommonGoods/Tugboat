package com.uncommongoods.tugboat.engine.exception;

public class EndOfPaginationException extends TugboatException {
    public EndOfPaginationException(String message) {
        super(message);
    }

    public EndOfPaginationException(String message, Throwable cause) {
        super(message, cause);
    }
}
