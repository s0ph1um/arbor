package com.sophium.treeier.service;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.exception.CyclicalTreeStructureException;
import com.sophium.treeier.exception.DepthLimitException;
import com.sophium.treeier.exception.MoveAttemptToSelfException;
import com.sophium.treeier.exception.NoSuchElementFoundException;
import com.sophium.treeier.exception.NodeAlreadyExistsException;
import com.sophium.treeier.mapper.NodeMapper;
import com.sophium.treeier.repository.NodeRepository;
import com.sophium.treeier.repository.TreeRepository;
import com.sophium.treeier.request.UpdateTreeNodeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sophium.treeier.util.Constants.CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF;
import static com.sophium.treeier.util.Constants.MAXIMUM_NODES_LIMIT_REACHED;
import static com.sophium.treeier.util.Constants.MOVE_NODE_TO_ITSELF;
import static com.sophium.treeier.util.Constants.NODE_ID_ALREADY_EXISTS;
import static com.sophium.treeier.util.Constants.NODE_NOT_FOUND;
import static com.sophium.treeier.util.Constants.TREE_NOT_FOUND;

@Slf4j
@Service
public class NodeService {

    @Autowired
    NodeRepository nodeRepository;
    @Autowired
    TreeRepository treeRepository;
    @Autowired
    private NodeMapper nodeMapper;

    @Transactional
    @CacheEvict(value = {"nodes", "descendants"}, allEntries = true)
    public NodeDto createNode(TreeNodeDto node) {
        if (!Objects.isNull(node.getParentId()) && !nodeRepository.canAddChild(node.getParentId())) {
            throw new DepthLimitException(MAXIMUM_NODES_LIMIT_REACHED);
        }

        NodeDto existingNode = nodeRepository.findById(node.getId());
        if (existingNode != null) {
            throw new NodeAlreadyExistsException(NODE_ID_ALREADY_EXISTS);
        }

        NodeDto resultNode = nodeRepository.createNodesTableEntry(node);
        nodeRepository.createChildrenTableEntry(resultNode);
        if (!Objects.isNull(resultNode.getParentId())) {
            nodeRepository.addNodeToParent(resultNode.getId(), resultNode.getParentId());
        }

        return resultNode;
    }

    @Cacheable(value = "nodes", key = "#id")
    public NodeDto findById(Long id) {
        return nodeRepository.findById(id);
    }

    @Cacheable(value = "nodes", key = "#rootId")
    public List<TreeNodeDto> findAllNodesFromRoot(Long rootId) {
        return nodeRepository.findAllTreeNodes(rootId);
    }

    @Transactional
    public NodeDto updateNode(Long treeId, Long nodeId, UpdateTreeNodeDto updateNodeDto, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        NodeDto node = nodeRepository.findById(nodeId);
        if (node == null) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }

        node.setTitle(updateNodeDto.getTitle());
        node.setDescription(updateNodeDto.getDescription());
        node.setFlagValue(updateNodeDto.getFlagValue());
        node.setLinkValue(updateNodeDto.getLinkValue());

        return nodeMapper.toDto(nodeRepository.updateNode(node));
    }

    @Transactional
    public void moveNode(Long nodeId, Long newParentId) throws NoSuchElementFoundException, CyclicalTreeStructureException, MoveAttemptToSelfException {
        if (Objects.equals(nodeId, newParentId)) {
            throw new MoveAttemptToSelfException(MOVE_NODE_TO_ITSELF);
        }

        if (nodeRepository.isDescendantOf(nodeId, newParentId)) {
            throw new CyclicalTreeStructureException(CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF);
        }

        NodeDto node = nodeRepository.findById(nodeId);
        if (node == null) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }

        NodeDto newParent = nodeRepository.findById(newParentId);
        if (newParent == null) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, newParentId));
        }

        Long oldParentId = node.getParentId();
        node.setParentId(newParentId);
        node.setRootId(newParent.getRootId());

        nodeRepository.removeNodeFromParent(node.getId(), oldParentId);
        nodeRepository.updateNode(node);
        nodeRepository.addNodeToParent(nodeId, newParentId);
    }

    public List<Long> deleteNodeAndDescendants(Long nodeId) {
        log.info("Deleting node {} and its descendants", nodeId);
        List<Long> deletedNodes = nodeRepository.deleteNodeAndDescendants(nodeId);
        log.info("Deleted nodes: {}", deletedNodes);
        return deletedNodes;
    }
}
