package com.sophium.treeier.exception;

public class CyclicalTreeStructureException extends RuntimeException {
    public CyclicalTreeStructureException(String message) {
        super(message);
    }
}
