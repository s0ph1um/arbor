package com.sophium.treeier.service;

import com.sophium.treeier.dto.CommentDto;
import com.sophium.treeier.entity.ChangelogEntry;
import com.sophium.treeier.entity.Comment;
import com.sophium.treeier.entity.NodeEntity;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.exception.NotFoundException;
import com.sophium.treeier.mapper.CommentMapper;
import com.sophium.treeier.repository.ChangelogRepository;
import com.sophium.treeier.repository.CommentRepository;
import com.sophium.treeier.repository.NodeJpaRepository;
import com.sophium.treeier.repository.TreeJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserEmail;
import static com.sophium.treeier.util.Constants.NODE_NOT_FOUND;
import static com.sophium.treeier.util.Constants.TREE_NOT_FOUND;
import static com.sophium.treeier.util.Constants.USER_CANNOT_EDIT_THIS_TREE;
import static com.sophium.treeier.util.Constants.COMMENT_NOT_FOUND;
import static com.sophium.treeier.util.Constants.USER_CANNOT_CHANGE_THIS_COMMENT;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ChangelogRepository changelogRepository;
    private final TreeJpaRepository treeRepository;
    private final NodeJpaRepository nodeJpaRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;

    public List<CommentDto> getNodeComments(Long nodeId) {
        Optional<Long> latestEntryId = changelogRepository
            .findTopByNodeIdOrderByChangedAtDesc(nodeId)
            .map(ChangelogEntry::getId);

        List<Comment> comments = latestEntryId.isPresent()
            ? commentRepository.findByNode_IdAndChangelogEntryIdOrderByCreatedAtDesc(nodeId, latestEntryId.get())
            : commentRepository.findByNode_IdAndChangelogEntryIdIsNullOrderByCreatedAtDesc(nodeId);

        return commentMapper.toDtos(comments);
    }

    @Transactional
    public CommentDto createComment(Long treeId, Long nodeId, String text) {
        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        Tree tree = treeRepository.findById(treeId)
            .orElseThrow(() -> new NotFoundException(String.format(TREE_NOT_FOUND, treeId)));

        if (!tree.canEdit(currentUser)) {
            throw new AccessDeniedException(USER_CANNOT_EDIT_THIS_TREE);
        }

        NodeEntity node = nodeJpaRepository.findById(nodeId)
            .orElseThrow(() -> new NotFoundException(String.format(NODE_NOT_FOUND, nodeId)));

        Long changelogEntryId = changelogRepository
            .findTopByNodeIdOrderByChangedAtDesc(nodeId)
            .map(ChangelogEntry::getId)
            .orElse(null);

        Comment comment = Comment.builder()
            .author(currentUser)
            .text(text)
            .node(node)
            .changelogEntryId(changelogEntryId)
            .createdAt(LocalDateTime.now(ZoneId.systemDefault()))
            .build();

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto updateComment(Long commentId, String text) {
        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(String.format(COMMENT_NOT_FOUND, commentId)));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(USER_CANNOT_CHANGE_THIS_COMMENT);
        }

        comment.setText(text);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = userService.getUserByEmail(getAuthenticatedUserEmail());
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(String.format(COMMENT_NOT_FOUND, commentId)));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(USER_CANNOT_CHANGE_THIS_COMMENT);
        }

        commentRepository.delete(comment);
    }
}
