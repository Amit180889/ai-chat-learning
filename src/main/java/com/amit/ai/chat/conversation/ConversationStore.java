package com.amit.ai.chat.conversation;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for conversations.
 */
public interface ConversationStore {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(String conversationId);

    List<Conversation> findByUserId(String userId);

    boolean deleteById(String conversationId);
}
