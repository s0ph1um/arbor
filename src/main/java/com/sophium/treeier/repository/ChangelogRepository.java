package com.sophium.treeier.repository;

import com.sophium.treeier.entity.ChangelogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangelogRepository extends JpaRepository<ChangelogEntry, Long> {
    List<ChangelogEntry> findByTreeIdOrderByChangedAtDesc(Long treeId);
}
