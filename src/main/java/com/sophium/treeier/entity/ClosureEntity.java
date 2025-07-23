package com.sophium.treeier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table
@IdClass(ClosureEntity.ClosureId.class)
public class ClosureEntity {

    @Id
    @Column(name = "ancestor")
    private Long ancestor;

    @Id
    @Column(name = "descendant")
    private Long descendant;

    @Column(name = "depth")
    private Integer depth;

    @Column(name = "parent")
    private Long parent;

    @Column(name = "root")
    private Long root;

    @ManyToOne
    @JoinColumn(name = "ancestor", insertable = false, updatable = false)
    private NodeEntity ancestorNode;

    @ManyToOne
    @JoinColumn(name = "descendant", insertable = false, updatable = false)
    private NodeEntity descendantNode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosureId implements Serializable {
        private Long ancestor;
        private Long descendant;
    }
}
