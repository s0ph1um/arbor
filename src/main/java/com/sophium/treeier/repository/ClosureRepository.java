package com.sophium.treeier.repository;

import com.sophium.treeier.entity.ClosureEntity;
import com.sophium.treeier.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosureRepository extends JpaRepository<ClosureEntity, ClosureEntity.ClosureId> {

        @Query("SELECT n " +
        "FROM NodeEntity n " +
        "LEFT JOIN ClosureEntity d ON d.ancestor = n.rootId AND d.descendant = n.id " +
        "WHERE n.parentId = :parentId AND d.ancestor != d.descendant " +
        "ORDER BY n.id")
    List<NodeEntity> findDirectChildren(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM ClosureEntity c " +
        "WHERE c.ancestor = :nodeId AND c.descendant != :nodeId")
    Long countDescendants(@Param("nodeId") Long nodeId);

    @Query("SELECT MAX(c.depth) FROM ClosureEntity c " +
        "WHERE c.descendant = :nodeId")
    Integer findMaxDepth(@Param("nodeId") Long nodeId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClosureEntity c " +
        "WHERE c.ancestor = :parentId AND c.descendant = :childId")
    boolean existsByAncestorAndDescendant(@Param("parentId") Long parentId, @Param("childId") Long childId);

    @Modifying
    @Query(value = "INSERT INTO closure_entity(ancestor, descendant, parent, root, depth) " +
        "SELECT p.ancestor, c.descendant, c.parent, p.root, p.depth+c.depth+1 " +
        "FROM closure_entity p, closure_entity c " +
        "WHERE p.descendant = :parentId AND c.ancestor = :childId",
        nativeQuery = true)
    void addNodeToParent(@Param("childId") Long childId, @Param("parentId") Long parentId);

    @Modifying
    @Query(value = "DELETE ce FROM closure_entity ce " +
        "INNER JOIN closure_entity p ON p.ancestor = ce.ancestor " +
        "INNER JOIN closure_entity c ON c.descendant = ce.descendant " +
        "WHERE p.descendant = :parentId AND c.ancestor = :childId",
        nativeQuery = true)
    void removeNodeFromParent(@Param("childId") Long childId, @Param("parentId") Long parentId);

    @Query("SELECT c.descendant FROM ClosureEntity c " +
        "WHERE c.ancestor = :nodeId AND c.descendant != :nodeId")
    List<Long> findDescendantIds(@Param("nodeId") Long nodeId);

    @Modifying
    @Query("DELETE FROM ClosureEntity c WHERE c.descendant IN :nodeIds")
    void deleteByDescendantIn(@Param("nodeIds") List<Long> nodeIds);

    @Query(value = "SELECT c.depth as level, COUNT(DISTINCT c.descendant) as count " +
        "FROM closure_entity c " +
        "WHERE c.ancestor = :rootId " +
        "GROUP BY c.depth " +
        "ORDER BY c.depth",
        nativeQuery = true)
    List<Object[]> countNodesPerLevel(@Param("rootId") Long rootId);

    @Query("SELECT n " +
        "FROM NodeEntity n " +
        "JOIN ClosureEntity c ON c.descendant = n.id " +
        "WHERE (c.ancestor = :rootId OR n.id = :rootId) AND c.ancestor != c.descendant " +
        "ORDER BY c.depth, n.id")
    List<NodeEntity> findAllTreeNodes(@Param("rootId") Long rootId);

    @Query("SELECT MAX(c.depth) FROM ClosureEntity c WHERE c.ancestor = :rootId")
    Integer findMaxDepthInTree(@Param("rootId") Long rootId);
}
