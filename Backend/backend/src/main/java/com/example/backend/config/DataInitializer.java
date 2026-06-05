package com.example.backend.config;

import com.example.backend.entity.ContentType;
import com.example.backend.entity.GameContent;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.ContentTypeRepository;
import com.example.backend.foundation.repository.GameContentRepository;
import com.example.backend.foundation.repository.UserRoleRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Инициализирует базовое состояние БД при первом запуске.
 * Запускается после полной инициализации Spring-контекста (в том числе
 * после @PostConstruct в GameContentService, который создаёт тип "All").
 *
 * Безопасен для повторного запуска: все операции проверяют существование
 * записи перед вставкой.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRoleRepository    userRoleRepository;
    private final ContentTypeRepository contentTypeRepository;
    private final GameContentRepository gameContentRepository;

    public DataInitializer(UserRoleRepository userRoleRepository,
                           ContentTypeRepository contentTypeRepository,
                           GameContentRepository gameContentRepository) {
        this.userRoleRepository    = userRoleRepository;
        this.contentTypeRepository = contentTypeRepository;
        this.gameContentRepository = gameContentRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        InitData data = new ObjectMapper().readValue(
                new ClassPathResource("init-data.json").getInputStream(),
                InitData.class
        );

        initRoles(data.roles);
        initContentTypes(data.contentTypes);
        initGameContent(data.gameContent);
    }

    // ── Роли ─────────────────────────────────────────────────────────────────

    private void initRoles(List<String> roles) {
        for (String name : roles) {
            if (userRoleRepository.findByName(name).isEmpty()) {
                UserRole role = new UserRole();
                role.setName(name);
                userRoleRepository.save(role);
                log.info("DataInitializer: создана роль '{}'", name);
            }
        }
    }

    // ── Типы контента ─────────────────────────────────────────────────────────

    private void initContentTypes(List<String> typeNames) {
        for (String name : typeNames) {
            if (contentTypeRepository.findByName(name).isEmpty()) {
                ContentType type = new ContentType();
                type.setName(name);
                contentTypeRepository.save(type);
                log.info("DataInitializer: создан тип контента '{}'", name);
            }
        }
    }

    // ── Игровой контент ───────────────────────────────────────────────────────

    private void initGameContent(List<GameContentEntry> entries) {
        if (gameContentRepository.count() > 0) {
            log.info("DataInitializer: game_content не пуста, пропуск инициализации контента");
            return;
        }

        ContentType allType = contentTypeRepository.findByName("All").orElse(null);

        for (GameContentEntry entry : entries) {
            GameContent content = new GameContent();
            content.setName(entry.name);
            content.setDescription(entry.description);
            content.setAttributes(entry.attributes);

            Set<ContentType> types = new HashSet<>();
            if (allType != null) types.add(allType);

            for (String typeName : entry.typeNames) {
                contentTypeRepository.findByName(typeName).ifPresent(types::add);
            }
            content.setTypes(types);

            gameContentRepository.save(content);
            log.info("DataInitializer: добавлен контент '{}'", entry.name);
        }
    }

    // ── Внутренние DTO ────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class InitData {
        public List<String>           roles;
        public List<String>           contentTypes;
        public List<GameContentEntry> gameContent;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GameContentEntry {
        public String              name;
        public String              description;
        public List<String>        typeNames;
        public Map<String, Object> attributes;
    }
}
