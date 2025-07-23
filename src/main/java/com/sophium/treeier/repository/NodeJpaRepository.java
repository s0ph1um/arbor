package com.sophium.treeier.repository;

import com.sophium.treeier.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeJpaRepository extends JpaRepository<NodeEntity, Long> {
}
