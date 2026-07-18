package com.sophium.treeier.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChangelogEntryDto {
    private Long id;
    private Long nodeId;
    private String nodeTitle;
    private OperationType operation;
    private String authorEmail;
    private String authorName;
    private Instant changedAt;
}
