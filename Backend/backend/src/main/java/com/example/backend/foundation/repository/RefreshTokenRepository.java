package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Прямой JPQL DELETE — выполняется немедленно, минуя entity lifecycle.
     * Необходимо использовать именно @Modifying + @Query вместо derived delete,
     * чтобы SQL удаления ушёл в базу до последующего INSERT в той же транзакции
     * и не нарушал UNIQUE constraint на user_id.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    void deleteByToken(@Param("token") String token);
}
