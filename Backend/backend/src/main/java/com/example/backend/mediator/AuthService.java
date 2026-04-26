package com.example.backend.mediator;

import java.time.LocalDateTime;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RefreshRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.RefreshTokenRepository;
import com.example.backend.foundation.repository.UserRepository;
import com.example.backend.foundation.repository.UserRoleRepository;

import jakarta.transaction.Transactional;


@Service 
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;


    public AuthService(UserRepository userRepository, UserRoleRepository userRoleRepository, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtService.generateAccessToken(user.getLogin());
        String refreshToken = jwtService.generateRefreshToken(user.getLogin());

        refreshTokenRepository.save(
                new RefreshToken(user, refreshToken, LocalDateTime.now().plusDays(7))
        );

        return new AuthResponse(accessToken, refreshToken);
    }   
    
   @Transactional
    public User register(RegisterRequest request) {

        UserRole role = userRoleRepository.findById(1L).orElseThrow(() -> new RuntimeException("Default role not found"));
        System.out.println("Default role: " + role.getName());
        System.out.println("Registering user: " + request.getLogin() + ", " + request.getEmail());
        User user = new User(request.getLogin(), passwordEncoder.encode(request.getPassword()), request.getEmail(), role);
        
        return userRepository.save(user);
    } 
    
    public void logout(RefreshRequest request) {
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
    }

}
