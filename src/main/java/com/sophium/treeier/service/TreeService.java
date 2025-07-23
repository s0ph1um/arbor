package com.sophium.treeier.service;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.TreeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.dto.TreeStatisticsDto;
import com.sophium.treeier.dto.UpdateTreeDto;
import com.sophium.treeier.entity.NodeEntity;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.exception.NoSuchElementFoundException;
import com.sophium.treeier.exception.TreeNotFoundException;
import com.sophium.treeier.mapper.TreeMapper;
import com.sophium.treeier.mapper.TreeNodeMapper;
import com.sophium.treeier.repository.NodeRepository;
import com.sophium.treeier.repository.TreeRepository;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserEmail;
import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserName;
import static com.sophium.treeier.util.Constants.TREE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class TreeService {

    private static final int MAX_NODES = 10000;
    private static final int MAX_DEPTH = 5;

    private final TreeRepository treeRepository;
    private final NodeService nodeService;
    private final NodeRepository nodeRepository;
    private final UserRepository userRepository;
    private final TreeMapper treeMapper;
    private final TreeNodeMapper treeNodeMapper;

    public TreeDto createTree(CreateTreeDto dto) {
        TreeNodeDto rootNode = new TreeNodeDto();
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

        if (dto.getNodes() != null && !dto.getNodes().isEmpty()) {
            for (CreateTreeNodeDto nodeDto : dto.getNodes()) {
                addNodeToTree(savedTree, nodeDto, Optional.of(treeOwner));
            }
        }
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
        if (Objects.isNull(user) || !tree.isOwner(user)) {
            throw new AccessDeniedException("Only owner can delete tree");
        }

        tree.softDelete(user.getName());
        treeRepository.save(tree);
    }

    @Transactional(readOnly = true)
    public Page<TreeDto> getAccessibleTrees(Optional<User> currentUser, Pageable pageable,
                                            Map<String, String> labels, boolean includeDeleted) {
        Page<Tree> trees;

        if (labels != null && !labels.isEmpty()) {
            trees = treeRepository.findByLabels(labels, pageable, includeDeleted);
        } else {
            trees = includeDeleted
                ? treeRepository.findAllIncludingDeleted(pageable)
                : treeRepository.findAll(pageable);
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

        List<TreeNodeDto> nodes = loadTreeStructure(tree.getRootNodeId());

        return treeMapper.toDtoWithNodes(tree, nodes);
    }

    private List<TreeNodeDto> loadTreeStructure(Long rootNodeId) {
        NodeDto rootNode = nodeService.findById(rootNodeId);
        TreeNodeDto rootDto = convertToTreeNodeDto(rootNode);

        loadChildrenRecursively(rootDto);

        return List.of(rootDto);
    }

    private void loadChildrenRecursively(TreeNodeDto parent) {
        List<NodeDto> directChildren = nodeRepository.findDirectChildren(parent.getId());

        List<TreeNodeDto> children = directChildren.stream()
            .map(child -> {
                TreeNodeDto childDto = treeNodeMapper.nodeDtoToTreeNodeDto(child);
                loadChildrenRecursively(childDto);
                return childDto;
            })
            .toList();

        parent.setChildren(children);
    }

    private TreeNodeDto convertToTreeNodeDto(NodeDto node) {
        NodeEntity entity = nodeRepository.findNodeEntityById(node.getId());

        TreeNodeDto dto = new TreeNodeDto();
        dto.setId(node.getId());
        dto.setParentId(node.getParentId());
        dto.setHeight(node.getHeight());

        if (entity != null) {
            dto.setTitle(entity.getTitle());
            dto.setDescription(entity.getDescription());
            dto.setNodeType(entity.getNodeType());
            dto.setFlagValue(entity.getFlagValue());
            dto.setLinkValue(entity.getLinkValue());
        }

        dto.setChildren(new ArrayList<>());
        return dto;
    }

    @Transactional(readOnly = true)
    public TreeDto getTreeWithoutNodes(Long treeId) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));
        return treeMapper.toDtoWithoutNodes(tree);
    }

    public TreeDto updateLabels(Long treeId, Map<String, String> labels, Optional<User> currentUser) {
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NoSuchElementFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        tree.setLabels(labels);
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

    public TreeNodeDto addNodeToTree(Tree tree, CreateTreeNodeDto createNodeDto, Optional<User> currentUser) {
        if (currentUser.isEmpty() || !tree.canEdit(currentUser.get())) {
            throw new AccessDeniedException("User cannot edit this tree");
        }

        if (tree.getNodeCount() >= MAX_NODES) {
            throw new RuntimeException("Maximum nodes limit reached: " + MAX_NODES);
        }

        if (createNodeDto.getParentId() != null && createNodeDto.getParentId() > 0) {
            NodeDto parentNode = nodeService.findById(createNodeDto.getParentId());
            if (parentNode != null && parentNode.getHeight() >= MAX_DEPTH - 1) {
                throw new RuntimeException("Maximum depth limit reached: " + MAX_DEPTH);
            }
        }

        TreeNodeDto newNode = new TreeNodeDto();
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
            throw new RuntimeException("Node does not belong to this tree");
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
        List<TreeNodeDto> allDescendants = nodeService.findAllNodesFromRoot(rootNodeId);

        TreeNodeDto newRoot = new TreeNodeDto();
        newRoot.setId(generateNodeId());
        newRoot.setRootId(newRoot.getId());
        newRoot.setTitle(rootNode.getTitle());
        newRoot.setHeight(0);

        NodeDto createdRoot = nodeService.createNode(newRoot);
        oldToNewIdMapping.put(rootNodeId, createdRoot.getId());

        // sort for proper creation order
        allDescendants.sort(Comparator.comparing(TreeNodeDto::getHeight));

        for (TreeNodeDto descendant : allDescendants) {
            TreeNodeDto newNode = new TreeNodeDto();
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

    private void copyNodeDetails(Long sourceNodeId, TreeNodeDto targetNode) {
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
        Optional<User> ownerOpt = userRepository.findByEmail(userId);

        if (ownerOpt.isEmpty()) {
            return userRepository.save(User.builder()
                .email(userId)
                .name(userName)
                .createdAt(LocalDateTime.now())
                .editableTrees(Set.of(tree))
                .build());
        } else {
            User user = ownerOpt.get();
            user.getEditableTrees().add(tree);
            return user;
        }
    }

    private Long generateNodeId() {
        return System.currentTimeMillis() % Long.MAX_VALUE;
    }
}
