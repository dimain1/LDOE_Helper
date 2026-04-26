package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.EventTemplate;

public interface EventTemplateRepository extends JpaRepository<EventTemplate, Long> {

}
