package com.sophium.treeier.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    // todo move other messages to constants
    public static final String TREE_NOT_FOUND = "Tree with id '%s' not found";
    public static final String NODE_NOT_FOUND = "The specified node %s does not exist";
}
