# Этап 9 — API

## Описание

REST API серверной части системы LDOE-Helper. Документация генерируется автоматически через SpringDoc OpenAPI (Swagger UI).

**Базовый URL:** `http://localhost:8080`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Аутентификация

Большинство эндпоинтов требуют JWT в заголовке:

```
Authorization: Bearer <access_token>
```

Access-токен выдаётся при входе (`POST /auth/login`) или регистрации и действует **20 минут**. По истечении обновляется через `POST /auth/refresh` без повторного ввода пароля.

---

## Swagger UI

![Swagger UI](./images/swagger.png)

---

## Группы эндпоинтов

### Auth — `/auth`

Публичные эндпоинты. JWT не требуется.

| Метод | Путь | Описание | Доступ |
|-------|------|---------|--------|
| `POST` | `/auth/register` | Регистрация нового пользователя | Публичный |
| `POST` | `/auth/login` | Вход в систему, выдача токенов | Публичный |
| `POST` | `/auth/logout` | Инвалидация refresh-токена | Публичный |
| `POST` | `/auth/refresh` | Обновление пары токенов | Публичный |

#### `POST /auth/register`

```json
// Request body
{
  "login": "alice",
  "password": "secret123",
  "email": "alice@mail.com"
}

// Response — 200 OK
1  // id созданного пользователя
```

Первый зарегистрированный пользователь автоматически получает роль `ADMIN`, все последующие — `USER`.

#### `POST /auth/login`

```json
// Request body
{
  "login": "alice",
  "password": "secret123"
}

// Response — 200 OK
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

| Ошибка | HTTP |
|--------|------|
| Неверный логин или пароль | 401 |

#### `POST /auth/refresh`

```json
// Request body
{
  "refreshToken": "eyJhbGci..."
}

// Response — 200 OK
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

| Ошибка | HTTP |
|--------|------|
| Токен не найден | 401 |
| Токен истёк (удаляется из БД) | 401 |

#### `POST /auth/logout`

```json
// Request body
{
  "refreshToken": "eyJhbGci..."
}

// Response — 200 OK (пустое тело)
```

---

### Events — `/events`

Управление событиями текущего пользователя. Все эндпоинты требуют JWT.

| Метод | Путь | Описание | Доступ |
|-------|------|---------|--------|
| `GET` | `/events` | Список всех событий пользователя | USER, ADMIN |
| `POST` | `/events` | Создать событие | USER, ADMIN |
| `PUT` | `/events/{id}` | Обновить своё событие | USER, ADMIN |
| `DELETE` | `/events/{id}` | Удалить своё событие | USER, ADMIN |

#### Схема `EventDto`

```json
{
  "id": 1,
  "userId": 3,
  "templateId": 2,        // null если без шаблона
  "name": "Рейд",
  "description": "Описание",
  "imageUrl": "/images/abc.png",
  "startTime": "2026-06-04T15:00:00Z",
  "endTime": "2026-06-04T16:00:00Z"
}
```

#### `POST /events` — тело запроса

```json
{
  "templateId": 2,         // nullable
  "name": "Рейд",
  "description": "Описание",
  "imageUrl": null,
  "startTime": "2026-06-04T15:00:00Z",
  "endTime": "2026-06-04T16:00:00Z"
}
```

#### `PUT /events/{id}` — частичное обновление

Все поля опциональны. Поля со значением `null` не перезаписывают существующие данные.

```json
{
  "name": "Новое имя",
  "description": null,
  "startTime": null,
  "endTime": "2026-06-04T17:00:00Z"
}
```

| Ошибка | HTTP |
|--------|------|
| Событие не найдено | 500 (RuntimeException) |
| Попытка изменить чужое событие | 500 (RuntimeException) |

---

### Templates — `/templates`

Управление шаблонами событий. Все эндпоинты требуют JWT. Запросы с изображением — `multipart/form-data`.

| Метод | Путь | Описание | Доступ |
|-------|------|---------|--------|
| `GET` | `/templates` | Шаблоны текущего пользователя | USER, ADMIN |
| `POST` | `/templates` | Создать шаблон | USER, ADMIN |
| `PUT` | `/templates/{id}` | Обновить свой шаблон | USER, ADMIN |
| `DELETE` | `/templates/{id}` | Удалить свой шаблон | USER, ADMIN |

#### Схема `EventTemplateDto`

```json
{
  "id": 2,
  "creatorId": 1,
  "name": "Рейдовый шаблон",
  "description": "Описание",
  "imageUrl": "/images/template.png",
  "duration": 3600000    // миллисекунды
}
```

#### `POST /templates` — `multipart/form-data`

| Поле | Тип | Обязательный | Описание |
|------|-----|:------------:|---------|
| `request` | JSON part | + | `{ "name", "description", "duration" }` |
| `image` | file part | - | Изображение (max 5 MB) |

#### `PUT /templates/{id}` — частичное обновление, `multipart/form-data`

Поля `name`, `description`, `duration` опциональны. `null` не перезаписывает.

---

### Content — `/content`

Игровой контент и типы. Чтение доступно всем авторизованным, создание/удаление — только `ADMIN`.

| Метод | Путь | Описание | Доступ |
|-------|------|---------|--------|
| `GET` | `/content` | Весь контент с флагом `pinned` | USER, ADMIN |
| `POST` | `/content` | Создать контент | **ADMIN** |
| `GET` | `/content/types` | Все типы контента | USER, ADMIN |
| `POST` | `/content/types` | Создать тип | **ADMIN** |
| `DELETE` | `/content/types/{id}` | Удалить тип | **ADMIN** |
| `POST` | `/content/{id}/pin` | Закрепить контент | USER, ADMIN |
| `DELETE` | `/content/{id}/pin` | Открепить контент | USER, ADMIN |

#### Схема `GameContentDto`

```json
{
  "id": 10,
  "name": "АК-74",
  "description": "Штурмовая винтовка",
  "imageUrl": "/images/ak74.png",
  "attributes": {
    "damage": 42,
    "range": "medium"
  },
  "types": [
    { "id": 1, "name": "All" },
    { "id": 3, "name": "Weapons" }
  ],
  "pinned": true
}
```

#### `POST /content` — `multipart/form-data`

| Поле | Тип | Обязательный | Описание |
|------|-----|:------------:|---------|
| `request` | JSON part | + | `{ "name", "description", "typeIds", "attributes" }` |
| `file` | file part | - | Изображение (max 5 MB) |

Тип `"All"` добавляется автоматически к каждому контенту.

#### Схема `ContentTypeDto`

```json
{ "id": 3, "name": "Weapons" }
```

---

### Users — `/users`

Профиль текущего пользователя. Все эндпоинты требуют JWT.

| Метод | Путь | Описание | Доступ |
|-------|------|---------|--------|
| `GET` | `/users/me` | Данные текущего пользователя | USER, ADMIN |
| `PUT` | `/users/me` | Обновить логин / email | USER, ADMIN |

#### Схема `UserDto`

```json
{
  "id": 1,
  "login": "alice",
  "email": "alice@mail.com",
  "admin": true
}
```

#### `PUT /users/me`

```json
// Поля опциональны. Пустые и null-значения игнорируются.
{
  "login": "alice_new",
  "email": "new@mail.com"
}
```
---

## Сводная таблица эндпоинтов

| Метод | Путь | Доступ |
|-------|------|--------|
| POST | `/auth/register` | Публичный |
| POST | `/auth/login` | Публичный |
| POST | `/auth/logout` | Публичный |
| POST | `/auth/refresh` | Публичный |
| GET | `/events` | USER, ADMIN |
| POST | `/events` | USER, ADMIN |
| PUT | `/events/{id}` | USER, ADMIN |
| DELETE | `/events/{id}` | USER, ADMIN |
| GET | `/templates` | USER, ADMIN |
| POST | `/templates` | USER, ADMIN |
| PUT | `/templates/{id}` | USER, ADMIN |
| DELETE | `/templates/{id}` | USER, ADMIN |
| GET | `/content` | USER, ADMIN |
| POST | `/content` | **ADMIN** |
| GET | `/content/types` | USER, ADMIN |
| POST | `/content/types` | **ADMIN** |
| DELETE | `/content/types/{id}` | **ADMIN** |
| POST | `/content/{id}/pin` | USER, ADMIN |
| DELETE | `/content/{id}/pin` | USER, ADMIN |
| GET | `/users/me` | USER, ADMIN |
| PUT | `/users/me` | USER, ADMIN |
