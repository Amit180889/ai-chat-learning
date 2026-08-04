package com.amit.ai.chat.prompt;

import com.amit.ai.chat.conversation.Conversation;
import com.amit.ai.chat.model.ChatRequest;
import com.amit.ai.chat.user.UserContext;

/**
 * Context data required for prompt assembly.
 */
public record PromptContext(
        UserContext userContext,
        Conversation conversation,
        String systemPrompt,
        ChatRequest chatRequest
) {
}
