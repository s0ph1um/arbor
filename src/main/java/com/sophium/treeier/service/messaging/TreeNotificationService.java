package com.sophium.treeier.service.messaging;

import com.sophium.treeier.dto.TreeUpdateNotification;
import com.sophium.treeier.service.ChangelogService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TreeNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChangelogService changelogService;

    public void notify(TreeUpdateNotification notification) {
        messagingTemplate.convertAndSend("/topic/tree/" + notification.getTreeId(), notification);
        changelogService.record(notification);
    }
}
