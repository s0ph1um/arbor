package com.sophium.treeier.service;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.TreeDto;
import com.sophium.treeier.dto.TreeStatisticsDto;
import com.sophium.treeier.dto.UpdateTreeDto;
import com.sophium.treeier.entity.NodeEntity;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.exception.DepthLimitException;
import com.sophium.treeier.exception.InvalidNodeException;
import com.sophium.treeier.exception.NoSuchElementFoundException;
import com.sophium.treeier.exception.NodeLimitException;
import com.sophium.treeier.exception.TreeNotFoundException;
import com.sophium.treeier.mapper.TreeMapper;
import com.sophium.treeier.mapper.TreeNodeMapper;
import com.sophium.treeier.repository.NodeRepository;
import com.sophium.treeier.repository.TreeJpaRepository;
import com.sophium.treeier.repository.UserRepository;
import com.sophium.treeier.request.CreateTreeDto;
import com.sophium.treeier.request.CreateTreeNodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserEmail;
import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserName;
import static com.sophium.treeier.util.Constants.MAXIMUM_DEPTH_LIMIT_REACHED;
import static com.sophium.treeier.util.Constants.MAXIMUM_NODES_LIMIT_REACHED;
import static com.sophium.treeier.util.Constants.NODE_DOES_NOT_EXIST_IN_THIS_TREE;
import static com.sophium.treeier.util.Constants.NODE_NOT_FOUND;
import static com.sophium.treeier.util.Constants.TREE_NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Transactional
public class TreeService {

    private static final int MAX_NODES = 10000;
    private static final int MAX_DEPTH = 5;

    private final TreeJpaRepository treeRepository;
    private final NodeService nodeService;
    private final NodeRepository nodeRepository;
    private final UserRepository userRepository;
    private final TreeMapper treeMapper;
    private final TreeNodeMapper treeNodeMapper;

    public TreeDto createTree(CreateTreeDto dto) {
        NodeDto rootNode = new NodeDto();
        rootNode.setId(generateNodeId());
        rootNode.setRootId(rootNode.getId());
        rootNode.setTitle(dto.getTitle());
        rootNode.setDescription(dto.getDescription());

        NodeDto createdRootNode = nodeService.createNode(rootNode);

        Tree tree = Tree.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .labels(dto.getLabels() != null ? dto.getLabels() : new HashMap<>())
            .rootNodeId(createdRootNode.getId())
            .nodeCount(1)
            .maxDepth(0)
            .build();

        User treeOwner = getTreeOwner(tree);
        tree.setOwner(treeOwner);

        Tree savedTree = treeRepository.save(tree);

        return treeMapper.toDto(savedTree);
    }

    public TreeDto forkTree(Long treeId, String newTitle) {
        Tree originalTree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        Map<Long, Long> idMapping = copyNodeStructure(originalTree.getRootNodeId());

        Tree forkedTree = Tree.builder()
            .title(newTitle != null ? newTitle : originalTree.getTitle() + " (Fork)")
            .description(originalTree.getDescription())
            .labels(new HashMap<>(originalTree.getLabels()))
            .parentTree(originalTree)
            .rootNodeId(idMapping.get(originalTree.getRootNodeId()))
            .nodeCount(originalTree.getNodeCount())
            .maxDepth(originalTree.getMaxDepth())
            .build();

        User treeOwner = getTreeOwner(forkedTree);
        forkedTree.setOwner(treeOwner);

        return treeMapper.toDto(treeRepository.save(forkedTree));
    }

    public TreeDto updateTree(Long treeId, UpdateTreeDto dto, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        if (dto.getTitle() != null) {
            tree.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            tree.setDescription(dto.getDescription());
        }
        if (dto.getLabels() != null) {
            tree.setLabels(dto.getLabels());
        }

        Tree saved = treeRepository.save(tree);
        return treeMapper.toDto(saved);
    }

    public void deleteTree(Long treeId, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new TreeNotFoundException(treeId));

        User user = currentUser.orElse(null);
        if (isNull(user) || !tree.isOwner(user)) {
            throw new AccessDeniedException("Only owner can delete tree");
        }

        tree.softDelete(user.getName());
        treeRepository.save(tree);
    }

    @Transactional(readOnly = true)
    public Page<TreeDto> getTrees(Pageable pageable, Map<String, String> labels) {
        Page<Tree> trees;

        if (labels != null && !labels.isEmpty()) {
            trees = treeRepository.findByLabels(labels, pageable);
        } else {
            trees = treeRepository.findAll(pageable);
        }

        return trees.map(treeMapper::toDtoWithoutNodes);
    }

    public void shareTree(Long treeId, List<Long> userIds, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.isOwner(currentUser.get())) {
            throw new AccessDeniedException("Only owner can share tree");
        }

        Set<User> editors = new HashSet<>(userRepository.findAllById(userIds));
        tree.getEditors().addAll(editors);

        treeRepository.save(tree);
    }

    public void removeEditor(Long treeId, Long userId, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.isOwner(currentUser.get())) {
            throw new AccessDeniedException("Only owner can manage editors");
        }

        tree.getEditors().removeIf(editor -> editor.getId().equals(userId));
        treeRepository.save(tree);
    }

    @Transactional(readOnly = true)
    public TreeDto getTreeWithNodes(Long treeId) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        List<NodeDto> nodes = loadTreeStructure(tree.getRootNodeId());

        return treeMapper.toDtoWithNodes(tree, nodes);
    }

    @Transactional(readOnly = true)
    public TreeDto getTreeWithoutNodes(Long treeId) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));
        return treeMapper.toDtoWithoutNodes(tree);
    }

    private List<NodeDto> loadTreeStructure(Long rootNodeId) {
        NodeDto rootNode = nodeService.findById(rootNodeId);
        loadChildrenRecursively(rootNode);
        return List.of(rootNode);
    }

    private void loadChildrenRecursively(NodeDto parent) {
        List<NodeEntity> directChildren = nodeRepository.findDirectChildren(parent.getId());

        List<NodeDto> children = directChildren.stream()
            .map(child -> {
                NodeDto childDto = treeNodeMapper.toDto(child);
                loadChildrenRecursively(childDto);
                return childDto;
            })
            .toList();

        parent.setChildren(children);
    }

    public TreeDto updateLabels(Long treeId, Map<String, String> labels, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        tree.getLabels().putAll(labels);
        Tree saved = treeRepository.save(tree);
        return treeMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public TreeStatisticsDto getTreeStatistics(Long treeId) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        Map<Integer, Integer> nodesPerLevel = nodeRepository.countNodesPerLevel(tree.getRootNodeId());

        return new TreeStatisticsDto(
            treeId,
            tree.getNodeCount(),
            tree.getMaxDepth(),
            tree.getForks().size(),
            tree.getUpdatedAt(),
            nodesPerLevel
        );
    }

    public NodeDto addNodeToTree(Tree tree, CreateTreeNodeDto createNodeDto, Optional<User> currentUser) {
        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        if (tree.getNodeCount() >= MAX_NODES) {
            throw new NodeLimitException(MAXIMUM_NODES_LIMIT_REACHED);
        }

        if (nonNull(createNodeDto.getParentId())) {
            NodeDto parentNode = nodeService.findById(createNodeDto.getParentId());
            if (isNull(parentNode)) {
                throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, createNodeDto.getParentId()));
            } else if (parentNode.getHeight() >= MAX_DEPTH - 1) {
                throw new DepthLimitException(MAXIMUM_DEPTH_LIMIT_REACHED);
            }
        }

        NodeDto newNode = new NodeDto();
        newNode.setId(generateNodeId());
        newNode.setParentId(createNodeDto.getParentId() != null ? createNodeDto.getParentId() : tree.getRootNodeId());
        newNode.setRootId(tree.getRootNodeId());
        newNode.setTitle(createNodeDto.getTitle());
        newNode.setDescription(createNodeDto.getDescription());
        newNode.setFlagValue(createNodeDto.getFlagValue());
        newNode.setNodeType(createNodeDto.getNodeType());
        newNode.setLinkValue(createNodeDto.getLinkValue());

        NodeDto created = nodeService.createNode(newNode);

        updateTreeStatistics(tree);

        return treeNodeMapper.nodeDtoToTreeNodeDto(created);
    }

    public void moveNodeWithinTree(Long treeId, Long nodeId, Long newParentId, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        NodeDto node = nodeService.findById(nodeId);
        if (node == null || !node.getRootId().equals(tree.getRootNodeId())) {
            throw new InvalidNodeException(String.format(NODE_DOES_NOT_EXIST_IN_THIS_TREE, nodeId));
        }

        nodeService.moveNode(nodeId, newParentId);
        updateTreeStatistics(tree);
    }

    private void updateTreeStatistics(Tree tree) {
        Integer nodeCount = nodeRepository.countDescendants(tree.getRootNodeId()).intValue() + 1;
        Integer maxDepth = nodeRepository.findMaxDepthInTree(tree.getRootNodeId());

        tree.setNodeCount(nodeCount);
        tree.setMaxDepth(maxDepth != null ? maxDepth : 0);
        treeRepository.save(tree);
    }

    private Map<Long, Long> copyNodeStructure(Long rootNodeId) {
        Map<Long, Long> oldToNewIdMapping = new HashMap<>();

        NodeDto rootNode = nodeService.findById(rootNodeId);
        List<NodeDto> allDescendants = nodeService.findAllNodesFromRoot(rootNodeId);

        NodeDto newRoot = new NodeDto();
        newRoot.setId(generateNodeId());
        newRoot.setRootId(newRoot.getId());
        newRoot.setTitle(rootNode.getTitle());
        newRoot.setHeight(0);

        NodeDto createdRoot = nodeService.createNode(newRoot);
        oldToNewIdMapping.put(rootNodeId, createdRoot.getId());

        allDescendants.sort(Comparator.comparing(NodeDto::getHeight));

        for (NodeDto descendant : allDescendants) {
            NodeDto newNode = new NodeDto();
            newNode.setId(generateNodeId());

            Long newParentId = oldToNewIdMapping.get(descendant.getParentId());
            newNode.setParentId(newParentId);
            newNode.setRootId(createdRoot.getId());
            newNode.setDescription(rootNode.getDescription());

            copyNodeDetails(descendant.getId(), newNode);

            NodeDto createdNode = nodeService.createNode(newNode);
            oldToNewIdMapping.put(descendant.getId(), createdNode.getId());
        }

        return oldToNewIdMapping;
    }

    private void copyNodeDetails(Long sourceNodeId, NodeDto targetNode) {
        NodeEntity sourceEntity = nodeRepository.findNodeEntityById(sourceNodeId);
        if (sourceEntity != null) {
            targetNode.setTitle(sourceEntity.getTitle());
            targetNode.setDescription(sourceEntity.getDescription());
            targetNode.setNodeType(sourceEntity.getNodeType());
            targetNode.setFlagValue(sourceEntity.getFlagValue());
            targetNode.setLinkValue(sourceEntity.getLinkValue());
        }
    }


    private User getTreeOwner(Tree tree) {
        String userId = getAuthenticatedUserEmail();
        String userName = getAuthenticatedUserName();
        Optional<User> existingUserOpt = userRepository.findByEmail(userId);

        return existingUserOpt
            .orElseGet(() -> userRepository.save(User.builder()
                .email(userId)
                .name(userName)
                .createdAt(LocalDateTime.now())
                .editableTrees(Set.of(tree))
                .build()));
    }

    private Long generateNodeId() {
        return System.currentTimeMillis() % Long.MAX_VALUE;
    }
}
