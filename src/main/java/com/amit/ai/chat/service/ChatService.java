package com.amit.ai.chat.service;

import com.amit.ai.chat.conversation.Conversation;
import com.amit.ai.chat.conversation.ConversationMemoryService;
import com.amit.ai.chat.model.ChatRequest;
import com.amit.ai.chat.model.ChatResponse;
import com.amit.ai.chat.prompt.PromptManager;
import com.amit.ai.chat.user.UserContext;
import com.amit.ai.chat.user.UserContextService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final GoogleAiGeminiChatModel chatModel;
    private final ConversationMemoryService memoryService;
    private final PromptManager promptManager;
    private final UserContextService userContextService;

    @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
    private String modelName;

    public ChatResponse chat(ChatRequest chatRequest) {
        logger.info("Calling chat model (model={}) with message length={}", modelName, chatRequest.message() == null ? 0 : chatRequest.message().length());

        Conversation conversation = memoryService.getOrCreateConversation(chatRequest.conversationId(), chatRequest.userId());
        conversation.addMessage(UserMessage.userMessage(chatRequest.message()));

        // Build message list: SystemMessage + Conversation History + Current UserMessage
        List<ChatMessage> messages = new ArrayList<>();

        // Load system prompt and enhance with user context
        String systemPromptText = promptManager.getSystemPrompt();
        String enhancedSystemPrompt = enhanceSystemPrompt(systemPromptText, chatRequest.userId());
        messages.add(SystemMessage.systemMessage(enhancedSystemPrompt));

        // Add conversation history
        messages.addAll(conversation.getMessages());

        dev.langchain4j.model.chat.request.ChatRequest request =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(messages)
                        .parameters(
                                DefaultChatRequestParameters.builder()
                                        .modelName(modelName)
                                        .build()
                        )
                        .build();

        dev.langchain4j.model.chat.response.ChatResponse response = chatModel.doChat(request);

        String text = response.aiMessage().text();
        logger.debug("Received response of length={}", text == null ? 0 : text.length());
        return new ChatResponse(text);
    }

    private String enhanceSystemPrompt(String baseSystemPrompt, String userId) {
        Optional<UserContext> userContext = userContextService.getUserContext(userId);

        if (userContext.isEmpty()) {
            return baseSystemPrompt;
        }

        UserContext ctx = userContext.get();
        StringBuilder enhanced = new StringBuilder(baseSystemPrompt);
        enhanced.append("\n\n--- User Context ---\n");
        enhanced.append("Preferred Language: ").append(ctx.preferredLanguage()).append("\n");
        enhanced.append("Experience Level: ").append(ctx.experienceLevel()).append("\n");
        enhanced.append("Response Style: ").append(ctx.preferredResponseStyle()).append("\n");
        enhanced.append("Profession: ").append(ctx.profession()).append("\n");
        enhanced.append("Interests: ").append(String.join(", ", ctx.interests())).append("\n");

        logger.debug("Enhanced system prompt for user={}", userId);
        return enhanced.toString();
    }
}
