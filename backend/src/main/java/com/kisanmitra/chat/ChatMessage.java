package com.kisanmitra.chat;

import com.kisanmitra.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "chat_messages") @Getter @Setter @NoArgsConstructor
public class ChatMessage {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.EAGER, optional = false) private Conversation conversation;
  @ManyToOne(fetch = FetchType.EAGER, optional = false) private User sender;
  @Column(nullable = false, length = 2000) private String text;
  private Instant createdAt = Instant.now();
}
