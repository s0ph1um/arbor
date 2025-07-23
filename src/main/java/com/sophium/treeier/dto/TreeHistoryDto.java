package com.sophium.treeier.dto;

import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TreeHistoryDto {

    private Long id;

    private Tree tree;

    private User author;

    private String changes;

    private LocalDateTime timestamp;

}
