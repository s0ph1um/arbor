package com.sophium.treeier.controller;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.exception.NoSuchElementFoundException;

import com.sophium.treeier.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/node/{id}")
    public NodeDto getNode(@PathVariable("id") Long nodeId) {
        NodeDto node = nodeService.findById(nodeId);
        if (node == null) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }
        return node;
    }

    @GetMapping("/nodes/{rootNodeId}")
    public List<NodeDto> findChildrenFromRoot(@PathVariable("rootNodeId") Long rootNodeId) {
        List<NodeDto> node = nodeService.findAllNodesFromRoot(rootNodeId);
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

}
