package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String token);
}