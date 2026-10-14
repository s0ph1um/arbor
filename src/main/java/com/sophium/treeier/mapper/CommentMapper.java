package com.sophium.treeier.mapper;

import com.sophium.treeier.dto.CommentDto;
import com.sophium.treeier.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
    uses = {UserMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface CommentMapper {

    @Mapping(source = "author", target = "author")
    CommentDto toDto(Comment entity);

    List<CommentDto> toDtos(List<Comment> entity);

}

