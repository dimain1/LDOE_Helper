-- ============================================================
-- DDL — LDOE-Helper Database Schema
-- СУБД: PostgreSQL 15
-- Схема восстановлена по JPA-аннотациям (ddl-auto=update)
-- ============================================================

-- ------------------------------------------------------------
-- Справочник ролей
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_role (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Начальные данные
INSERT INTO user_role (name) VALUES ('USER')  ON CONFLICT DO NOTHING;
INSERT INTO user_role (name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------
-- Пользователи
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL    PRIMARY KEY,
    login    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    role_id  BIGINT       NOT NULL
        REFERENCES user_role(id)
);

-- ------------------------------------------------------------
-- Персональные настройки (1:1 с users)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    UNIQUE
        REFERENCES users(id),
    preferences JSONB
);

-- ------------------------------------------------------------
-- Refresh-токены JWT (1:1 с users, CASCADE при удалении)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT
        REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL
);

-- ------------------------------------------------------------
-- Типы игрового контента (справочник)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS content_type (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- ------------------------------------------------------------
-- Игровой контент
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS game_content (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    image       VARCHAR(512),
    attributes  JSONB
);

-- ------------------------------------------------------------
-- Связь контент ↔ тип (M:N)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS type_to_content (
    content_id BIGINT NOT NULL
        REFERENCES game_content(id) ON DELETE CASCADE,
    type_id    BIGINT NOT NULL
        REFERENCES content_type(id) ON DELETE CASCADE,
    PRIMARY KEY (content_id, type_id)
);

-- ------------------------------------------------------------
-- Закреплённый контент пользователя (M:N)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_pinned_content (
    user_id    BIGINT NOT NULL
        REFERENCES users(id) ON DELETE CASCADE,
    content_id BIGINT NOT NULL
        REFERENCES game_content(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, content_id)
);

-- ------------------------------------------------------------
-- Шаблоны событий
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS event_template (
    id          BIGSERIAL    PRIMARY KEY,
    creator_id  BIGINT
        REFERENCES users(id) ON DELETE SET NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    image       VARCHAR(512),
    duration    BIGINT       NOT NULL  -- длительность в секундах
);

-- ------------------------------------------------------------
-- События пользователя
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL
        REFERENCES users(id) ON DELETE CASCADE,
    template_id BIGINT
        REFERENCES event_template(id) ON DELETE SET NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    image       VARCHAR(512),
    start_time  TIMESTAMP    NOT NULL,
    end_time    TIMESTAMP    NOT NULL
);
