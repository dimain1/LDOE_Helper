package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Settings;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

}