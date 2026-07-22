package com.secondhand.backend.service;

import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin operations: reviewing advertisements, managing users
 * and the statistics dashboard (bonus feature).
 */
@Service
public class AdminService {

    private final AdvertisementRepository adRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final RatingRepository ratingRepository;

    public AdminService(AdvertisementRepository adRepository,
                        UserRepository userRepository,
                        ConversationRepository conversationRepository,
                        ChatMessageRepository messageRepository,
                        RatingRepository ratingRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.ratingRepository = ratingRepository;
    }

    // ---------- advertisement review ----------

    public List<Advertisement> getPendingAds() {
        return adRepository.findByStatus(AdStatus.PENDING);
    }

    public Advertisement approveAd(Long adId) {
        Advertisement ad = getAd(adId);
        if (ad.getStatus() != AdStatus.PENDING) {
            throw new ApiException(400, "فقط آگهی در انتظار تایید را می‌توان تایید کرد");
        }
        ad.setStatus(AdStatus.ACTIVE);
        ad.setRejectReason(null);
        return adRepository.save(ad);
    }

    public Advertisement rejectAd(Long adId, String reason) {
        Advertisement ad = getAd(adId);
        if (ad.getStatus() != AdStatus.PENDING) {
            throw new ApiException(400, "فقط آگهی در انتظار تایید را می‌توان رد کرد");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new ApiException(400, "دلیل رد آگهی را بنویسید");
        }
        ad.setStatus(AdStatus.REJECTED);
        ad.setRejectReason(reason.trim());
        return adRepository.save(ad);
    }

    /** Admin can remove any advertisement (logical delete). */
    public void deleteAd(Long adId) {
        Advertisement ad = getAd(adId);
        ad.setStatus(AdStatus.DELETED);
        adRepository.save(ad);
    }

    // ---------- user management ----------

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User blockUser(Long userId) {
        User user = getUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new ApiException(400, "مدیر سامانه را نمی‌توان مسدود کرد");
        }
        user.setStatus(UserStatus.BLOCKED);
        return userRepository.save(user);
    }

    public User unblockUser(Long userId) {
        User user = getUser(userId);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    // ---------- statistics dashboard (bonus) ----------

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("blockedUsers", userRepository.countByStatus(UserStatus.BLOCKED));
        stats.put("totalAds", adRepository.count());
        stats.put("pendingAds", adRepository.countByStatus(AdStatus.PENDING));
        stats.put("activeAds", adRepository.countByStatus(AdStatus.ACTIVE));
        stats.put("soldAds", adRepository.countByStatus(AdStatus.SOLD));
        stats.put("rejectedAds", adRepository.countByStatus(AdStatus.REJECTED));
        stats.put("deletedAds", adRepository.countByStatus(AdStatus.DELETED));
        stats.put("totalConversations", conversationRepository.count());
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalRatings", ratingRepository.count());
        return stats;
    }

    // ---------- private helpers ----------

    private Advertisement getAd(Long adId) {
        return adRepository.findById(adId)
                .orElseThrow(() -> new ApiException(404, "آگهی پیدا نشد"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));
    }
}
