package com.sophium.treeier.dto;

import com.sophium.treeier.entity.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TreeNodeDto {
    private Long id;
    private Long parentId;
    private Long rootId;
    private String title;
    private String description;
    private NodeType nodeType;
    private Boolean flagValue;
    private String linkValue;
    private Integer height;
    private List<TreeNodeDto> children;
    private NodeType type;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<CommentDto> comments;

    public TreeNodeDto(Long id, Long parentId, Long rootId, String title, String description,
                       NodeType nodeType, Boolean flagValue, String linkValue, Integer height) {
        this.id = id;
        this.parentId = parentId;
        this.rootId = rootId;
        this.title = title;
        this.description = description;
        this.nodeType = nodeType;
        this.flagValue = flagValue;
        this.linkValue = linkValue;
        this.height = height;
        this.children = new ArrayList<>();
    }
}
