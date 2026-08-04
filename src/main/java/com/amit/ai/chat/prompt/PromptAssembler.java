package com.amit.ai.chat.prompt;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * Contract for assembling prompts and building message lists.
 */
public interface PromptAssembler {

    List<ChatMessage> build(PromptContext context);
}
