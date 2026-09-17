package com.library.exception;

/**
 * Thrown when attempting to issue a book that has zero available copies.
 */
public class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
