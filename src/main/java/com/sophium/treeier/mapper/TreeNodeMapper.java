package com.sophium.treeier.mapper;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.entity.NodeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TreeNodeMapper {

    NodeDto toDto(NodeEntity entity);

    NodeDto nodeDtoToTreeNodeDto(NodeDto nodeDto);

}
