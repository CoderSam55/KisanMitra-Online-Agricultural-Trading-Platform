package com.kisanmitra.chat;

import com.kisanmitra.listing.Listing;
import com.kisanmitra.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "conversations") @Getter @Setter @NoArgsConstructor
public class Conversation {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.EAGER, optional = false) private User participantOne;
  @ManyToOne(fetch = FetchType.EAGER, optional = false) private User participantTwo;
  @ManyToOne(fetch = FetchType.EAGER) private Listing listing;
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();
  @PreUpdate void touch() { updatedAt = Instant.now(); }
}
