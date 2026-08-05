package com.amit.ai.chat.memory;

import com.amit.ai.chat.prompt.PromptContext;
import com.amit.ai.chat.user.UserContext;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sliding window implementation: builds enhanced system prompt, adds conversation history, then trims to fit.
 * Keeps SystemMessage + last N messages to manage token limits.
 */
@Component
public class SlidingWindowContextManager implements ContextWindowManager {

    private static final Logger logger = LoggerFactory.getLogger(SlidingWindowContextManager.class);

    private final int maxMessages;

    public SlidingWindowContextManager(@Value("${app.context.max-messages:20}") int maxMessages) {
        this.maxMessages = maxMessages > 0 ? maxMessages : 20;
        logger.info("Initialized SlidingWindowContextManager with maxMessages={}", this.maxMessages);
    }

    @Override
    public List<ChatMessage> trim(PromptContext context) {
        List<ChatMessage> messages = new ArrayList<>();

        // Build enhanced system prompt with user context
        String enhancedSystemPrompt = buildSystemPrompt(context.systemPrompt(), context.userContext());
        messages.add(SystemMessage.systemMessage(enhancedSystemPrompt));

        // Add conversation history
        messages.addAll(context.conversation().getMessages());

        // Apply sliding window trimming
        return trimToWindow(messages, context.userContext().userId());
    }

    private String buildSystemPrompt(String baseSystemPrompt, UserContext userContext) {
        if (userContext == null) {
            return baseSystemPrompt;
        }

        StringBuilder enhanced = new StringBuilder(baseSystemPrompt);
        enhanced.append("\n\n--- User Context ---\n");
        enhanced.append("Preferred Language: ").append(userContext.preferredLanguage()).append("\n");
        enhanced.append("Experience Level: ").append(userContext.experienceLevel()).append("\n");
        enhanced.append("Response Style: ").append(userContext.preferredResponseStyle()).append("\n");
        enhanced.append("Profession: ").append(userContext.profession()).append("\n");
        enhanced.append("Interests: ").append(String.join(", ", userContext.interests())).append("\n");

        logger.debug("Enhanced system prompt with user context (userId={})", userContext.userId());
        return enhanced.toString();
    }

    private List<ChatMessage> trimToWindow(List<ChatMessage> messages, String userId) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        // Extract SystemMessage if present (always keep it)
        SystemMessage systemMessage = null;
        List<ChatMessage> nonSystemMessages = new ArrayList<>();

        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) {
                systemMessage = (SystemMessage) msg;
            } else {
                nonSystemMessages.add(msg);
            }
        }

        // If conversation history is within limit, return as is
        if (nonSystemMessages.size() <= maxMessages) {
            return messages;
        }

        // Apply sliding window: keep only last maxMessages
        int startIdx = nonSystemMessages.size() - maxMessages;
        List<ChatMessage> trimmed = new ArrayList<>();

        // Add system message at the beginning
        if (systemMessage != null) {
            trimmed.add(systemMessage);
        }

        // Add last maxMessages
        trimmed.addAll(nonSystemMessages.subList(startIdx, nonSystemMessages.size()));

        logger.debug("Trimmed messages from {} to {} (kept SystemMessage + last {} messages) for userId={}", 
                messages.size(), trimmed.size(), maxMessages, userId);

        return trimmed;
    }
}
