package com.sophium.treeier.dto;

import com.sophium.treeier.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {
    private Long id;

    private User author;

    private String text;

}
