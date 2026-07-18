package com.sophium.treeier.entity;

import com.sophium.treeier.dto.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "changelog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tree_id", nullable = false)
    private Long treeId;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "node_title")
    private String nodeTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private OperationType operation;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;
}
