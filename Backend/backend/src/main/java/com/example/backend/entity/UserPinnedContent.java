package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_pinned_content")
@IdClass(UserPinnedContent.PK.class)
public class UserPinnedContent {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private GameContent content;

    public UserPinnedContent() {}

    public UserPinnedContent(User user, GameContent content) {
        this.user = user;
        this.content = content;
    }

    public User getUser()               { return user; }
    public void setUser(User user)      { this.user = user; }

    public GameContent getContent()             { return content; }
    public void setContent(GameContent content) { this.content = content; }

    // ── Composite PK ─────────────────────────────────────────────────────────

    public static class PK implements Serializable {
        private Long user;
        private Long content;

        public PK() {}

        public PK(Long user, Long content) {
            this.user = user;
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(user, pk.user) && Objects.equals(content, pk.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, content);
        }
    }
}
