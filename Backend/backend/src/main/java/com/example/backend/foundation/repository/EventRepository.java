package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
