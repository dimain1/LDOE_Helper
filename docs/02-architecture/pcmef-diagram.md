# PCMEF-диаграмма

## Описание

Диаграмма пакетов PCMEF показывает разбиение системы на слои и пакеты в соответствии с архитектурным паттерном PCMEF (Presentation – Control – Mediator – Entity – Foundation). Каждый слой имеет чёткую ответственность; зависимости направлены строго сверху вниз.

---

## Серверная сторона (Spring Boot)

| Слой | Пакет | Ответственность |
|------|-------|----------------|
| **Control** | `com.example.backend.control` | REST-контроллеры: принимают HTTP-запросы, валидируют входные DTO, делегируют в Mediator |
| **Mediator** | `com.example.backend.mediator` | Сервисы бизнес-логики: `AuthService`, `UserService`, `EventService`, `EventTemplateService`, `GameContentService`, `JwtService`, `ImageStorageService` |
| **Entity** | `com.example.backend.entity` | JPA-сущности: `User`, `UserRole`, `Settings`, `GameContent`, `ContentType`, `Event`, `EventTemplate`, `UserPinnedContent`, `RefreshToken` |
| **Foundation** | `com.example.backend.foundation.repository` | Spring Data JPA-репозитории для каждой сущности |
| *(Security)* | `com.example.backend.security` | Сквозной слой: `JwtFilter`, `SecurityConfig`, `CustomUserDetailsService` |
| *(DTO)* | `com.example.backend.dto` | Объекты передачи данных между Control и клиентом |

---

## Клиентская сторона (Android / Kotlin)

| Слой | Описание |
|------|---------|
| **Presentation** | Jetpack Compose — экраны, компоненты UI, overlay-сервис |
| **ViewModel** | Koin DI — `ViewModel`-классы, управление состоянием экрана |
| **Repository** | Паттерн Repository — абстракция над источниками данных |
| **Local Cache** | Room Database — локальное хранение событий, шаблонов и игрового контента |
| **API Client** | Retrofit — HTTP-клиент к REST API, OkHttp-интерсептор для JWT |

---

## Диаграмма

![Диаграмма пакетов PCMEF](./images/PCMEF-diagramm.png)

---

## Правила зависимостей

```
Presentation → ViewModel → Repository → Room (local)
                                      → Retrofit → REST API

Control → Mediator → Foundation → Entity
Security (JwtFilter) ──→ (cross-cutting, применяется до Control)
```

Запрещённые зависимости: Foundation → Mediator, Entity → Control.
