package com.kisanmitra.chat;

import com.kisanmitra.listing.Listing;
import com.kisanmitra.listing.ListingRepository;
import com.kisanmitra.user.User;
import com.kisanmitra.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/chat") @RequiredArgsConstructor
public class ChatController {
  private final ConversationRepository conversations;
  private final ChatMessageRepository messages;
  private final UserRepository users;
  private final ListingRepository listings;

  @PostMapping("/conversations")
  public ResponseEntity<?> start(@RequestBody Map<String, Object> body, Authentication authentication) {
    User current = current(authentication);
    User recipient = users.findById(UUID.fromString(String.valueOf(body.get("recipientId")))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller was not found."));
    if (recipient.getId().equals(current.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot contact yourself.");
    Conversation conversation = conversations.findBetween(current.getId(), recipient.getId()).stream().findFirst().orElseGet(() -> {
      Conversation created = new Conversation(); created.setParticipantOne(current); created.setParticipantTwo(recipient);
      Object listingId = body.get("listingId");
      if (listingId != null && !String.valueOf(listingId).isBlank()) {
        Listing listing = listings.findById(UUID.fromString(String.valueOf(listingId))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing was not found."));
        created.setListing(listing);
      }
      return conversations.save(created);
    });
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", Map.of("conversation", conversationView(conversation, current))));
  }

  @GetMapping("/conversations")
  public Map<String, Object> list(Authentication authentication) {
    User current = current(authentication);
    List<Map<String, Object>> data = conversations.findByParticipantOneIdOrParticipantTwoIdOrderByUpdatedAtDesc(current.getId(), current.getId()).stream().map(c -> conversationView(c, current)).toList();
    return Map.of("success", true, "data", Map.of("conversations", data));
  }

  @GetMapping("/conversations/{id}/messages")
  public Map<String, Object> getMessages(@PathVariable UUID id, Authentication authentication) {
    Conversation conversation = conversationFor(id, current(authentication));
    return Map.of("success", true, "data", Map.of("messages", messages.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream().map(this::messageView).toList()));
  }

  @PostMapping("/messages")
  public ResponseEntity<?> send(@RequestBody Map<String, Object> body, Authentication authentication) {
    User sender = current(authentication);
    Object rawId = body.get("conversationId");
    if (rawId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation is required.");
    Conversation conversation = conversationFor(UUID.fromString(String.valueOf(rawId)), sender);
    String text = String.valueOf(body.getOrDefault("text", "")).trim();
    if (text.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty.");
    ChatMessage message = new ChatMessage(); message.setConversation(conversation); message.setSender(sender); message.setText(text); message = messages.save(message);
    conversation.setUpdatedAt(Instant.now()); conversations.save(conversation);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", messageView(message)));
  }

  private Conversation conversationFor(UUID id, User user) {
    Conversation conversation = conversations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation was not found."));
    if (!conversation.getParticipantOne().getId().equals(user.getId()) && !conversation.getParticipantTwo().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return conversation;
  }
  private User current(Authentication a) { if (a == null || !(a.getPrincipal() instanceof User user)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); return user; }
  private Map<String, Object> userView(User user) { return Map.of("id", user.getId(), "_id", user.getId(), "name", user.getName(), "avatar", user.getAvatar() == null ? "" : user.getAvatar()); }
  private Map<String, Object> messageView(ChatMessage message) { return Map.of("id", message.getId(), "_id", message.getId(), "text", message.getText(), "createdAt", message.getCreatedAt(), "sender", userView(message.getSender())); }
  private Map<String, Object> conversationView(Conversation c, User current) {
    User other = c.getParticipantOne().getId().equals(current.getId()) ? c.getParticipantTwo() : c.getParticipantOne();
    Map<String, Object> result = new LinkedHashMap<>(); result.put("id", c.getId()); result.put("_id", c.getId()); result.put("participants", List.of(userView(c.getParticipantOne()), userView(c.getParticipantTwo()))); result.put("otherParticipant", userView(other)); result.put("updatedAt", c.getUpdatedAt());
    if (c.getListing() != null) result.put("listing", Map.of("id", c.getListing().getId(), "title", c.getListing().getTitle()));
    messages.findTopByConversationIdOrderByCreatedAtDesc(c.getId()).ifPresent(message -> result.put("lastMessage", messageView(message)));
    return result;
  }
}
