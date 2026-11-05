package com.kisanmitra.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
  @Query("select c from Conversation c where (c.participantOne.id = :first and c.participantTwo.id = :second) or (c.participantOne.id = :second and c.participantTwo.id = :first) order by c.updatedAt desc")
  List<Conversation> findBetween(@Param("first") UUID first, @Param("second") UUID second);
  List<Conversation> findByParticipantOneIdOrParticipantTwoIdOrderByUpdatedAtDesc(UUID first, UUID second);
}
