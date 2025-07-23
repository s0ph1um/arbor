package com.sophium.treeier.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class TreeDto {

    private Long id;
    private String title;
    private String description;
    private Map<String, String> labels;
    private Long ownerId;
    private String ownerName;
    private Set<Long> editorIds;
    private Long parentTreeId;
    private List<Long> forkIds;
    private Integer nodeCount;
    private Integer maxDepth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TreeHistoryDto> history;

    private boolean deleted;

    private List<TreeNodeDto> nodes;
}
