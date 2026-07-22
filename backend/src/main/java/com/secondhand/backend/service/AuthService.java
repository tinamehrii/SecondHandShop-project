package com.secondhand.backend.service;

import com.secondhand.backend.dto.LoginRequest;
import com.secondhand.backend.dto.RegisterRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.User;
import com.secondhand.backend.model.UserStatus;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles register, login and loading the profile of a user.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Hashes the password with SHA-256 so we never store the raw password.
     */
    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Registers a new user and returns a token plus the user info. */
    public Map<String, Object> register(RegisterRequest request) {
        // ---- validation ----
        if (isBlank(request.fullName)) {
            throw new ApiException(400, "نام و نام خانوادگی نمی‌تواند خالی باشد");
        }
        if (isBlank(request.username) || request.username.trim().length() < 4) {
            throw new ApiException(400, "نام کاربری باید حداقل ۴ کاراکتر باشد");
        }
        if (isBlank(request.password) || request.password.length() < 6) {
            throw new ApiException(400, "رمز عبور باید حداقل ۶ کاراکتر باشد");
        }
        if (isBlank(request.phoneNumber) || !request.phoneNumber.matches("09\\d{9}")) {
            throw new ApiException(400, "شماره موبایل معتبر نیست (مثال: 09123456789)");
        }
        if (isBlank(request.email) || !request.email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ApiException(400, "ایمیل معتبر نیست");
        }
        if (userRepository.existsByUsername(request.username.trim())) {
            throw new ApiException(400, "این نام کاربری قبلا ثبت شده است");
        }

        User user = new User(
                request.fullName.trim(),
                request.username.trim(),
                hashPassword(request.password),
                request.phoneNumber.trim(),
                request.email.trim());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /** Checks username and password and returns a token when they are correct. */
    public Map<String, Object> login(LoginRequest request) {
        if (isBlank(request.username) || isBlank(request.password)) {
            throw new ApiException(400, "نام کاربری و رمز عبور را وارد کنید");
        }

        User user = userRepository.findByUsername(request.username.trim())
                .orElseThrow(() -> new ApiException(401, "نام کاربری یا رمز عبور اشتباه است"));

        if (!user.getPassword().equals(hashPassword(request.password))) {
            throw new ApiException(401, "نام کاربری یا رمز عبور اشتباه است");
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ApiException(403, "حساب کاربری شما توسط مدیر مسدود شده است");
        }

        return buildAuthResponse(user);
    }

    /** Loads a user by id (used for the profile page). */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "کاربر پیدا نشد"));
    }

    private Map<String, Object> buildAuthResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(user));
        response.put("user", user);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
