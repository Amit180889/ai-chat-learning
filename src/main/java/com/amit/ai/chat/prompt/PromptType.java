package com.amit.ai.chat.prompt;

/**
 * Enum representing different prompt types.
 */
public enum PromptType {
    SYSTEM("system-prompt"),
    RAG("rag-prompt"),
    SUMMARIZER("summarizer-prompt"),
    INTERVIEW("interview-prompt");

    private final String filename;

    PromptType(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
