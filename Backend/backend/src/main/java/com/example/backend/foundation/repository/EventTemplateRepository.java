package com.example.backend.foundation.repository;

import com.example.backend.entity.EventTemplate;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventTemplateRepository extends JpaRepository<EventTemplate, Long> {
    List<EventTemplate> findByCreator(User creator);
}
