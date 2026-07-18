package com.sophium.treeier.service;

import com.sophium.treeier.dto.ChangelogEntryDto;
import com.sophium.treeier.dto.TreeUpdateNotification;
import com.sophium.treeier.entity.ChangelogEntry;
import com.sophium.treeier.repository.ChangelogRepository;
import com.sophium.treeier.repository.TreeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangelogService {

    private final ChangelogRepository changelogRepository;


    public void record(TreeUpdateNotification notification) {
        changelogRepository.save(ChangelogEntry.builder()
            .treeId(notification.getTreeId())
            .nodeId(notification.getNodeId())
            .nodeTitle(notification.getNodeTitle())
            .operation(notification.getOperationType())
            .authorEmail(notification.getAuthorEmail())
            .authorName(notification.getAuthorName())
            .changedAt(notification.getTimestamp())
            .build());
    }

    public List<ChangelogEntryDto> getByTreeId(Long treeId) {
        return changelogRepository.findByTreeIdOrderByChangedAtDesc(treeId)
            .stream()
            .map(e -> ChangelogEntryDto.builder()
                .id(e.getId())
                .nodeId(e.getNodeId())
                .nodeTitle(e.getNodeTitle())
                .operation(e.getOperation())
                .authorEmail(e.getAuthorEmail())
                .authorName(e.getAuthorName())
                .changedAt(e.getChangedAt())
                .build())
            .toList();
    }
}
