package com.sophium.treeier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeUpdateNotification {

    private Long treeId;
    private Long nodeId;
    private String nodeTitle;
    private OperationType operationType;
    private String authorEmail;
    private String authorName;
    private List<Long> deletedNodeIds;
    private Long oldParentId;
    private Long newParentId;
    private Instant timestamp;

}
