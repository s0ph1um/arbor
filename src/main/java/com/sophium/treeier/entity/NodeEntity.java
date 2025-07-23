package com.sophium.treeier.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.util.List;

@Entity
@Table(name = "nodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeEntity {
    @Id
    private Long id;

    @Column(name = "parent")
    private Long parentId;

    @Column(name = "root")
    private Long rootId;

    @Column(length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type")
    private NodeType nodeType = NodeType.DEFAULT;

    @Column(name = "flag_value")
    private Boolean flagValue;

    @Column(name = "link_value")
    private String linkValue;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Formula("(SELECT c.depth FROM closure_entity c WHERE c.ancestor = root AND c.descendant = id)")
    private Integer depth;

}
