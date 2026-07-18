package com.sophium.treeier.controller;

import com.sophium.treeier.dto.ChangelogEntryDto;
import com.sophium.treeier.service.ChangelogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/changelog")
@RequiredArgsConstructor
public class ChangelogController {

    private final ChangelogService changelogService;

    @GetMapping("/tree/{treeId}")
    public ResponseEntity<List<ChangelogEntryDto>> getTreeChangelog(
        @PathVariable Long treeId
    ) {
        List<ChangelogEntryDto> byTreeId = changelogService.getByTreeId(treeId);
        return ResponseEntity.ok(byTreeId);
    }
}
