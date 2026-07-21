package com.secondhand.backend.service;

import com.secondhand.backend.dto.RatingRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RatingService rules.
 */
class RatingServiceTest {

    private RatingRepository ratingRepository;
    private AdvertisementRepository adRepository;
    private ConversationRepository conversationRepository;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingRepository = Mockito.mock(RatingRepository.class);
        adRepository = Mockito.mock(AdvertisementRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        conversationRepository = Mockito.mock(ConversationRepository.class);
        ratingService = new RatingService(ratingRepository, adRepository,
                userRepository, conversationRepository);
    }

    private Advertisement buildActiveAd(Long adId, Long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        Advertisement ad = new Advertisement();
        ad.setId(adId);
        ad.setOwner(owner);
        ad.setTitle("گوشی");
        ad.setStatus(AdStatus.ACTIVE);
        return ad;
    }

    @Test
    void scoreOutOfRangeFails() {
        RatingRequest request = new RatingRequest();
        request.advertisementId = 1L;
        request.score = 7;

        ApiException e = assertThrows(ApiException.class, () -> ratingService.rateSeller(1L, request));
        assertEquals(400, e.getStatus());
    }

    @Test
    void ratingYourOwnAdFails() {
        when(adRepository.findById(1L)).thenReturn(Optional.of(buildActiveAd(1L, 5L)));

        RatingRequest request = new RatingRequest();
        request.advertisementId = 1L;
        request.score = 4;

        // user 5 is the owner of the ad, so rating must fail
        assertThrows(ApiException.class, () -> ratingService.rateSeller(5L, request));
    }

    @Test
    void ratingTwiceForSameAdFails() {
        Advertisement ad = buildActiveAd(1L, 5L);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(1L, 2L))
                .thenReturn(Optional.of(new Conversation()));
        when(ratingRepository.existsByRaterIdAndAdvertisementId(2L, 1L)).thenReturn(true);

        RatingRequest request = new RatingRequest();
        request.advertisementId = 1L;
        request.score = 4;

        assertThrows(ApiException.class, () -> ratingService.rateSeller(2L, request));
    }

    @Test
    void ratingWithoutConversationFails() {
        Advertisement ad = buildActiveAd(1L, 5L);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(1L, 2L))
                .thenReturn(Optional.empty());

        RatingRequest request = new RatingRequest();
        request.advertisementId = 1L;
        request.score = 4;

        assertThrows(ApiException.class, () -> ratingService.rateSeller(2L, request));
    }
}
