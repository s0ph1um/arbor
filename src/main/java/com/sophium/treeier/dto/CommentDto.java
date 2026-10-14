package com.sophium.treeier.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;

    private UserDto author;

    private String text;

    private LocalDateTime createdAt;
}
