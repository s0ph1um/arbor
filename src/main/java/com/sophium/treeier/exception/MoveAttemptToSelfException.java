package com.sophium.treeier.exception;

public class MoveAttemptToSelfException extends RuntimeException {
    public MoveAttemptToSelfException(String message) {
        super(message);
    }
}
