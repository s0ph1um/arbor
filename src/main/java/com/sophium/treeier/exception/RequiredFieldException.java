package com.sophium.treeier.exception;

import lombok.Getter;

@Getter
public class RequiredFieldException extends RuntimeException {
    public RequiredFieldException(String message) {
        super(message);
    }
}
