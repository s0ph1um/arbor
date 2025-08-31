package com.sophium.treeier.mapper;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.entity.NodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface NodeMapper {

    @Mapping(source = "depth", target = "height", defaultValue = "0")
    NodeDto toDto(NodeEntity entity);

    List<NodeDto> toDtos(List<NodeEntity> entity);

    NodeEntity toEntity(NodeDto dto);

    NodeEntity treeNodeDtoToEntity(NodeDto dto);

}
