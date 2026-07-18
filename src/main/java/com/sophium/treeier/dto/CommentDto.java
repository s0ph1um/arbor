package com.sophium.treeier.dto;

import com.sophium.treeier.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;

    private User author;

    private String text;

    private LocalDateTime createdAt; // todo check mapper
}
