package com.amit.ai.chat.conversation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple thread-safe in-memory store for conversations.
 */
@Component
public class InMemoryConversationStore implements ConversationStore {

    // conversationId -> Conversation
    private final Map<String, Conversation> store = new ConcurrentHashMap<>();

    // userId -> set of conversationIds
    private final Map<String, Set<String>> userIndex = new ConcurrentHashMap<>();

    @Override
    public Conversation save(Conversation conversation) {
        if (conversation == null || conversation.getConversationId() == null) {
            throw new IllegalArgumentException("conversation and conversationId must not be null");
        }

        store.put(conversation.getConversationId(), conversation);

        // maintain user index
        String userId = conversation.getUserId();
        if (userId != null) {
            userIndex.compute(userId, (k, set) -> {
                if (set == null) set = ConcurrentHashMap.newKeySet();
                set.add(conversation.getConversationId());
                return set;
            });
        }

        return conversation;
    }

    @Override
    public Optional<Conversation> findById(String conversationId) {
        if (conversationId == null) return Optional.empty();
        return Optional.ofNullable(store.get(conversationId));
    }

    @Override
    public List<Conversation> findByUserId(String userId) {
        if (userId == null) return List.of();
        Set<String> ids = userIndex.get(userId);
        if (ids == null || ids.isEmpty()) return List.of();

        List<Conversation> result = new ArrayList<>();
        for (String id : ids) {
            Conversation c = store.get(id);
            if (c != null) result.add(c);
        }
        return result;
    }

    @Override
    public boolean deleteById(String conversationId) {
        if (conversationId == null) return false;
        Conversation removed = store.remove(conversationId);
        if (removed != null && removed.getUserId() != null) {
            String userId = removed.getUserId();
            userIndex.computeIfPresent(userId, (k, set) -> {
                set.remove(conversationId);
                return set.isEmpty() ? null : set;
            });
            return true;
        }
        return removed != null;
    }
}
