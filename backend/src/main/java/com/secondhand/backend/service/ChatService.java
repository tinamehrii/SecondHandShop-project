package com.secondhand.backend.service;

import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat between the buyer and the seller about one advertisement.
 */
@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final AdvertisementRepository adRepository;
    private final UserRepository userRepository;

    public ChatService(ConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository,
                       AdvertisementRepository adRepository,
                       UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    /**
     * Opens (or returns the existing) conversation between the logged-in user
     * and the owner of the advertisement.
     */
    public Conversation startConversation(Long userId, Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new ApiException(404, "آگهی پیدا نشد"));

        if (ad.getStatus() != AdStatus.ACTIVE && ad.getStatus() != AdStatus.SOLD) {
            throw new ApiException(400, "برای این آگهی نمی‌توان گفتگو شروع کرد");
        }
        if (ad.getOwner().getId().equals(userId)) {
            throw new ApiException(400, "نمی‌توانید برای آگهی خودتان پیام بفرستید");
        }

        return conversationRepository.findByAdvertisementIdAndBuyerId(adId, userId)
                .orElseGet(() -> {
                    User buyer = userRepository.findById(userId)
                            .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));
                    return conversationRepository.save(
                            new Conversation(ad, buyer, ad.getOwner()));
                });
    }

    /**
     * List of conversations of the user.
     * For every conversation we also send the last message and the
     * number of unread messages (bonus feature).
     */
    public List<Map<String, Object>> getMyConversations(Long userId) {
        List<Conversation> conversations =
                conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Conversation conversation : conversations) {
            Map<String, Object> item = new HashMap<>();
            item.put("conversation", conversation);
            item.put("lastMessage",
                    messageRepository.findTopByConversation_IdOrderBySentAtDesc(conversation.getId()));
            item.put("unreadCount",
                    messageRepository.countByConversation_IdAndSenderIdNotAndSeenFalse(
                            conversation.getId(), userId));
            result.add(item);
        }
        return result;
    }

    /**
     * Messages of one conversation.
     * Opening the conversation marks the messages of the other user as seen (bonus).
     */
    public List<ChatMessage> getMessages(Long userId, Long conversationId) {
        Conversation conversation = getConversationForMember(userId, conversationId);

        List<ChatMessage> unseen = messageRepository
                .findByConversation_IdAndSenderIdNotAndSeenFalse(conversation.getId(), userId);
        for (ChatMessage message : unseen) {
            message.setSeen(true);
        }
        messageRepository.saveAll(unseen);

        return messageRepository.findByConversation_IdOrderBySentAtAsc(conversation.getId());
    }

    /** Sends a new message inside a conversation. */
    public ChatMessage sendMessage(Long userId, Long conversationId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new ApiException(400, "متن پیام نمی‌تواند خالی باشد");
        }
        Conversation conversation = getConversationForMember(userId, conversationId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));
        if (sender.getStatus() == UserStatus.BLOCKED) {
            throw new ApiException(403, "حساب شما مسدود است و امکان ارسال پیام ندارید");
        }

        return messageRepository.save(
                new ChatMessage(conversation, sender, text.trim()));
    }

    /** Checks that the user is the buyer or the seller of the conversation. */
    private Conversation getConversationForMember(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(404, "گفتگو پیدا نشد"));
        boolean isMember = conversation.getBuyer().getId().equals(userId)
                || conversation.getSeller().getId().equals(userId);
        if (!isMember) {
            throw new ApiException(403, "شما عضو این گفتگو نیستید");
        }
        return conversation;
    }
}
