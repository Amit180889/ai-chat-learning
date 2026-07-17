package com.amit.ai.chat.controller;

import com.amit.ai.chat.model.ChatRequest;
import com.amit.ai.chat.model.ChatResponse;
import com.amit.ai.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/basic")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        return chatService.chat(chatRequest);
    }
}
