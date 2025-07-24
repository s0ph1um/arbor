package com.sophium.treeier.repository;

import com.sophium.treeier.entity.Tree;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface TreeJpaRepository extends JpaRepository<Tree, Long> {

    @Query("SELECT t FROM Tree t WHERE " +
        "EXISTS (SELECT 1 FROM t.labels l WHERE KEY(l) IN :keys AND VALUE(l) IN :values)")
    Page<Tree> findByLabels(@Param("keys") Set<String> keys,
                            @Param("values") Set<String> values,
                            Pageable pageable);

    default Page<Tree> findByLabels(Map<String, String> labels, Pageable pageable) {
        return findByLabels(labels.keySet(), new HashSet<>(labels.values()), pageable);
    }
}
