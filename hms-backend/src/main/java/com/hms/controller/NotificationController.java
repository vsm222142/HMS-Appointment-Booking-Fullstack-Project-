package com.hms.controller;

import com.hms.entity.Notification;
import com.hms.entity.User;
import com.hms.service.NotificationService;
import com.hms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public Map<String, Object> getNotifications() {
        User user = userService.requireCurrentUser();
        List<Notification> list = notificationService.getNotificationsForUser(user);
        return Map.of(
            "success", true,
            "data", list.stream().map(this::toDto).collect(Collectors.toList()),
            "unreadCount", notificationService.getUnreadCount(user)
        );
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Long id) {
        User user = userService.requireCurrentUser();
        notificationService.markAsRead(id, user);
        return Map.of("success", true);
    }

    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead() {
        User user = userService.requireCurrentUser();
        notificationService.markAllAsRead(user);
        return Map.of("success", true);
    }

    private Map<String, Object> toDto(Notification n) {
        return Map.of(
            "id", n.getId(),
            "message", n.getMessage(),
            "type", n.getType().name(),
            "isRead", n.isRead(),
            "relatedId", n.getRelatedId() != null ? n.getRelatedId() : "",
            "createdAt", n.getCreatedAt()
        );
    }
}
