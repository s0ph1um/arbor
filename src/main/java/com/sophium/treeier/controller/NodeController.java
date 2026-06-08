package com.sophium.treeier.controller;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.exception.NotFoundException;

import com.sophium.treeier.service.NodeService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class NodeController {

    private final NodeService nodeService;

    @GetMapping("/node/{id}")
    public NodeDto getNode(@PathVariable("id") Long nodeId) {
        NodeDto node = nodeService.findById(nodeId);
        if (node == null) {
            throw new NotFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }
        return node;
    }

    @GetMapping("/nodes/{rootNodeId}")
    public List<NodeDto> findChildrenFromRoot(@PathVariable("rootNodeId") Long rootNodeId) {
        List<NodeDto> node = nodeService.findAllNodesFromRoot(rootNodeId);
        if (node == null) {
            throw new NotFoundException(String.format(NODE_NOT_FOUND, rootNodeId));
        }
        return node;
    }

    @PutMapping("/tree/{treeId}/node/{nodeId}/move/{newParentId}")
    @ResponseStatus(HttpStatus.OK)
    public void moveNode(@PathVariable Long treeId,
                         @PathVariable Long nodeId,
                         @PathVariable Long newParentId) {
        nodeService.moveNode(treeId, nodeId, newParentId);
    }

    @DeleteMapping("/tree/{treeId}/node/{nodeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> deleteNode(@PathVariable Long treeId, @PathVariable Long nodeId) {
        return nodeService.deleteNodeAndDescendants(treeId, nodeId);
    }

}
