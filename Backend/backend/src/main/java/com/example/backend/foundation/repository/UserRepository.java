package com.example.backend.foundation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);
}
