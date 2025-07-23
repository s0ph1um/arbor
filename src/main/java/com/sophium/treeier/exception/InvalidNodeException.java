package com.sophium.treeier.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class InvalidNodeException extends RuntimeException {
    public InvalidNodeException(String message) {
        super(message);
    }
}
