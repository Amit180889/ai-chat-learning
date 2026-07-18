package com.amit.ai.chat.service;

import com.amit.ai.chat.conversation.Conversation;
import com.amit.ai.chat.conversation.ConversationMemoryService;
import com.amit.ai.chat.model.ChatRequest;
import com.amit.ai.chat.model.ChatResponse;
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

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final GoogleAiGeminiChatModel chatModel;

    private final ConversationMemoryService memoryService;

    @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
    private String modelName;

    public ChatResponse chat(ChatRequest chatRequest) {

        logger.info("Calling chat model (model={}) with message length={}", modelName, chatRequest.message() == null ? 0 : chatRequest.message().length());

        /*UserMessage userMessage = UserMessage.userMessage(chatRequest.message());

        List<ChatMessage> messages = List.of(userMessage);*/

        Conversation conversation = memoryService.getOrCreateConversation
                (chatRequest.conversationId(), chatRequest.userId());

        conversation.addMessage(UserMessage.userMessage(chatRequest.message()));

        List<ChatMessage> messages = conversation.getMessages();

        dev.langchain4j.model.chat.request.ChatRequest request =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(messages)
                        .parameters(
                                DefaultChatRequestParameters.builder()
                                        .modelName(modelName)
                                        .build()
                        )
                        .build();

        dev.langchain4j.model.chat.response.ChatResponse response =
                chatModel.doChat(request);

        String text = response.aiMessage().text();
        logger.debug("Received response of length={}", text == null ? 0 : text.length());
        return new ChatResponse(text);
    }
}