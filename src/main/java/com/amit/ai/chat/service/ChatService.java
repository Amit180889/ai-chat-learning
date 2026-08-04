package com.amit.ai.chat.service;

import com.amit.ai.chat.conversation.Conversation;
import com.amit.ai.chat.conversation.ConversationMemoryService;
import com.amit.ai.chat.model.ChatRequest;
import com.amit.ai.chat.model.ChatResponse;
import com.amit.ai.chat.prompt.PromptAssembler;
import com.amit.ai.chat.prompt.PromptContext;
import com.amit.ai.chat.prompt.PromptManager;
import com.amit.ai.chat.prompt.PromptType;
import com.amit.ai.chat.user.UserContext;
import com.amit.ai.chat.user.UserContextService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private final PromptAssembler promptAssembler;

    @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
    private String modelName;

    public ChatResponse chat(ChatRequest chatRequest) {
        logger.info("Calling chat model (model={}) with message length={}", modelName, chatRequest.message() == null ? 0 : chatRequest.message().length());

        // Get or create conversation
        Conversation conversation = memoryService.getOrCreateConversation(chatRequest.conversationId(), chatRequest.userId());
        conversation.addMessage(UserMessage.userMessage(chatRequest.message()));

        // Fetch user context
        Optional<UserContext> userContext = userContextService.getUserContext(chatRequest.userId());

        // Load system prompt
        String systemPrompt = promptManager.getSystemPrompt();

        // Assemble prompt with all context
        PromptContext promptContext = new PromptContext(
                userContext.orElse(null),
                conversation,
                systemPrompt,
                chatRequest
        );
        List<ChatMessage> messages = promptAssembler.build(promptContext);

        // Build and send request
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
}
