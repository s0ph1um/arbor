package com.sophium.treeier.mapper;

import com.sophium.treeier.dto.TreeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TreeMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "editorIds", expression = "java(mapEditorIds(tree.getEditors()))")
    @Mapping(target = "parentTreeId", source = "parentTree.id")
    @Mapping(target = "forkIds", expression = "java(mapForkIds(tree.getForks()))")
    TreeDto toDto(Tree tree);

    @Named("toDtoWithoutNodes")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "editorIds", expression = "java(mapEditorIds(tree.getEditors()))")
    @Mapping(target = "parentTreeId", source = "parentTree.id")
    @Mapping(target = "nodes", ignore = true)
    @Mapping(target = "forkIds", expression = "java(mapForkIds(tree.getForks()))")
    TreeDto toDtoWithoutNodes(Tree tree);

    default Set<Long> mapEditorIds(Set<User> editors) {
        return editors.stream()
            .map(User::getId)
            .collect(Collectors.toSet());
    }

    default List<Long> mapForkIds(List<Tree> forks) {
        return forks.stream()
            .map(Tree::getId)
            .toList();
    }

    default TreeDto toDtoWithNodes(Tree tree, List<TreeNodeDto> nodes) {
        TreeDto dto = toDto(tree);
        dto.setNodes(nodes);
        return dto;
    }
}
