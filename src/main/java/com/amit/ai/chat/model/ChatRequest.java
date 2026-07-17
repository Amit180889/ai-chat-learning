package com.amit.ai.chat.model;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank(message = "message must not be blank") String message) {
}
