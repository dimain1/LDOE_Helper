package com.example.backend.foundation.repository;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUser(User user);
}
