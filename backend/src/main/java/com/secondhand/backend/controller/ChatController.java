package com.secondhand.backend.controller;

import com.secondhand.backend.dto.MessageRequest;
import com.secondhand.backend.model.ChatMessage;
import com.secondhand.backend.model.Conversation;
import com.secondhand.backend.security.CurrentUser;
import com.secondhand.backend.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Chat endpoints between buyers and sellers.
 */
@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** POST /api/conversations/start/{adId} - open (or find) a conversation about an ad */
    @PostMapping("/start/{adId}")
    public Conversation start(@PathVariable Long adId, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return chatService.startConversation(userId, adId);
    }

    /** GET /api/conversations - conversations of the logged-in user with last message and unread count */
    @GetMapping
    public List<Map<String, Object>> myConversations(HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return chatService.getMyConversations(userId);
    }

    /** GET /api/conversations/{id}/messages - messages of a conversation (marks them as seen) */
    @GetMapping("/{id}/messages")
    public List<ChatMessage> messages(@PathVariable Long id, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return chatService.getMessages(userId, id);
    }

    /** POST /api/conversations/{id}/messages - send a message */
    @PostMapping("/{id}/messages")
    public ChatMessage send(@PathVariable Long id, @RequestBody MessageRequest body,
                            HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return chatService.sendMessage(userId, id, body.text);
    }
}
