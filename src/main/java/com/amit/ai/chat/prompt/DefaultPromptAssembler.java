package com.amit.ai.chat.prompt;

import com.amit.ai.chat.conversation.Conversation;
import com.amit.ai.chat.user.UserContext;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation: builds message lists with system prompt, user context, and conversation history.
 */
@Component
public class DefaultPromptAssembler implements PromptAssembler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPromptAssembler.class);

    @Override
    public List<ChatMessage> build(PromptContext context) {
        List<ChatMessage> messages = new ArrayList<>();

        // Build enhanced system prompt
        String enhancedSystemPrompt = buildSystemPrompt(context.systemPrompt(), context.userContext());
        messages.add(SystemMessage.systemMessage(enhancedSystemPrompt));

        // Add conversation history
        messages.addAll(context.conversation().getMessages());

        logger.debug("Assembled {} messages for prompt", messages.size());
        return messages;
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
}
