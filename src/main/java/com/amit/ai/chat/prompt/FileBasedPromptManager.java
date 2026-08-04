package com.amit.ai.chat.prompt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation: loads prompts from classpath resources, caches in memory.
 */
@Component
public class FileBasedPromptManager implements PromptManager {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedPromptManager.class);

    private final ResourceLoader resourceLoader;
    private final String promptsPath;
    private final Map<PromptType, String> cache = new ConcurrentHashMap<>();

    public FileBasedPromptManager(ResourceLoader resourceLoader, @Value("${app.prompts.path:classpath:prompts/}") String promptsPath) {
        this.resourceLoader = resourceLoader;
        this.promptsPath = promptsPath;
    }

    @Override
    public Optional<String> getPrompt(PromptType type) {
        if (type == null) return Optional.empty();

        // Check cache first
        if (cache.containsKey(type)) {
            return Optional.of(cache.get(type));
        }

        // Load from file
        try {
            String filename = type.getFilename();
            String filePath = promptsPath + filename + ".txt";
            Resource resource = resourceLoader.getResource(filePath);
            if (!resource.exists()) {
                logger.warn("Prompt file not found: {}", filePath);
                return Optional.empty();
            }

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            cache.put(type, content);
            logger.debug("Loaded and cached prompt: {}", type);
            return Optional.of(content);
        } catch (IOException e) {
            logger.error("Error loading prompt: {}", type, e);
            return Optional.empty();
        }
    }

    @Override
    public String getSystemPrompt() {
        return getPrompt(PromptType.SYSTEM).orElse("You are a helpful assistant.");
    }

    @Override
    public String getSummaryPrompt() {
        return getPrompt(PromptType.SUMMARIZER).orElse("Summarize the following text concisely.");
    }

    @Override
    public String getRagPrompt() {
        return getPrompt(PromptType.RAG).orElse("Answer based on the provided context.");
    }

    @Override
    public String getInterviewPrompt() {
        return getPrompt(PromptType.INTERVIEW).orElse("Conduct an interview.");
    }
}
