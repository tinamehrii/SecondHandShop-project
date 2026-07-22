package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CategoryRequest;
import com.secondhand.backend.dto.RejectRequest;
import com.secondhand.backend.model.Advertisement;
import com.secondhand.backend.model.Category;
import com.secondhand.backend.model.User;
import com.secondhand.backend.security.CurrentUser;
import com.secondhand.backend.service.AdminService;
import com.secondhand.backend.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * All admin endpoints. Every method first checks the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;

    public AdminController(AdminService adminService, CategoryService categoryService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
    }

    // ---------- advertisement review ----------

    /** GET /api/admin/advertisements/pending */
    @GetMapping("/advertisements/pending")
    public List<Advertisement> pendingAds(HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.getPendingAds();
    }

    /** PUT /api/admin/advertisements/{id}/approve */
    @PutMapping("/advertisements/{id}/approve")
    public Advertisement approve(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.approveAd(id);
    }

    /** PUT /api/admin/advertisements/{id}/reject */
    @PutMapping("/advertisements/{id}/reject")
    public Advertisement reject(@PathVariable Long id, @RequestBody RejectRequest body,
                                HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.rejectAd(id, body.reason);
    }

    /** DELETE /api/admin/advertisements/{id} */
    @DeleteMapping("/advertisements/{id}")
    public void deleteAd(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        adminService.deleteAd(id);
    }

    // ---------- user management (bonus: block/unblock) ----------

    /** GET /api/admin/users */
    @GetMapping("/users")
    public List<User> users(HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.getAllUsers();
    }

    /** PUT /api/admin/users/{id}/block */
    @PutMapping("/users/{id}/block")
    public User block(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.blockUser(id);
    }

    /** PUT /api/admin/users/{id}/unblock */
    @PutMapping("/users/{id}/unblock")
    public User unblock(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.unblockUser(id);
    }

    // ---------- statistics dashboard (bonus) ----------

    /** GET /api/admin/stats */
    @GetMapping("/stats")
    public Map<String, Object> stats(HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return adminService.getStats();
    }

    // ---------- category management ----------

    /** POST /api/admin/categories */
    @PostMapping("/categories")
    public Category createCategory(@RequestBody CategoryRequest body, HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return categoryService.createCategory(body);
    }

    /** PUT /api/admin/categories/{id} */
    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody CategoryRequest body,
                                   HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        return categoryService.updateCategory(id, body);
    }

    /** DELETE /api/admin/categories/{id} */
    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser.requireAdmin(request);
        categoryService.deleteCategory(id);
    }
}
