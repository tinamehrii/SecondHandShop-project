package com.secondhand.backend.controller;

import com.secondhand.backend.dto.RatingRequest;
import com.secondhand.backend.model.Rating;
import com.secondhand.backend.security.CurrentUser;
import com.secondhand.backend.service.RatingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Rating endpoints.
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /** POST /api/ratings - rate the seller of an advertisement */
    @PostMapping
    public Rating rate(@RequestBody RatingRequest body, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return ratingService.rateSeller(userId, body);
    }

    /** GET /api/ratings/seller/{sellerId} - ratings and average score of a seller */
    @GetMapping("/seller/{sellerId}")
    public Map<String, Object> sellerRatings(@PathVariable Long sellerId) {
        return ratingService.getSellerRatings(sellerId);
    }
}
