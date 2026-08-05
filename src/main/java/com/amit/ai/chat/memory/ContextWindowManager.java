package com.amit.ai.chat.memory;

import com.amit.ai.chat.prompt.PromptContext;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * Contract for managing context window: building and trimming message history to fit token limits.
 */
public interface ContextWindowManager {

    /**
     * Build and trim messages to fit within context window.
     * Constructs: SystemMessage (with user context) → Conversation history → Trimmed list.
     * 
     * @param context Full context including user, conversation, and system prompt
     * @return Trimmed message list ready for LLM
     */
    List<ChatMessage> trim(PromptContext context);
}
