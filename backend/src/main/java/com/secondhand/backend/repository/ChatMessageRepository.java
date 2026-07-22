package com.secondhand.backend.repository;

import com.secondhand.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Note: we write "Conversation_Id" (with underscore) instead of "ConversationId"
 * so Spring Data clearly understands we mean conversation.id (the id of the
 * conversation field) and does not mix it up with the getConversationId() helper
 * method in ChatMessage.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversation_IdOrderBySentAtAsc(Long conversationId);

    /** Messages of the other user that are not seen yet. */
    List<ChatMessage> findByConversation_IdAndSenderIdNotAndSeenFalse(Long conversationId, Long userId);

    ChatMessage findTopByConversation_IdOrderBySentAtDesc(Long conversationId);

    long countByConversation_IdAndSenderIdNotAndSeenFalse(Long conversationId, Long userId);
}
