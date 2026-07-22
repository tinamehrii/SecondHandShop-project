package com.secondhand.backend.service;

import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.AdStatus;
import com.secondhand.backend.model.Advertisement;
import com.secondhand.backend.model.Favorite;
import com.secondhand.backend.model.User;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.FavoriteRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bookmarked (favorite) advertisements of each user.
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AdvertisementRepository adRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           AdvertisementRepository adRepository,
                           UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    public Favorite addFavorite(Long userId, Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new ApiException(404, "آگهی پیدا نشد"));
        if (ad.getStatus() != AdStatus.ACTIVE) {
            throw new ApiException(400, "فقط آگهی فعال را می‌توان نشان کرد");
        }
        if (favoriteRepository.existsByUserIdAndAdvertisementId(userId, adId)) {
            throw new ApiException(400, "این آگهی قبلا نشان شده است");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));
        return favoriteRepository.save(new Favorite(user, ad));
    }

    public void removeFavorite(Long userId, Long adId) {
        Favorite favorite = favoriteRepository.findByUserIdAndAdvertisementId(userId, adId)
                .orElseThrow(() -> new ApiException(404, "این آگهی در لیست نشان‌شده‌ها نیست"));
        favoriteRepository.delete(favorite);
    }

    public List<Favorite> getMyFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
