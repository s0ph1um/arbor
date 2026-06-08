package com.sophium.treeier.service;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.OperationType;
import com.sophium.treeier.dto.TreeUpdateNotification;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.exception.CyclicalTreeStructureException;
import com.sophium.treeier.exception.DepthLimitException;
import com.sophium.treeier.exception.MoveAttemptToSelfException;
import com.sophium.treeier.exception.NotFoundException;
import com.sophium.treeier.exception.NodeAlreadyExistsException;
import com.sophium.treeier.mapper.NodeMapper;
import com.sophium.treeier.repository.NodeRepository;
import com.sophium.treeier.repository.TreeJpaRepository;
import com.sophium.treeier.request.UpdateTreeNodeDto;
import com.sophium.treeier.service.messaging.TreeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserEmail;
import static com.sophium.treeier.util.Constants.CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF;
import static com.sophium.treeier.util.Constants.MAXIMUM_NODES_LIMIT_REACHED;
import static com.sophium.treeier.util.Constants.MOVE_NODE_TO_ITSELF;
import static com.sophium.treeier.util.Constants.NODE_ID_ALREADY_EXISTS;
import static com.sophium.treeier.util.Constants.NODE_NOT_FOUND;
import static com.sophium.treeier.util.Constants.TREE_NOT_FOUND;
import static com.sophium.treeier.util.Constants.USER_CANNOT_EDIT_THIS_TREE;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final TreeJpaRepository treeRepository;
    private final NodeMapper nodeMapper;
    private final UserService userService;
    private final TreeNotificationService treeNotificationService;

    public NodeDto findById(Long id) {
        return nodeRepository.findById(id);
    }

    public List<NodeDto> findAllNodesFromRoot(Long rootId) {
        return nodeMapper.toDtos(nodeRepository.findAllTreeNodes(rootId));
    }

    @Transactional
    public NodeDto createNode(Long treeId, NodeDto node) {
        if (Objects.nonNull(node.getParentId()) && !nodeRepository.canAddChild(node.getParentId())) {
            throw new DepthLimitException(MAXIMUM_NODES_LIMIT_REACHED);
        }

        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        verifyCanEdit(treeId, currentUser);

        NodeDto existingNode = nodeRepository.findById(node.getId());
        if (Objects.nonNull(existingNode)) {
            throw new NodeAlreadyExistsException(NODE_ID_ALREADY_EXISTS);
        }

        NodeDto resultNode = nodeRepository.createNode(node);
        nodeRepository.createChildrenTableEntry(resultNode);
        if (Objects.nonNull(resultNode.getParentId())) {
            nodeRepository.addNodeToParent(resultNode.getId(), resultNode.getParentId());
        }

        treeNotificationService.notify(TreeUpdateNotification.builder()
            .treeId(treeId)
            .nodeId(node.getId())
            .nodeTitle(node.getTitle())
            .operationType(OperationType.NODE_CREATED)
            .authorEmail(currentUser.getEmail())
            .authorName(currentUser.getName())
            .timestamp(Instant.now())
            .build());

        return resultNode;
    }

    @Transactional
    public NodeDto updateNode(Long treeId, Long nodeId, UpdateTreeNodeDto updateNodeDto) {
        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        verifyCanEdit(treeId, currentUser);

        NodeDto updatedNode = nodeRepository.findById(nodeId);
        if (Objects.isNull(updatedNode)) {
            throw new NotFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }

        updatedNode.setTitle(updateNodeDto.getTitle());
        updatedNode.setDescription(updateNodeDto.getDescription());
        updatedNode.setFlagValue(updateNodeDto.getFlagValue());
        updatedNode.setLinkValue(updateNodeDto.getLinkValue());

        treeNotificationService.notify(TreeUpdateNotification.builder()
            .treeId(treeId)
            .nodeId(nodeId)
            .nodeTitle(updatedNode.getTitle())
            .operationType(OperationType.NODE_UPDATED)
            .authorEmail(currentUser.getEmail())
            .authorName(currentUser.getName())
            .timestamp(Instant.now())
            .build());

        return nodeMapper.toDto(nodeRepository.updateNode(updatedNode));
    }

    @Transactional
    public void moveNode(Long treeId, Long nodeId, Long newParentId) throws NotFoundException, CyclicalTreeStructureException, MoveAttemptToSelfException {
        if (Objects.equals(nodeId, newParentId)) {
            throw new MoveAttemptToSelfException(MOVE_NODE_TO_ITSELF);
        }

        if (nodeRepository.isDescendantOf(nodeId, newParentId)) {
            throw new CyclicalTreeStructureException(CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF);
        }

        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        verifyCanEdit(treeId, currentUser);

        NodeDto node = nodeRepository.findById(nodeId);
        if (Objects.isNull(node)) {
            throw new NotFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }

        NodeDto newParent = nodeRepository.findById(newParentId);
        if (Objects.isNull(newParent)) {
            throw new NotFoundException(String.format(NODE_NOT_FOUND, newParentId));
        }

        Long oldParentId = node.getParentId();
        node.setParentId(newParentId);
        node.setRootId(newParent.getRootId());

        nodeRepository.removeNodeFromParent(node.getId(), oldParentId);
        nodeRepository.updateNode(node);
        nodeRepository.addNodeToParent(nodeId, newParentId);

        treeNotificationService.notify(TreeUpdateNotification.builder()
            .treeId(treeId)
            .nodeId(nodeId)
            .nodeTitle(node.getTitle())
            .operationType(OperationType.NODE_MOVED)
            .oldParentId(oldParentId)
            .newParentId(newParentId)
            .authorEmail(currentUser.getEmail())
            .authorName(currentUser.getName())
            .timestamp(Instant.now())
            .build());
    }

    public List<Long> deleteNodeAndDescendants(Long treeId, Long nodeId) {
        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        verifyCanEdit(treeId, currentUser);

        log.info("Deleting node {} and its descendants", nodeId);
        List<Long> deletedNodes = nodeRepository.deleteNodeAndDescendants(nodeId);
        log.info("Deleted nodes: {}", deletedNodes);

        treeNotificationService.notify(TreeUpdateNotification.builder()
            .treeId(treeId)
            .nodeId(nodeId)
            .operationType(OperationType.NODE_DELETED)
            .authorEmail(currentUser.getEmail())
            .authorName(currentUser.getName())
            .deletedNodeIds(deletedNodes)
            .timestamp(Instant.now())
            .build());

        return deletedNodes;
    }

    private void verifyCanEdit(Long treeId, User currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NotFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (!tree.canEdit(currentUser)) {
            throw new AccessDeniedException(USER_CANNOT_EDIT_THIS_TREE);
        }
    }
}
