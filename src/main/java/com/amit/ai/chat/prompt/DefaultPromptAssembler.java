package com.amit.ai.chat.prompt;

import com.amit.ai.chat.memory.ContextWindowManager;
import dev.langchain4j.data.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default implementation: delegates to ContextWindowManager for prompt building and trimming.
 * Separates concerns: orchestration here, all logic in ContextWindowManager.
 */
@Component
@RequiredArgsConstructor
public class DefaultPromptAssembler implements PromptAssembler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPromptAssembler.class);

    private final ContextWindowManager contextWindowManager;

    @Override
    public List<ChatMessage> build(PromptContext context) {
        List<ChatMessage> messages = contextWindowManager.trim(context);
        logger.debug("Assembled and trimmed {} messages for userId={}", messages.size(), context.userContext().userId());
        return messages;
    }
}

