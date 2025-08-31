package com.sophium.treeier.dto;

import com.sophium.treeier.entity.NodeType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class NodeDto {

    @NotNull
    private Long id;

    private Long parentId;

    @NotNull
    private Long rootId;

    @Size(max = 255, message = "Node title is longer than 255 characters.")
    private String title;

    @Size(max = 255, message = "Node description is longer than 1000 characters.")
    private String description;

    private NodeType nodeType = NodeType.DEFAULT;

    private Boolean flagValue;

    private String linkValue;

    @Min(0)
    @Max(5)
    private Integer height = 0;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<CommentDto> comments;
    private List<NodeDto> children;

}
