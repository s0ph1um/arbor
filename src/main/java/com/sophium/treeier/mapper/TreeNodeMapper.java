package com.sophium.treeier.mapper;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.entity.NodeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TreeNodeMapper {

    TreeNodeDto toDto(NodeEntity entity);

    TreeNodeDto nodeDtoToTreeNodeDto(NodeDto nodeDto);

}
