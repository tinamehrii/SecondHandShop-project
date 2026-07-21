package com.secondhand.backend.controller;

import com.secondhand.backend.model.Favorite;
import com.secondhand.backend.security.CurrentUser;
import com.secondhand.backend.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Favorite (bookmarked) advertisements of the logged-in user.
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /** GET /api/favorites */
    @GetMapping
    public List<Favorite> myFavorites(HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return favoriteService.getMyFavorites(userId);
    }

    /** POST /api/favorites/{adId} */
    @PostMapping("/{adId}")
    public Favorite add(@PathVariable Long adId, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return favoriteService.addFavorite(userId, adId);
    }

    /** DELETE /api/favorites/{adId} */
    @DeleteMapping("/{adId}")
    public void remove(@PathVariable Long adId, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        favoriteService.removeFavorite(userId, adId);
    }
}
