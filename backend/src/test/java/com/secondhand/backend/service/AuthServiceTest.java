package com.secondhand.backend.service;

import com.secondhand.backend.dto.LoginRequest;
import com.secondhand.backend.dto.RegisterRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.User;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService (register and login rules).
 */
class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        when(jwtUtil.generateToken(any())).thenReturn("fake-token");
        authService = new AuthService(userRepository, jwtUtil);
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.fullName = "علی رضایی";
        request.username = "ali1234";
        request.password = "123456";
        request.phoneNumber = "09123456789";
        request.email = "ali@example.com";
        return request;
    }

    @Test
    void registerWithShortPasswordFails() {
        RegisterRequest request = validRequest();
        request.password = "123";

        ApiException e = assertThrows(ApiException.class, () -> authService.register(request));
        assertEquals(400, e.getStatus());
    }

    @Test
    void registerWithInvalidPhoneFails() {
        RegisterRequest request = validRequest();
        request.phoneNumber = "12345";

        assertThrows(ApiException.class, () -> authService.register(request));
    }

    @Test
    void registerWithDuplicateUsernameFails() {
        RegisterRequest request = validRequest();
        when(userRepository.existsByUsername("ali1234")).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.register(request));
    }

    @Test
    void registerWithValidDataReturnsToken() {
        RegisterRequest request = validRequest();
        when(userRepository.existsByUsername("ali1234")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = authService.register(request);

        assertEquals("fake-token", response.get("token"));
        assertNotNull(response.get("user"));
    }

    @Test
    void loginWithWrongPasswordFails() {
        User user = new User("علی", "ali1234",
                AuthService.hashPassword("correct-pass"), "09123456789", "ali@example.com");
        when(userRepository.findByUsername("ali1234")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.username = "ali1234";
        request.password = "wrong-pass";

        ApiException e = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(401, e.getStatus());
    }

    @Test
    void hashPasswordIsDeterministicAndNotPlain() {
        String hash1 = AuthService.hashPassword("123456");
        String hash2 = AuthService.hashPassword("123456");

        assertEquals(hash1, hash2);
        assertNotEquals("123456", hash1);
    }
}
