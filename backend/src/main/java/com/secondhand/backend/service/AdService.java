package com.secondhand.backend.service;

import com.secondhand.backend.dto.AdRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Everything about advertisements:
 * creating, editing, deleting, searching and filtering.
 */
@Service
public class AdService {

    private final AdvertisementRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final RatingRepository ratingRepository;
    private final ImageStorageService imageStorageService;

    public AdService(AdvertisementRepository adRepository,
                     UserRepository userRepository,
                     CategoryRepository categoryRepository,
                     CityRepository cityRepository,
                     RatingRepository ratingRepository,
                     ImageStorageService imageStorageService) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.cityRepository = cityRepository;
        this.ratingRepository = ratingRepository;
        this.imageStorageService = imageStorageService;
    }

    /** Creates a new advertisement. It starts in PENDING status. */
    public Advertisement createAd(Long userId, AdRequest request) {
        validateAdRequest(request);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));
        Category category = categoryRepository.findById(request.categoryId)
                .orElseThrow(() -> new ApiException(400, "دسته‌بندی انتخاب شده وجود ندارد"));
        City city = cityRepository.findById(request.cityId)
                .orElseThrow(() -> new ApiException(400, "شهر انتخاب شده وجود ندارد"));

        Advertisement ad = new Advertisement();
        ad.setTitle(request.title.trim());
        ad.setDescription(request.description.trim());
        ad.setPrice(request.price);
        ad.setItemCondition(request.itemCondition);
        ad.setOwner(owner);
        ad.setCategory(category);
        ad.setCity(city);
        ad.setStatus(AdStatus.PENDING);
        adRepository.save(ad);

        saveImages(ad, request.imagesBase64);
        return adRepository.save(ad);
    }

    /** Edits an advertisement. After editing, it must be approved again by admin. */
    public Advertisement updateAd(Long userId, Long adId, AdRequest request) {
        validateAdRequest(request);

        Advertisement ad = getAdOrThrow(adId);
        if (!ad.getOwner().getId().equals(userId)) {
            throw new ApiException(403, "فقط صاحب آگهی می‌تواند آن را ویرایش کند");
        }
        if (ad.getStatus() == AdStatus.DELETED || ad.getStatus() == AdStatus.SOLD) {
            throw new ApiException(400, "آگهی حذف شده یا فروخته شده قابل ویرایش نیست");
        }

        Category category = categoryRepository.findById(request.categoryId)
                .orElseThrow(() -> new ApiException(400, "دسته‌بندی انتخاب شده وجود ندارد"));
        City city = cityRepository.findById(request.cityId)
                .orElseThrow(() -> new ApiException(400, "شهر انتخاب شده وجود ندارد"));

        ad.setTitle(request.title.trim());
        ad.setDescription(request.description.trim());
        ad.setPrice(request.price);
        ad.setItemCondition(request.itemCondition);
        ad.setCategory(category);
        ad.setCity(city);
        // after every edit the ad must be reviewed again
        ad.setStatus(AdStatus.PENDING);
        ad.setRejectReason(null);

        // when new images are sent, old images are replaced
        if (request.imagesBase64 != null && !request.imagesBase64.isEmpty()) {
            ad.getImages().clear();
            saveImages(ad, request.imagesBase64);
        }
        return adRepository.save(ad);
    }

    /** Logical delete: the row stays in the database with DELETED status. */
    public void deleteAd(Long userId, Long adId) {
        Advertisement ad = getAdOrThrow(adId);
        if (!ad.getOwner().getId().equals(userId)) {
            throw new ApiException(403, "فقط صاحب آگهی می‌تواند آن را حذف کند");
        }
        ad.setStatus(AdStatus.DELETED);
        adRepository.save(ad);
    }

    /** The owner marks the advertisement as sold. */
    public Advertisement markAsSold(Long userId, Long adId) {
        Advertisement ad = getAdOrThrow(adId);
        if (!ad.getOwner().getId().equals(userId)) {
            throw new ApiException(403, "فقط صاحب آگهی می‌تواند آن را فروخته شده اعلام کند");
        }
        if (ad.getStatus() != AdStatus.ACTIVE) {
            throw new ApiException(400, "فقط آگهی فعال را می‌توان فروخته شده اعلام کرد");
        }
        ad.setStatus(AdStatus.SOLD);
        return adRepository.save(ad);
    }

    /** Advertisements of the logged-in user (all statuses except deleted). */
    public List<Advertisement> getMyAds(Long userId) {
        return adRepository.findByOwnerIdAndStatusNot(userId, AdStatus.DELETED);
    }

    /** Loads one advertisement for showing its details. */
    public Advertisement getAdForView(Long adId, Long viewerId, boolean isAdmin) {
        Advertisement ad = getAdOrThrow(adId);
        boolean isOwner = viewerId != null && ad.getOwner().getId().equals(viewerId);
        // normal users can only open active or sold ads
        if (ad.getStatus() == AdStatus.ACTIVE || ad.getStatus() == AdStatus.SOLD || isOwner || isAdmin) {
            return ad;
        }
        throw new ApiException(404, "آگهی در دسترس نیست");
    }

    /**
     * Search on active advertisements with optional filters (bonus: combined filters)
     * and different sort options (bonus: sorting).
     * sort values: newest, oldest, cheapest, expensive, bestRated
     */
    public List<Advertisement> searchActiveAds(String keyword, Long categoryId, Long cityId,
                                               Long minPrice, Long maxPrice, String itemCondition,
                                               String sort) {
        List<Advertisement> result = new ArrayList<>(adRepository.findByStatus(AdStatus.ACTIVE));

        // ---- filter by keyword (title or description) ----
        if (keyword != null && !keyword.trim().isEmpty()) {
            String key = keyword.trim().toLowerCase();
            result.removeIf(ad -> !ad.getTitle().toLowerCase().contains(key)
                    && !ad.getDescription().toLowerCase().contains(key));
        }

        // ---- filter by category (a parent category also includes its children - bonus) ----
        if (categoryId != null) {
            List<Long> allowedIds = new ArrayList<>();
            allowedIds.add(categoryId);
            for (Category child : categoryRepository.findByParentId(categoryId)) {
                allowedIds.add(child.getId());
            }
            result.removeIf(ad -> !allowedIds.contains(ad.getCategory().getId()));
        }

        // ---- filter by city ----
        if (cityId != null) {
            result.removeIf(ad -> !ad.getCity().getId().equals(cityId));
        }

        // ---- filter by price range ----
        if (minPrice != null) {
            result.removeIf(ad -> ad.getPrice() < minPrice);
        }
        if (maxPrice != null) {
            result.removeIf(ad -> ad.getPrice() > maxPrice);
        }

        // ---- filter by item condition ----
        if (itemCondition != null && !itemCondition.trim().isEmpty()) {
            result.removeIf(ad -> ad.getItemCondition() == null
                    || !ad.getItemCondition().equals(itemCondition));
        }

        // ---- sorting (bonus) ----
        if (sort == null || sort.isEmpty() || sort.equals("newest")) {
            result.sort(Comparator.comparing(Advertisement::getCreatedAt).reversed());
        } else if (sort.equals("oldest")) {
            result.sort(Comparator.comparing(Advertisement::getCreatedAt));
        } else if (sort.equals("cheapest")) {
            result.sort(Comparator.comparing(Advertisement::getPrice));
        } else if (sort.equals("expensive")) {
            result.sort(Comparator.comparing(Advertisement::getPrice).reversed());
        } else if (sort.equals("bestRated")) {
            result.sort(Comparator.comparingDouble(this::getSellerAverageScore).reversed());
        }

        return result;
    }

    /** Average rating of the seller of an advertisement (0 when there is no rating). */
    public double getSellerAverageScore(Advertisement ad) {
        List<Rating> ratings = ratingRepository.findBySellerIdOrderByCreatedAtDesc(ad.getOwner().getId());
        if (ratings.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Rating rating : ratings) {
            sum += rating.getScore();
        }
        return sum / ratings.size();
    }

    public Advertisement getAdOrThrow(Long adId) {
        return adRepository.findById(adId)
                .orElseThrow(() -> new ApiException(404, "آگهی پیدا نشد"));
    }

    // ---------- private helper methods ----------

    private void validateAdRequest(AdRequest request) {
        if (request.title == null || request.title.trim().isEmpty()) {
            throw new ApiException(400, "عنوان آگهی نمی‌تواند خالی باشد");
        }
        if (request.description == null || request.description.trim().isEmpty()) {
            throw new ApiException(400, "توضیحات آگهی نمی‌تواند خالی باشد");
        }
        if (request.price == null || request.price <= 0) {
            throw new ApiException(400, "قیمت باید یک عدد مثبت باشد");
        }
        if (request.categoryId == null || request.cityId == null) {
            throw new ApiException(400, "دسته‌بندی و شهر را انتخاب کنید");
        }
        if (request.imagesBase64 != null && request.imagesBase64.size() > 5) {
            throw new ApiException(400, "حداکثر ۵ تصویر برای هر آگهی مجاز است");
        }
    }

    private void saveImages(Advertisement ad, List<String> imagesBase64) {
        if (imagesBase64 == null) {
            return;
        }
        int index = 0;
        for (String base64 : imagesBase64) {
            String fileName = imageStorageService.saveBase64Image(base64, ad.getId(), index);
            ad.getImages().add(new AdImage(fileName, ad));
            index++;
        }
    }
}
