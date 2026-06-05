package com.example.backend.foundation.repository;

import com.example.backend.entity.GameContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameContentRepository extends JpaRepository<GameContent, Long> {

    /** Весь контент с флагом pinned для конкретного пользователя. */
    @Query("""
        SELECT gc, CASE WHEN upc.content IS NOT NULL THEN true ELSE false END
        FROM GameContent gc
        LEFT JOIN UserPinnedContent upc
            ON upc.content = gc AND upc.user.id = :userId
        ORDER BY gc.name
        """)
    List<Object[]> findAllWithPinnedFlag(@Param("userId") Long userId);
}
