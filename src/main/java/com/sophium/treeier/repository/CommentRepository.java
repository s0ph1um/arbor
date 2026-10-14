package com.sophium.treeier.repository;

import com.sophium.treeier.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.node.id = :nodeId AND c.changelogEntryId = :changelogEntryId ORDER BY c.createdAt DESC")
    List<Comment> findByNode_IdAndChangelogEntryIdOrderByCreatedAtDesc(
        @Param("nodeId") Long nodeId,
        @Param("changelogEntryId") Long changelogEntryId
    );

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.node.id = :nodeId AND c.changelogEntryId IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByNode_IdAndChangelogEntryIdIsNullOrderByCreatedAtDesc(
        @Param("nodeId") Long nodeId
    );
}

