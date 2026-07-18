package com.amit.ai.chat.controller;

import com.amit.ai.chat.conversation.Conversation;
import com.amit.ai.chat.conversation.ConversationMemoryService;
import com.amit.ai.chat.model.ChatRequest;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    private final ConversationMemoryService memoryService;

    @GetMapping("/users/{userId}/conversations")
    public List<ConversationDto> listForUser(@PathVariable String userId) {
        logger.debug("Listing conversations for user={}", userId);
        return memoryService.listConversationsForUser(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDto> get(@PathVariable String id) {
        logger.debug("Getting conversation {}", id);
        return memoryService.getConversation(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        logger.debug("Deleting conversation {}", id);
        boolean deleted = memoryService.deleteConversation(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private ConversationDto toDto(Conversation conv) {
        List<MessageDto> msgs = conv.getMessages().stream()
                .map(this::mapMessage)
                .collect(Collectors.toList());

        Instant created = conv.getCreatedAt();
        Instant updated = conv.getUpdatedAt();

        return new ConversationDto(conv.getConversationId(), conv.getUserId(), msgs, created, updated);
    }

    private MessageDto mapMessage(ChatMessage m) {
        String type = m.getClass().getSimpleName();
        String content = extractText(m);
        return new MessageDto(type, content);
    }

    private String extractText(Object m) {
        if (m == null) return null;
        try {
            // try common method names
            for (String name : new String[]{"text", "content", "getText", "getContent", "message"}) {
                try {
                    java.lang.reflect.Method method = m.getClass().getMethod(name);
                    Object res = method.invoke(m);
                    if (res != null) return String.valueOf(res);
                } catch (NoSuchMethodException ignored) {
                }
            }
            // fallback to toString
            return String.valueOf(m);
        } catch (Exception e) {
            return "<unserializable>";
        }
    }
}
