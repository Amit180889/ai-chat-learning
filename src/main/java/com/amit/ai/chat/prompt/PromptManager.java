package com.amit.ai.chat.prompt;

import java.util.Optional;

/**
 * Contract for managing prompts: loading, caching, and retrieving by type.
 */
public interface PromptManager {

    Optional<String> getPrompt(PromptType type);

    String getSystemPrompt();

    String getSummaryPrompt();

    String getRagPrompt();

    String getInterviewPrompt();
}
