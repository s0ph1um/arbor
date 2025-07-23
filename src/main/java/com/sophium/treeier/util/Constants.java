package com.sophium.treeier.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String TREE_NOT_FOUND = "Tree with id '%s' not found";
    public static final String NODE_NOT_FOUND = "The specified node %s does not exist";
    public static final String MAXIMUM_DEPTH_LIMIT_REACHED = "Maximum depth limit (5) reached";
    public static final String MAXIMUM_NODES_LIMIT_REACHED = "Maximum nodes limit (10000) reached";
    public static final String FAILED_TO_FIND_REQUESTED_ELEMENT = "Failed to find the requested element";
    public static final String CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF = "Cannot move node to a descendant of itself";
    public static final String MOVE_NODE_TO_ITSELF = "You may not move a node to itself";
    public static final String NODE_ID_ALREADY_EXISTS = "A node with this ID already exists";
    public static final String NODE_DOES_NOT_EXIST_IN_THIS_TREE = "The specified node %d does not exist in this tree";
    public static final String REQUIRED_FIELD = "%s is a required field";
    public static final String UNKNOWN_ERROR_OCCURRED = "Unknown error occurred";

}
