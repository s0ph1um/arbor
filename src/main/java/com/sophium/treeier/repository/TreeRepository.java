package com.sophium.treeier.repository;

import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TreeRepository extends JpaRepository<Tree, Long> {

    @Query("SELECT t FROM Tree t WHERE t.owner = :user OR :user MEMBER OF t.editors")
    List<Tree> findAccessibleTrees(@Param("user") User user);

    @Query("SELECT t FROM Tree t WHERE t.deletedAt IS NOT NULL")
    List<Tree> findDeletedTrees();

    @Query("SELECT t FROM Tree t WHERE t.deletedAt IS NOT NULL")
    Page<Tree> findAllIncludingDeleted(Pageable pageable);

    @Query("SELECT t FROM Tree t WHERE " +
        "(:includeDeleted = true OR t.deletedAt IS NULL) AND " +
        "EXISTS (SELECT 1 FROM t.labels l WHERE KEY(l) IN :keys AND VALUE(l) IN :values)")
    Page<Tree> findByLabels(@Param("keys") Set<String> keys,
                            @Param("values") Set<String> values,
                            @Param("includeDeleted") boolean includeDeleted,
                            Pageable pageable);

    default Page<Tree> findByLabels(Map<String, String> labels, Pageable pageable, boolean includeDeleted) {
        return findByLabels(labels.keySet(), new HashSet<>(labels.values()), includeDeleted, pageable);
    }
}
