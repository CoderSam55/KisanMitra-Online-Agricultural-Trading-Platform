package com.kisanmitra.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
  List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
  Optional<ChatMessage> findTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
