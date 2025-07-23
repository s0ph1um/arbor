package com.sophium.treeier.request;

import com.sophium.treeier.entity.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTreeNodeDto {
    private Long parentId;
    private String title;
    private String description;
    private NodeType nodeType = NodeType.DEFAULT;
    private Boolean flagValue;
    private String linkValue;

}
