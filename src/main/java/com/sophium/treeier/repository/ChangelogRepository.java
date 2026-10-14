package com.sophium.treeier.repository;

import com.sophium.treeier.entity.ChangelogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChangelogRepository extends JpaRepository<ChangelogEntry, Long> {
    List<ChangelogEntry> findByTreeIdOrderByChangedAtDesc(Long treeId);

    Optional<ChangelogEntry> findTopByNodeIdOrderByChangedAtDesc(Long nodeId);
}
