package com.library.exception;

/**
 * Thrown when a member has reached or exceeded their active borrowing quota.
 */
public class MemberLimitExceededException extends LibraryException {
    public MemberLimitExceededException(String message) {
        super(message);
    }
}
