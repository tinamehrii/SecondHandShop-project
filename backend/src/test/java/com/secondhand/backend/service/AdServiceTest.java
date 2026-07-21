package com.secondhand.backend.service;

import com.secondhand.backend.dto.AdRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdService (validation, search filters and sold logic).
 */
class AdServiceTest {

    private AdvertisementRepository adRepository;
    private CategoryRepository categoryRepository;
    private RatingRepository ratingRepository;
    private AdService adService;

    @BeforeEach
    void setUp() {
        adRepository = Mockito.mock(AdvertisementRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        CityRepository cityRepository = Mockito.mock(CityRepository.class);
        ratingRepository = Mockito.mock(RatingRepository.class);
        ImageStorageService imageStorageService = Mockito.mock(ImageStorageService.class);
        adService = new AdService(adRepository, userRepository, categoryRepository,
                cityRepository, ratingRepository, imageStorageService);
    }

    /** Small helper for building a test advertisement. */
    private Advertisement buildAd(Long id, Long ownerId, String title, long price, AdStatus status) {
        User owner = new User();
        owner.setId(ownerId);

        Category category = new Category("موبایل", null);
        category.setId(1L);
        City city = new City("تهران");
        city.setId(1L);

        Advertisement ad = new Advertisement();
        ad.setId(id);
        ad.setOwner(owner);
        ad.setTitle(title);
        ad.setDescription("توضیحات تستی");
        ad.setPrice(price);
        ad.setCategory(category);
        ad.setCity(city);
        ad.setStatus(status);
        ad.setCreatedAt(LocalDateTime.now());
        return ad;
    }

    @Test
    void createAdWithNegativePriceFails() {
        AdRequest request = new AdRequest();
        request.title = "گوشی سامسونگ";
        request.description = "سالم";
        request.price = -100L;
        request.categoryId = 1L;
        request.cityId = 1L;

        ApiException e = assertThrows(ApiException.class, () -> adService.createAd(1L, request));
        assertEquals(400, e.getStatus());
    }

    @Test
    void createAdWithEmptyTitleFails() {
        AdRequest request = new AdRequest();
        request.title = "   ";
        request.description = "سالم";
        request.price = 1000L;
        request.categoryId = 1L;
        request.cityId = 1L;

        assertThrows(ApiException.class, () -> adService.createAd(1L, request));
    }

    @Test
    void searchFiltersByKeywordAndPriceRange() {
        Advertisement cheapPhone = buildAd(1L, 1L, "گوشی ارزان", 1_000_000, AdStatus.ACTIVE);
        Advertisement expensivePhone = buildAd(2L, 1L, "گوشی گران", 90_000_000, AdStatus.ACTIVE);
        Advertisement bike = buildAd(3L, 2L, "دوچرخه", 5_000_000, AdStatus.ACTIVE);
        when(adRepository.findByStatus(AdStatus.ACTIVE))
                .thenReturn(List.of(cheapPhone, expensivePhone, bike));

        List<Advertisement> result = adService.searchActiveAds(
                "گوشی", null, null, null, 10_000_000L, null, "newest");

        assertEquals(1, result.size());
        assertEquals("گوشی ارزان", result.get(0).getTitle());
    }

    @Test
    void searchSortsByCheapestPrice() {
        Advertisement ad1 = buildAd(1L, 1L, "کالا یک", 3_000_000, AdStatus.ACTIVE);
        Advertisement ad2 = buildAd(2L, 1L, "کالا دو", 1_000_000, AdStatus.ACTIVE);
        when(adRepository.findByStatus(AdStatus.ACTIVE)).thenReturn(List.of(ad1, ad2));

        List<Advertisement> result = adService.searchActiveAds(
                null, null, null, null, null, null, "cheapest");

        assertEquals("کالا دو", result.get(0).getTitle());
    }

    @Test
    void markAsSoldByAnotherUserFails() {
        Advertisement ad = buildAd(1L, 1L, "گوشی", 1_000_000, AdStatus.ACTIVE);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));

        ApiException e = assertThrows(ApiException.class, () -> adService.markAsSold(99L, 1L));
        assertEquals(403, e.getStatus());
    }
}
