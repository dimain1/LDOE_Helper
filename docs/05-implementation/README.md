# Этап 5 — Реализация слоёв PCMEF

## Описание

Документ описывает реализацию архитектурного паттерна PCMEF в обеих частях системы LDOE-Helper: серверной (Spring Boot) и мобильной (Android / Kotlin). Для каждой части приведено соответствие пакетов слоям, правила зависимостей и проверка их соблюдения.

---

## Правила зависимостей PCMEF

PCMEF (Presentation – Control – Mediator – Entity – Foundation) определяет жёсткие правила направления зависимостей между слоями.

**Разрешённые направления:**

```
Presentation  ──→  Control
Control       ──→  Mediator
Mediator      ──→  Entity
Mediator      ──→  Foundation
Foundation    ──→  Entity
```

**Ключевые ограничения:**

| Правило | Обоснование |
|---------|------------|
| Зависимости направлены только вниз | Нижние слои не знают о верхних — это обеспечивает независимость Foundation и возможность её замены |
| Control зависит от Mediator через интерфейсы | Control не создаёт объекты сервисов напрямую — внедрение зависимостей (DI) обеспечивает подменяемость |
| Foundation не зависит от Mediator и Control | Репозитории и DAO работают только с Entity — они не несут бизнес-логику |
| Entity доступен из любого слоя | Доменные объекты используются для передачи данных между слоями |
| Presentation не содержит бизнес-логики | Вся обработка данных вынесена в Control и Mediator |

**Запрещённые зависимости:**

```
Foundation  ──✗──  Mediator
Foundation  ──✗──  Control
Foundation  ──✗──  Presentation
Mediator    ──✗──  Control
Mediator    ──✗──  Presentation
Control     ──✗──  Presentation
```

---

## Серверная часть (Spring Boot)

Пакетная структура серверной части спроектирована с явным именованием, отражающим слои PCMEF.

### Соответствие пакетов слоям

| Слой PCMEF | Пакет | Классы |
|---|---|---|
| **Presentation** | `dto/` | `AuthResponse`, `LoginRequest`, `RegisterRequest`, `RefreshRequest`, `EventDto`, `EventCreateRequest`, `EventUpdateRequest`, `GameContentDto`, `GameContentRequest`, `ContentTypeDto`, `EventTemplateDto`, `EventTemplateCreateRequest`, `EventTemplateUpdateRequest`, `UserDto`, `UpdateUserRequest`, `ChangePasswordRequest` |
| **Control** | `control/` | `AuthController`, `EventController`, `EventTemplateController`, `GameContentController`, `UserController` |
| **Mediator** | `mediator/` | `AuthService`, `EventService`, `EventTemplateService`, `GameContentService`, `UserService`, `JwtService`, `ImageStorageService` |
| **Entity** | `entity/` | `User`, `UserRole`, `Settings`, `Event`, `EventTemplate`, `GameContent`, `ContentType`, `UserPinnedContent`, `RefreshToken`, `JsonConverter` |
| **Foundation** | `foundation/repository/` | `UserRepository`, `UserRoleRepository`, `EventRepository`, `EventTemplateRepository`, `GameContentRepository`, `ContentTypeRepository`, `UserPinnedContentRepository`, `RefreshTokenRepository`, `SettingsRepository` |

**Сквозные компоненты** (не принадлежат конкретному слою):

| Пакет | Назначение |
|-------|-----------|
| `security/` | `JwtFilter`, `SecurityConfig`, `CustomUserDetailsService`, `MethodSecurityConfig` — инфраструктура аутентификации и авторизации, применяется до обращения к Control |
| `config/` | `WebConfig`, `DataInitializer` — конфигурация Spring-контекста |

### Проверка соблюдения правил

Пример трассировки запроса `POST /auth/login`:

```
AuthController (Control)
    │  принимает LoginRequest (DTO/Presentation)
    │  вызывает
    ▼
AuthService (Mediator)
    │  читает User (Entity) через
    ▼
UserRepository (Foundation)  ──→  User (Entity)
    │
    └─ возвращает AuthResponse (DTO/Presentation)
```

Проверка зависимостей:

| Зависимость | Статус |
|-------------|--------|
| `AuthController` → `AuthService` |  C → M |
| `AuthController` → `JwtService` |  C → M |
| `AuthService` → `UserRepository` |  M → F |
| `AuthService` → `User`, `UserRole`, `RefreshToken` |  M → E |
| `UserRepository` → `User` |  F → E |
| `JwtFilter` → `JwtService` |  сквозной → M |

---

## Мобильная часть (Android / Kotlin)

Мобильный клиент реализован по архитектурному паттерну MVVM, который является стандартной реализацией PCMEF для мобильной траектории на платформе Android.

### Соответствие пакетов слоям

| Слой PCMEF | Пакет | Классы |
|---|---|---|
| **Presentation** | `presentation/` | Экраны: `EventScreen`, `HomeScreen`, `InfoScreen`, `TemplateScreen`, `SettingsScreen`; компоненты: `EventItem`, `TemplateItem`, `EventCreateModal`, `EventViewDetails`, `TemplateViewDetails`, `AppHeader`, `MyBottomAppBar`; overlay: `OverlayService`, `OverlayController`, `OverlayEventsScreen`, `OverlayCreateScreen`, `OverlayMenuScreen` |
| **Control** | `state_management/viewModel/` | `EventViewModel`, `EventTemplateViewModel`, `HomeViewModel`, `InfoViewModel`, `SettingsViewModel`, `SharedAppViewModel` |
| **Mediator** | `state_management/repository/interfaces/` + `state_management/repository/implementation/` | Интерфейсы: `EventRepository`, `EventTemplateRepository`, `AuthRepository`, `GameContentRepository`, `ContentTypeRepository`, `UserRepository`, `SharedPreferencesRepository`; реализации: `EventRepositoryImpl`, `EventTemplateRepositoryImpl`, `AuthRepositoryImpl`, `GameContentRepositoryImpl`, `ContentTypeRepositoryImpl`, `UserRepositoryImpl`, `SharedPreferencesRepositoryImpl` |
| **Entity** | `state_management/entity/` | `User`, `Event`, `EventTemplate`, `GameContent`, `ContentType`, `Settings` |
| **Foundation** | `local_cache/dao/` + `local_cache/entity/` + `api_client/` + `sync/` | DAO: `EventDao`, `EventTemplateDao`, `UserDao`, `GameContentDao`, `ContentTypeDao`; Room-сущности: `EventEntity`, `EventTemplateEntity`, `UserEntity`, `GameContentEntity`, `ContentTypeEntity`, `UserPinnedGameContentCrossRef`, `GameContentTypeCrossRef`; сеть: `ApiService`, `NetworkConfig`, `AuthInterceptor`, `TokenAuthenticator`, `TokenStorage`; синхронизация: `SyncWorker`; БД: `AppDatabase` |

**Сквозные компоненты:**

| Пакет | Назначение |
|-------|-----------|
| `di/` | `databaseModule`, `networkModule`, `repositoryModule`, `viewModelModule`, `preferencesModule`, `notificationModule` — Koin-модули, связывающие интерфейсы с реализациями |
| `ui/theme/` | `Theme`, `Color`, `Type` — тема Material 3 |

### Подслои Foundation

Foundation мобильного клиента разделён на два независимых источника данных:

```
Foundation
├── Локальный (local_cache/)
│     ├── AppDatabase        — Room-база данных
│     ├── dao/               — DAO-интерфейсы (EventDao, UserDao, …)
│     ├── entity/            — Room-сущности (*Entity)
│     └── converters/        — маппинг между Room-сущностями и доменными объектами
│
└── Удалённый (api_client/)
      ├── ApiService         — Retrofit-интерфейс HTTP-эндпоинтов
      ├── NetworkConfig      — OkHttp + Retrofit конфигурация
      ├── AuthInterceptor    — добавляет Bearer-токен в заголовки запросов
      ├── TokenAuthenticator — обновляет access-токен при получении 401
      ├── TokenStorage       — хранение токенов в DataStore
      └── dto/               — DTO для сериализации/десериализации (EventDto, AuthDto, …)
```

Реализации Mediator (`*RepositoryImpl`) координируют оба подслоя: сначала обновляют локальный кэш, затем отправляют запрос к API. При сетевой ошибке операция помечается как `PENDING_*` и откладывается для последующей синхронизации через `SyncWorker`.

### Проверка соблюдения правил

Пример трассировки действия «Создать событие»:

```
EventScreen (Presentation)
    │  вызывает onAction(EventAction.OnFormAction(ValidateAndSave))
    ▼
EventViewModel (Control)
    │  валидирует форму, создаёт Event (Entity)
    │  вызывает eventRepository.addEvent(event)
    ▼
EventRepositoryImpl (Mediator)
    │  конвертирует Event → EventEntity
    │  вызывает eventDao.addEvent(entity)       ──→  EventDao (Foundation / local)
    │  вызывает api.createEvent(dto)             ──→  ApiService (Foundation / remote)
    ▼
Event (Entity) — возвращается как localId: Long
    │
    └─ EventViewModel вызывает alarmScheduler.scheduleFinish(event)
```

Проверка зависимостей:

| Зависимость | Статус |
|-------------|--------|
| `EventScreen` → `EventViewModel` |  P → C |
| `EventViewModel` → `EventRepository` (интерфейс) |  C → M (через абстракцию) |
| `EventRepositoryImpl` → `EventDao` |  M → F |
| `EventRepositoryImpl` → `ApiService` |  M → F |
| `EventRepositoryImpl` → `Event` |  M → E |
| `EventDao` → `EventEntity` |  F → E (Room-сущность) |

### Инверсия зависимостей

`EventViewModel` зависит от интерфейса `EventRepository`, а не от `EventRepositoryImpl`. Связывание выполняется Koin в `repositoryModule`:

```kotlin
// di/repositoryModule.kt
val repositoryModule = module {
    single<EventRepository> { EventRepositoryImpl(get(), get(), get()) }
    // …
}
```

Это подтверждается наличием тестового дублёра `FakeEventRepository` в `ViewModelTest/`, который подменяет реализацию без изменения ViewModel.

---


## Сводная таблица соответствия

| Слой PCMEF | Серверная часть | Мобильная часть |
|---|---|---|
| **Presentation** | `dto/` — DTO для API-границы | `presentation/` — Jetpack Compose экраны и компоненты |
| **Control** | `control/` — REST-контроллеры | `state_management/viewModel/` — ViewModels |
| **Mediator** | `mediator/` — Spring-сервисы | `state_management/repository/` — репозитории |
| **Entity** | `entity/` — JPA-сущности | `state_management/entity/` — доменные модели |
| **Foundation** | `foundation/repository/` — Spring Data JPA | `local_cache/dao/` + `api_client/` — Room + Retrofit |
