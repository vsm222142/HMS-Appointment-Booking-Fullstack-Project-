package com.hms.service;

import com.hms.entity.Notification;
import com.hms.entity.User;
import com.hms.entity.enums.NotificationType;
import com.hms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @SuppressWarnings("null")
    public void sendNotification(User recipient, String message, NotificationType type, Long relatedId) {
        if (recipient == null || recipient.getEmail() == null) return;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .type(type)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        
        Notification saved = java.util.Objects.requireNonNull(notificationRepository.save(notification));

        // Push real-time via WebSocket
        String destination = "/queue/notifications";
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("id", saved.getId() != null ? saved.getId() : 0L);
        payload.put("message", saved.getMessage());
        payload.put("type", saved.getType().name());
        payload.put("relatedId", relatedId != null ? relatedId : "");
        payload.put("createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : java.time.LocalDateTime.now().toString());

        messagingTemplate.convertAndSendToUser(recipient.getEmail(), destination, payload);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        if (notificationId == null) return;
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(user.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
