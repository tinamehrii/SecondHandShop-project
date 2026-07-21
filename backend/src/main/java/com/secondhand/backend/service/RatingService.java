package com.secondhand.backend.service;

import com.secondhand.backend.dto.RatingRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.RatingRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rating the seller of an advertisement (score 1 to 5 with optional comment).
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final AdvertisementRepository adRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public RatingService(RatingRepository ratingRepository,
                         AdvertisementRepository adRepository,
                         UserRepository userRepository,
                         ConversationRepository conversationRepository) {
        this.ratingRepository = ratingRepository;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    /** Registers a new rating for the seller of an advertisement. */
    public Rating rateSeller(Long userId, RatingRequest request) {
        if (request.advertisementId == null) {
            throw new ApiException(400, "آگهی مشخص نشده است");
        }
        if (request.score == null || request.score < 1 || request.score > 5) {
            throw new ApiException(400, "امتیاز باید عددی بین ۱ تا ۵ باشد");
        }

        Advertisement ad = adRepository.findById(request.advertisementId)
                .orElseThrow(() -> new ApiException(404, "آگهی پیدا نشد"));
        if (ad.getStatus() != AdStatus.ACTIVE && ad.getStatus() != AdStatus.SOLD) {
            throw new ApiException(400, "برای این آگهی نمی‌توان امتیاز ثبت کرد");
        }
        if (ad.getOwner().getId().equals(userId)) {
            throw new ApiException(400, "نمی‌توانید به آگهی خودتان امتیاز بدهید");
        }
        // only a buyer that started a conversation about this ad can rate the seller
        if (conversationRepository.findByAdvertisementIdAndBuyerId(ad.getId(), userId).isEmpty()) {
            throw new ApiException(400, "فقط خریداری که با فروشنده گفتگو کرده می‌تواند امتیاز بدهد");
        }
        if (ratingRepository.existsByRaterIdAndAdvertisementId(userId, ad.getId())) {
            throw new ApiException(400, "شما قبلا برای این آگهی امتیاز ثبت کرده‌اید");
        }

        User rater = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));

        return ratingRepository.save(new Rating(ad, rater, ad.getOwner(),
                request.score, request.comment));
    }

    /** Ratings of one seller plus the average score. */
    public Map<String, Object> getSellerRatings(Long sellerId) {
        List<Rating> ratings = ratingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);

        double average = 0;
        if (!ratings.isEmpty()) {
            double sum = 0;
            for (Rating rating : ratings) {
                sum += rating.getScore();
            }
            average = sum / ratings.size();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("average", Math.round(average * 10.0) / 10.0);
        result.put("count", ratings.size());
        result.put("ratings", ratings);
        return result;
    }
}
