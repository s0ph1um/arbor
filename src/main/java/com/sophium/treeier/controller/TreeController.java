package com.sophium.treeier.controller;

import com.sophium.treeier.dto.NodeDto;
import com.sophium.treeier.dto.ShareTreeDto;
import com.sophium.treeier.dto.TreeDto;
import com.sophium.treeier.dto.TreeNodeDto;
import com.sophium.treeier.dto.TreeStatisticsDto;
import com.sophium.treeier.dto.UpdateTreeDto;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.repository.TreeRepository;
import com.sophium.treeier.repository.UserRepository;
import com.sophium.treeier.request.CreateTreeDto;
import com.sophium.treeier.request.CreateTreeNodeDto;
import com.sophium.treeier.request.UpdateTreeNodeDto;
import com.sophium.treeier.service.NodeService;
import com.sophium.treeier.service.TreeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserEmail;

@RestController
@RequestMapping("/api/trees")
@RequiredArgsConstructor
@Validated
public class TreeController {

    private final TreeService treeService;
    private final UserRepository userRepository;
    private final TreeRepository treeRepository;
    private final NodeService nodeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TreeDto> createTree(@Valid @RequestBody CreateTreeDto createDto) {
        TreeDto created = treeService.createTree(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreeDto> getTree(
        @PathVariable Long id,
        @RequestParam(defaultValue = "true") boolean includeNodes) {
        TreeDto tree = includeNodes
            ? treeService.getTreeWithNodes(id)
            : treeService.getTreeWithoutNodes(id);
        return ResponseEntity.ok(tree);
    }

    @GetMapping
    public ResponseEntity<Page<TreeDto>> getTrees(
        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) Map<String, String> labels,
        @RequestParam(defaultValue = "false") boolean includeDeleted) {
        Optional<User> currentUser = userRepository.findByEmail(getAuthenticatedUserEmail());

        Page<TreeDto> trees = treeService.getAccessibleTrees(currentUser, pageable, labels, includeDeleted);
        return ResponseEntity.ok(trees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreeDto> updateTree(@PathVariable Long id, @Valid @RequestBody UpdateTreeDto updateTreeDto) {
        Optional<User> currentUser = userRepository.findByEmail(getAuthenticatedUserEmail());

        TreeDto updated = treeService.updateTree(id, updateTreeDto, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTree(@PathVariable Long id) {
        Optional<User> currentUser = userRepository.findByEmail(getAuthenticatedUserEmail());

        treeService.deleteTree(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/fork")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TreeDto> forkTree(
        @PathVariable Long id,
        @RequestParam(required = false) String title) {
        TreeDto forked = treeService.forkTree(id, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(forked);
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<Void> shareTree(
        @PathVariable Long id,
        @Valid @RequestBody ShareTreeDto shareDto) {
        Optional<User> currentUser = userRepository.findByEmail(getAuthenticatedUserEmail());
        treeService.shareTree(id, shareDto.getUserIds(), currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/editors/{userId}")
    public ResponseEntity<Void> removeEditor(
        @PathVariable Long id,
        @PathVariable Long userId) {
        Optional<User> currentUser = userRepository.findByEmail(getAuthenticatedUserEmail());

        treeService.removeEditor(id, userId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/labels")
    public ResponseEntity<TreeDto> updateLabels(
        @PathVariable Long id,
        @Valid @Size(max = 5) @RequestBody Map<@Size(max = 15) String, @Size(max = 15) String> labels) {
        Optional<User> user = userRepository.findByEmail(getAuthenticatedUserEmail());

        TreeDto updated = treeService.updateLabels(id, labels, user);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<TreeStatisticsDto> getTreeStatistics(
        @PathVariable Long id) {
        TreeStatisticsDto stats = treeService.getTreeStatistics(id);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{id}/nodes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TreeNodeDto> addNode(
        @PathVariable Long id,
        @Valid @RequestBody CreateTreeNodeDto nodeDto) {
        Optional<User> user = userRepository.findByEmail(getAuthenticatedUserEmail());

        Optional<Tree> treeOpt = treeRepository.findById(id);

        TreeNodeDto created = treeService.addNodeToTree(treeOpt.orElse(null), nodeDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{treeId}/node/{nodeId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<NodeDto> updateNode(
        @PathVariable Long treeId,
        @PathVariable Long nodeId,
        @Valid @RequestBody UpdateTreeNodeDto updateNodeDto) {
        Optional<User> user = userRepository.findByEmail(getAuthenticatedUserEmail());

        NodeDto updatedNode = nodeService.updateNode(treeId, nodeId, updateNodeDto, user);
        return ResponseEntity.status(HttpStatus.OK).body(updatedNode);
    }

    @PutMapping("/{id}/nodes/{nodeId}/move/{newParentId}")
    public ResponseEntity<Void> moveNode(
        @PathVariable Long id,
        @PathVariable Long nodeId,
        @PathVariable Long newParentId) {
        Optional<User> user = userRepository.findByEmail(getAuthenticatedUserEmail());
        treeService.moveNodeWithinTree(id, nodeId, newParentId, user);
        return ResponseEntity.ok().build();
    }
}
