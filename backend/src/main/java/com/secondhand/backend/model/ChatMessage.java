package com.secondhand.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One text message inside a conversation.
 * The "seen" field is used for the read/unread bonus feature.
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String text;

    private LocalDateTime sentAt = LocalDateTime.now();

    /** True when the other side of the conversation has seen this message. */
    private boolean seen = false;

    public ChatMessage() {
    }

    public ChatMessage(Conversation conversation, User sender, String text) {
        this.conversation = conversation;
        this.sender = sender;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    /** Only the id of the conversation is sent to the client (to avoid a big nested JSON). */
    public Long getConversationId() {
        return conversation == null ? null : conversation.getId();
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
