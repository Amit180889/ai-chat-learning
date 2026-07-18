package com.amit.ai.chat.conversation;

import dev.langchain4j.data.message.ChatMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Domain model representing a conversation.
 */
public class Conversation {

    private String conversationId;

    private String userId;

    private final List<ChatMessage> messages = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;

    public Conversation() {
    }

    public Conversation(String conversationId, String userId, List<ChatMessage> initialMessages, Instant createdAt, Instant updatedAt) {
        this.conversationId = conversationId;
        this.userId = userId;
        if (initialMessages != null) {
            this.messages.addAll(initialMessages);
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Conversation create(String conversationId, String userId) {
        Instant now = Instant.now();
        return new Conversation(conversationId, userId, null, now, now);
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(ChatMessage message) {
        Objects.requireNonNull(message, "message must not be null");
        this.messages.add(message);
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
