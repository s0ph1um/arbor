package com.sophium.treeier.controller;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.exception.CyclicalTreeStructureException;
import com.sophium.treeier.exception.InvalidNodeException;
import com.sophium.treeier.exception.MoveAttemptToSelfException;
import com.sophium.treeier.exception.NoSuchElementFoundException;
import com.sophium.treeier.exception.NodeExistsException;
import com.sophium.treeier.exception.RequiredFieldException;
import com.sophium.treeier.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sophium.treeier.util.Constants.NODE_NOT_FOUND;


@RestController
@RequestMapping("/api")
public class NodeController {

    @Autowired
    NodeService nodeService;

    private final Logger logger = LoggerFactory.getLogger(NodeController.class);

    @GetMapping("/node/{id}")
    public NodeDto getNode(@PathVariable("id") Long nodeId) {
        NodeDto node = nodeService.findById(nodeId);
        if (node == null) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }
        return node;
    }

    @GetMapping("/nodes/{rootNodeId}")
    public List<TreeNodeDto> findChildrenFromRoot(@PathVariable("rootNodeId") Long rootNodeId) {
        List<TreeNodeDto> node = nodeService.findAllNodesFromRoot(rootNodeId);
        if (node == null) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, rootNodeId));
        }
        return node;
    }

    @PutMapping("/node/{id}/move/{newParentId}")
    @ResponseStatus(HttpStatus.OK)
    public void moveNode(@PathVariable("id") Long nodeId,
                         @PathVariable("newParentId") Long newParentId) {
        nodeService.moveNode(nodeId, newParentId);
    }

    @DeleteMapping("/node/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> deleteNode(@PathVariable("id") Long nodeId) {
        return nodeService.deleteNodeAndDescendants(nodeId);
    }

    @ExceptionHandler(CyclicalTreeStructureException.class)
    public ResponseEntity<String> handleCyclicalTreeStructure(CyclicalTreeStructureException ex) {
        return new ResponseEntity<>("You may not move a node to one of its descendants",
            HttpStatus.LOOP_DETECTED);
    }

    @ExceptionHandler(InvalidNodeException.class)
    public ResponseEntity<String> handleInvalidNode(InvalidNodeException e) {
        return new ResponseEntity<>(String.format("The specified node %d does not exist", e.getNodeId()),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MoveAttemptToSelfException.class)
    public ResponseEntity<String> handleMoveToSelf(MoveAttemptToSelfException e) {
        return new ResponseEntity<>("You may not move a node to itself", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NodeExistsException.class)
    public ResponseEntity<String> handleNodeExists(NodeExistsException e) {
        return new ResponseEntity<>("A node with this ID already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RequiredFieldException.class)
    public ResponseEntity<String> handleRequiredField(RequiredFieldException e) {
        return new ResponseEntity<>(String.format("%s is a required field", e.getFieldName()),
            HttpStatus.BAD_REQUEST);
    }
}
