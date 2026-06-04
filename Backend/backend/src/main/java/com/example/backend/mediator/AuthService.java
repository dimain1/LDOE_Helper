package com.example.backend.mediator;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 401 — пользователь не найден или пароль неверен
        User user;
        try {
            user = userRepository.findByLogin(request.getLogin()).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        String accessToken  = jwtService.generateAccessToken(user.getLogin());
        String refreshToken = jwtService.generateRefreshToken(user.getLogin());

        // Удаляем старый refresh-токен пользователя (если есть), чтобы избежать
        // нарушения уникального constraint по user_id при повторном входе
        refreshTokenRepository.deleteByUser(user);

        refreshTokenRepository.save(
            new RefreshToken(user, refreshToken, Instant.now().plusSeconds(7L * 24 * 60 * 60))
        );

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public User register(RegisterRequest request) {
        // 409 — логин или email уже заняты
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Логин уже занят");
        }

        // Первый зарегистрированный пользователь получает роль ADMIN,
        // все последующие — USER
        String roleName = userRepository.count() == 0 ? "ADMIN" : "USER";

        UserRole role = userRoleRepository.findByName(roleName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Роль '" + roleName + "' не найдена в справочнике"));

        User user = new User(
            request.getLogin(),
            passwordEncoder.encode(request.getPassword()),
            request.getEmail(),
            role
        );
        return userRepository.save(user);
    }

    public void logout(RefreshRequest request) {
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
    }

    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Недействительный refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token истёк");
        }

        String accessToken      = jwtService.generateAccessToken(refreshToken.getUser().getLogin());
        String newRefreshToken  = jwtService.generateRefreshToken(refreshToken.getUser().getLogin());

        refreshToken.setToken(newRefreshToken);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7L * 24 * 60 * 60));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, newRefreshToken);
    }
}
