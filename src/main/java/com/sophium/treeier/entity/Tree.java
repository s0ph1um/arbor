package com.sophium.treeier.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "trees", indexes = {
    @Index(name = "idx_tree_owner", columnList = "owner_id"),
    @Index(name = "idx_tree_deleted", columnList = "deleted_at"),
    @Index(name = "idx_tree_parent", columnList = "parent_tree_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Tree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "root_node_id", nullable = false)
    private Long rootNodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tree_labels",
        joinColumns = @JoinColumn(name = "tree_id")
    )
    @MapKeyColumn(name = "label_key", length = 15)
    @Column(name = "label_value", length = 15)
    @Builder.Default
    private Map<String, String> labels = new HashMap<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tree_editors",
        joinColumns = @JoinColumn(name = "tree_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_tree_editors_tree", columnList = "tree_id"),
            @Index(name = "idx_tree_editors_user", columnList = "user_id")
        }
    )
    @Builder.Default
    private Set<User> editors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tree_id")
//    @NotFound(action = NotFoundAction.IGNORE)
    private Tree parentTree;

    @OneToMany(mappedBy = "parentTree", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Tree> forks = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "node_count")
    @Builder.Default
    private Integer nodeCount = 0;

    @Column(name = "max_depth")
    @Builder.Default
    private Integer maxDepth = 0;

    public boolean isOwner(User user) {
        return owner != null && owner.equals(user);
    }

    public boolean canEdit(User user) {
        return isOwner(user) || editors.contains(user);
    }

    @Transient
    public boolean isParentDeleted() {
        return isForked() && parentTree.getDeletedAt() != null;
    }

    public boolean isForked() {
        return parentTree != null;
    }

    public void softDelete(String deletedByUser) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUser;
    }
}

