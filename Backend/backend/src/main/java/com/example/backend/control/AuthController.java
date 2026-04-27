package com.example.backend.control;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RefreshRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.mediator.AuthService;
import com.example.backend.mediator.JwtService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Transactional
    public Long register(@RequestBody RegisterRequest request) {
        return authService.register(request).getId();
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @Transactional
    public void logout(@RequestBody RefreshRequest request) {
        authService.logout(request);
    }

    @GetMapping("/refresh")
    @Transactional
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/test/token")
    public String testToken() {
        String token = jwtService.generateAccessToken("test");
        
        System.out.println("Generated Token: " + token);
        String username = jwtService.extractLogin(token);
        System.out.println("Extracted Username: " + username);

        return token;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/test")
    public String adminOnly() {
        return "admin ok";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/test")
    public String userOnly() {
        return "user ok";
    }
}
