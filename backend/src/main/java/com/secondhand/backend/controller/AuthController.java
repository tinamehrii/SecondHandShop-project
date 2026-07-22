package com.secondhand.backend.controller;

import com.secondhand.backend.dto.LoginRequest;
import com.secondhand.backend.dto.RegisterRequest;
import com.secondhand.backend.model.User;
import com.secondhand.backend.security.CurrentUser;
import com.secondhand.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Register / login / profile endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/register */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** POST /api/auth/login */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** GET /api/auth/me - profile of the logged-in user */
    @GetMapping("/me")
    public User me(HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        return authService.getUserById(userId);
    }
}
