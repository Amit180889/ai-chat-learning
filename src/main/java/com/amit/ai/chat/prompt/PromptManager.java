package com.amit.ai.chat.prompt;

import java.util.Optional;

/**
 * Contract for managing prompts: loading, caching, and retrieving by name.
 */
public interface PromptManager {

    Optional<String> getPrompt(String name);

    String getSystemPrompt();

    String getSummaryPrompt();

    String getRagPrompt();

    String getInterviewPrompt();
}
