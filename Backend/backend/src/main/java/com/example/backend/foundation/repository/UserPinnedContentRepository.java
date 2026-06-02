package com.example.backend.foundation.repository;

import com.example.backend.entity.GameContent;
import com.example.backend.entity.User;
import com.example.backend.entity.UserPinnedContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPinnedContentRepository extends JpaRepository<UserPinnedContent, UserPinnedContent.PK> {
    boolean existsByUserAndContent(User user, GameContent content);
    void deleteByUserAndContent(User user, GameContent content);
}
