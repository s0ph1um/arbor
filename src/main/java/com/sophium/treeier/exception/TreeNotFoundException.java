package com.sophium.treeier.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TreeNotFoundException extends RuntimeException {
    private final long treeId;
}
