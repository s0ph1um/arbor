package com.sophium.treeier.repository;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.entity.ClosureEntity;
import com.sophium.treeier.entity.NodeEntity;
import com.sophium.treeier.exception.NoSuchElementFoundException;
import com.sophium.treeier.mapper.NodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import static com.sophium.treeier.util.Constants.NODE_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class NodeRepository {

    private static final int MAX_DEPTH = 5;

    private final NodeJpaRepository nodeJpaRepository;
    private final ClosureJpaRepository closureJpaRepository;
    private final NodeMapper nodeMapper;

    public NodeDto findById(long id) {
        NodeEntity node = nodeJpaRepository.findById(id).orElse(null);
        if (node == null) {
            return null;
        }
        return nodeMapper.toDto(node);
    }

    public NodeEntity findNodeEntityById(Long id) {
        return nodeJpaRepository.findById(id).orElse(null);
    }

    public NodeDto createNodesTableEntry(TreeNodeDto node) {
        NodeEntity entity = nodeMapper.treeNodeDtoToEntity(node);
        NodeEntity saved = nodeJpaRepository.save(entity);
        return findById(saved.getId());
    }

    public NodeEntity updateNode(NodeDto node) {
        NodeEntity entity = nodeJpaRepository.findById(node.getId()).orElse(null);
        if (entity != null) {
            entity.setParentId(node.getParentId());
            entity.setRootId(node.getRootId());
            entity.setTitle(node.getTitle());
            entity.setDescription(node.getDescription());
            entity.setDescription(node.getDescription());
            return nodeJpaRepository.save(entity);
        }
        throw new NoSuchElementFoundException(NODE_NOT_FOUND);
    }

    public void createChildrenTableEntry(NodeDto node) {
        ClosureEntity selfReference = new ClosureEntity();
        selfReference.setAncestor(node.getId());
        selfReference.setDescendant(node.getId());
        selfReference.setDepth(0);
        selfReference.setParent(node.getParentId());
        selfReference.setRoot(node.getRootId());

        closureJpaRepository.save(selfReference);
    }

    public void addNodeToParent(Long nodeId, Long parentId) {
        if (parentId > 0) {
            closureJpaRepository.addNodeToParent(nodeId, parentId);
        }
    }

    public void removeNodeFromParent(Long nodeId, Long parentId) {
        if (parentId > 0) {
            closureJpaRepository.removeNodeFromParent(nodeId, parentId);
        }
    }

    public boolean isDescendantOf(Long parentId, Long childId) {
        return closureJpaRepository.existsByAncestorAndDescendant(parentId, childId);
    }

    public List<NodeDto> findDirectChildren(Long nodeId) {
        return closureJpaRepository.findDirectChildren(nodeId);
    }

    public Long countDescendants(Long nodeId) {
        return closureJpaRepository.countDescendants(nodeId);
    }

    public boolean canAddChild(Long parentId) {
        Integer currentDepth = closureJpaRepository.findMaxDepth(parentId);
        return currentDepth == null || currentDepth < MAX_DEPTH - 1;
    }

    public Integer findMaxDepthInTree(Long rootId) {
        return closureJpaRepository.findMaxDepthInTree(rootId);
    }

    public Map<Integer, Integer> countNodesPerLevel(Long rootId) {
        List<Object[]> results = closureJpaRepository.countNodesPerLevel(rootId);
        Map<Integer, Integer> nodesPerLevel = new HashMap<>();

        for (Object[] result : results) {
            Integer level = ((Number) result[0]).intValue();
            Integer count = ((Number) result[1]).intValue();
            nodesPerLevel.put(level, count);
        }

        return nodesPerLevel;
    }

    public List<TreeNodeDto> findAllTreeNodes(Long rootId) {
        return closureJpaRepository.findAllTreeNodes(rootId);
    }

    @Transactional
    public List<Long> deleteNodeAndDescendants(Long nodeId) {
        Optional<NodeEntity> nodeToDelete = nodeJpaRepository.findById(nodeId);

        if (nodeToDelete.isEmpty()) {
            throw new NoSuchElementFoundException(String.format(NODE_NOT_FOUND, nodeId));
        }
        List<Long> nodesToDelete = closureJpaRepository.findDescendantIds(nodeId);
        nodesToDelete.add(nodeId);

        closureJpaRepository.deleteByDescendantIn(nodesToDelete);

        nodeJpaRepository.deleteAllById(nodesToDelete);
        return nodesToDelete;
    }

}
