package com.amit.ai.chat.controller;

import java.time.Instant;
import java.util.List;

public record ConversationDto(String conversationId, String userId, List<MessageDto> messages, Instant createdAt, Instant updatedAt) {
}
