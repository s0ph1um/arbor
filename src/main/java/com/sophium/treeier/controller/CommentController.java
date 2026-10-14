package com.sophium.treeier.controller;

import com.sophium.treeier.dto.CommentDto;
import com.sophium.treeier.dto.CommentRequest;
import com.sophium.treeier.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/trees/{treeId}/nodes/{nodeId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(
        @PathVariable Long treeId,
        @PathVariable Long nodeId
    ) {
        return ResponseEntity.ok(commentService.getNodeComments(treeId, nodeId));
    }

    @PostMapping("/trees/{treeId}/nodes/{nodeId}/comments")
    public ResponseEntity<CommentDto> createComment(
        @PathVariable Long treeId,
        @PathVariable Long nodeId,
        @RequestBody @Valid CommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.createComment(treeId, nodeId, request.getText()));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
        @PathVariable Long commentId,
        @RequestBody @Valid CommentRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request.getText()));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
