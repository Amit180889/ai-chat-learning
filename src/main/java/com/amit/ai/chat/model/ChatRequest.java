package com.amit.ai.chat.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        String conversationId,
        //Later, after authentication is introduced, we'll remove userId from the request and derive it from the JWT/security context.
        @NotBlank(message = "userId must not be blank")
        String userId,
        @NotBlank(message = "message must not be blank")
        @Size(max = 2000, message = "message must not exceed 2000 characters")
        String message

) {}
