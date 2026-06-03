# Спецификация методов

## Описание

Документ описывает контракты публичных методов сервисного слоя (Mediator) системы LDOE-Helper. Для каждого метода указаны сигнатура, предусловия, постусловия, исключения и краткий алгоритм. Все методы находятся в пакете `com.example.backend.mediator`.

---

## AuthService

### `login(LoginRequest request) → AuthResponse`

| Поле | Значение |
|------|---------|
| **Класс** | `AuthService` |
| **Транзакция** | Нет |
| **Доступ** | Публичный (`/auth/login`) |

**Входные данные:**
| Параметр | Тип | Описание |
|---------|-----|---------|
| `request.login` | `String` | Логин пользователя |
| `request.password` | `String` | Пароль в открытом виде |

**Выходные данные:** `AuthResponse { accessToken, refreshToken }`

**Предусловия:**
- Таблица `users` содержит запись с указанным логином

**Постусловия:**
- В таблице `refresh_tokens` создана запись для данного пользователя
- Клиент получил валидную пару токенов

**Алгоритм:**
1. Найти пользователя по логину (`userRepository.findByLogin`)
2. Проверить пароль через `BCryptPasswordEncoder.matches`
3. Сгенерировать access-токен (TTL 20 мин) и refresh-токен (TTL 7 дней)
4. Сохранить refresh-токен в `refresh_tokens`
5. Вернуть `AuthResponse`

**Исключения:**
| Условие | Исключение | HTTP |
|---------|-----------|------|
| Пользователь не найден | `ResponseStatusException(UNAUTHORIZED)` | 401 |
| Неверный пароль | `ResponseStatusException(UNAUTHORIZED)` | 401 |

---

### `register(RegisterRequest request) → User`

| Поле | Значение |
|------|---------|
| **Класс** | `AuthService` |
| **Транзакция** | `@Transactional` |
| **Доступ** | Публичный (`/auth/register`) |

**Входные данные:**
| Параметр | Тип | Описание |
|---------|-----|---------|
| `request.login` | `String` | Желаемый логин |
| `request.password` | `String` | Пароль в открытом виде |
| `request.email` | `String` | Email пользователя |

**Выходные данные:** сохранённая сущность `User` (с присвоенным `id`)

**Предусловия:**
- Логин не занят другим пользователем
- Роли `USER` и `ADMIN` существуют в таблице `user_role`

**Постусловия:**
- В таблице `users` создана новая запись
- Если это первый пользователь — роль `ADMIN`, иначе — `USER`
- Пароль сохранён в виде BCrypt-хэша

**Алгоритм:**
1. Проверить уникальность логина (`userRepository.findByLogin`)
2. Определить роль: `userRepository.count() == 0` → `"ADMIN"`, иначе `"USER"`
3. Найти роль по имени (`userRoleRepository.findByName`)
4. Создать `User` с BCrypt-хэшем пароля
5. Сохранить и вернуть

**Исключения:**
| Условие | Исключение | HTTP |
|---------|-----------|------|
| Логин уже занят | `ResponseStatusException(CONFLICT)` | 409 |
| Роль не найдена в БД | `ResponseStatusException(INTERNAL_SERVER_ERROR)` | 500 |

---

### `refresh(RefreshRequest request) → AuthResponse`

**Входные данные:** `request.refreshToken` — строка refresh-токена

**Постусловия:** старый refresh-токен заменён новым; выдана новая пара токенов

**Исключения:**
| Условие | HTTP |
|---------|------|
| Токен не найден в БД | 401 |
| Токен просрочен (удаляется из БД) | 401 |

---

### `logout(RefreshRequest request) → void`

**Алгоритм:** `refreshTokenRepository.deleteByToken(token)` — инвалидирует сессию.

---

## EventService

### `getMyEvents(String login) → List<EventDto>`

| Поле | Значение |
|------|---------|
| **Транзакция** | Нет (read-only) |

**Алгоритм:**
1. Найти `User` по логину
2. `eventRepository.findByUser(user)` → `List<Event>`
3. Маппинг через `EventService.toDto()`

---

### `create(String login, EventCreateRequest request) → EventDto`

| Поле | Значение |
|------|---------|
| **Транзакция** | `@Transactional` |

**Входные данные:**
| Параметр | Тип | Описание |
|---------|-----|---------|
| `login` | `String` | Логин из JWT (`@AuthenticationPrincipal`) |
| `request.name` | `String` | Название события |
| `request.startTime` | `Instant` | Время начала отсчёта |
| `request.endTime` | `Instant` | Время срабатывания уведомления |
| `request.templateId` | `Long?` | ID шаблона (nullable) |
| `request.description` | `String?` | Описание (nullable) |

**Постусловия:**
- В таблице `events` создана запись с привязкой к пользователю
- Если `templateId` передан — событие связано с шаблоном

**Алгоритм:**
1. Найти `User` по логину
2. Если `templateId != null` — найти `EventTemplate` по id
3. Создать и заполнить `Event`
4. `eventRepository.save(event)` → `Event`
5. Вернуть `EventService.toDto(event)`

**Исключения:**
| Условие | Исключение |
|---------|-----------|
| Пользователь не найден | `RuntimeException("User not found")` |
| Шаблон не найден | `RuntimeException("Template not found: id")` |

---

### `update(String login, Long eventId, EventUpdateRequest request) → EventDto`

| Поле | Значение |
|------|---------|
| **Транзакция** | `@Transactional` |

**Предусловия:** событие принадлежит пользователю с данным логином

**Алгоритм:**
1. `eventRepository.findById(eventId)` → `Event`
2. Проверить `event.user.login == login` (иначе `RuntimeException("Access denied")`)
3. Применить non-null поля из `request` (partial update)
4. `eventRepository.save(event)` → вернуть `EventDto`

---

### `delete(String login, Long eventId) → void`

**Предусловия:** событие принадлежит данному пользователю

**Алгоритм:** найти событие → проверить владельца → `eventRepository.delete(event)`

---

## GameContentService

### `getAll(String login) → List<GameContentDto>`

**Алгоритм:**
1. Найти `User` по логину
2. `gameContentRepository.findAllWithPinnedFlag(userId)` → `List<Object[]>`
   - каждая строка: `[GameContent, Boolean pinned]`
3. Маппинг через `GameContentService.toDto(gc, pinned)`

---

### `create(GameContentRequest request, MultipartFile file) → GameContentDto`

| Поле | Значение |
|------|---------|
| **Транзакция** | `@Transactional` |
| **Доступ** | Только `ROLE_ADMIN` (`@PreAuthorize`) |

**Входные данные:**
| Параметр | Тип | Описание |
|---------|-----|---------|
| `request.name` | `String` | Название объекта |
| `request.description` | `String?` | Описание |
| `request.attributes` | `Map<String,Object>` | JSONB-атрибуты |
| `request.typeIds` | `List<Long>?` | ID типов контента |
| `file` | `MultipartFile?` | Изображение (max 5 MB) |

**Постусловия:**
- Файл сохранён в `uploads/{uuid}.ext` (если передан)
- Запись создана в `game_content` и связана с типами через `type_to_content`
- Тип `"All"` всегда добавляется автоматически

**Алгоритм:**
1. Если `file != null` → `imageStorageService.saveImage(file)` → `imagePath`
2. Создать `GameContent`, установить поля
3. Найти/создать тип `"All"`, добавить в `Set<ContentType>`
4. Добавить типы из `request.typeIds`
5. `gameContentRepository.save(content)` → вернуть `GameContentDto`

---

### `pin(String login, Long contentId) → void`

**Алгоритм:**
1. Найти `User` и `GameContent`
2. Проверить: `pinnedRepository.existsByUserAndContent(user, content)`
3. Если не закреплён → `pinnedRepository.save(new UserPinnedContent(user, content))`

---

### `unpin(String login, Long contentId) → void`

**Алгоритм:** найти пользователя и контент → `pinnedRepository.deleteByUserAndContent(user, content)`

---

## Вспомогательные методы (static)

### `EventService.toDto(Event event) → EventDto`

Маппинг `Event` → `EventDto`. Если `event.template == null` → `templateId = null`.

### `GameContentService.toDto(GameContent gc, boolean pinned) → GameContentDto`

Маппинг `GameContent` → `GameContentDto` с флагом `pinned` и коллекцией `Set<ContentTypeDto>`.
