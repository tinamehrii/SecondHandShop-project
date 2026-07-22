package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdRequest;
import com.secondhand.backend.model.Advertisement;
import com.secondhand.backend.security.CurrentUser;
import com.secondhand.backend.service.AdService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for advertisements (create, edit, delete, search, my ads).
 */
@RestController
@RequestMapping("/api/advertisements")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    /**
     * GET /api/advertisements
     * Public search with combined filters and sorting.
     * Example: /api/advertisements?keyword=گوشی&cityId=1&minPrice=1000&sort=cheapest
     */
    @GetMapping
    public List<Advertisement> search(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) Long cityId,
                                      @RequestParam(required = false) Long minPrice,
                                      @RequestParam(required = false) Long maxPrice,
                                      @RequestParam(required = false) String itemCondition,
                                      @RequestParam(required = false) String sort) {
        return adService.searchActiveAds(keyword, categoryId, cityId, minPrice, maxPrice, itemCondition, sort);
    }

    /** GET /api/advertisements/mine - ads of the logged-in user */
    @GetMapping("/mine")
    public List<Advertisement> myAds(HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return adService.getMyAds(userId);
    }

    /** GET /api/advertisements/{id} - details of one ad */
    @GetMapping("/{id}")
    public Advertisement getById(@PathVariable Long id, HttpServletRequest request) {
        Long viewerId = (Long) request.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        return adService.getAdForView(id, viewerId, isAdmin);
    }

    /** POST /api/advertisements - create a new ad */
    @PostMapping
    public Advertisement create(@RequestBody AdRequest body, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return adService.createAd(userId, body);
    }

    /** PUT /api/advertisements/{id} - edit an ad (goes back to pending) */
    @PutMapping("/{id}")
    public Advertisement update(@PathVariable Long id, @RequestBody AdRequest body,
                                HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return adService.updateAd(userId, id, body);
    }

    /** DELETE /api/advertisements/{id} - logical delete by the owner */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        adService.deleteAd(userId, id);
    }

    /** PUT /api/advertisements/{id}/sold - mark as sold */
    @PutMapping("/{id}/sold")
    public Advertisement markSold(@PathVariable Long id, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return adService.markAsSold(userId, id);
    }
}
