package com.sophium.treeier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class TreeStatisticsDto {
    private Long treeId;
    private Integer nodeCount;
    private Integer maxDepth;
    private Integer forksCount;
    private LocalDateTime lastModified;
    private Map<Integer, Integer> nodesPerLevel;
}
