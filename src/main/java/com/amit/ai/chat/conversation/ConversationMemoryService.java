package com.amit.ai.chat.conversation;

import dev.langchain4j.data.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service providing conversation memory operations backed by a ConversationStore.
 */
@Service
@RequiredArgsConstructor
public class ConversationMemoryService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationMemoryService.class);

    private final ConversationStore store;

    public Conversation startConversation(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }

        // If the user already has conversations, return the most recently updated one
        var existing = store.findByUserId(userId);
        if (existing != null && !existing.isEmpty()) {
            Conversation latest = existing.stream()
                    .max((c1, c2) -> {
                        java.time.Instant t1 = c1.getUpdatedAt() == null ? c1.getCreatedAt() : c1.getUpdatedAt();
                        java.time.Instant t2 = c2.getUpdatedAt() == null ? c2.getCreatedAt() : c2.getUpdatedAt();
                        return t1.compareTo(t2);
                    }).get();
            logger.info("Returning existing conversation {} for user={}", latest.getConversationId(), userId);
            return latest;
        }

        String id = UUID.randomUUID().toString();
        Conversation conv = Conversation.create(id, userId);
        store.save(conv);
        logger.info("Started conversation {} for user={}", id, userId);
        return conv;
    }

    public Optional<Conversation> getConversation(String conversationId) {
        return store.findById(conversationId);
    }

    public List<Conversation> listConversationsForUser(String userId) {
        return store.findByUserId(userId);
    }

    public Optional<Conversation> addMessage(String conversationId, ChatMessage message) {
        Optional<Conversation> maybe = store.findById(conversationId);
        if (maybe.isEmpty()) return Optional.empty();
        Conversation conv = maybe.get();
        conv.addMessage(message);
        conv.setUpdatedAt(Instant.now());
        store.save(conv);
        logger.debug("Added message to conversation {} (user={})", conversationId, conv.getUserId());
        return Optional.of(conv);
    }

    public boolean deleteConversation(String conversationId) {
        boolean deleted = store.deleteById(conversationId);
        if (deleted) logger.info("Deleted conversation {}", conversationId);
        return deleted;
    }

    public Conversation getOrCreateConversation(String conversationId, String userId) {

        if (conversationId != null) {
            Optional<Conversation> existing = store.findById(conversationId);

            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Conversation conversation = Conversation.create(
                UUID.randomUUID().toString(),
                userId
        );

        store.save(conversation);

        return conversation;
    }

}
