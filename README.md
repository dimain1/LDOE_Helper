# LDOE-Helper

**Автор:** Черников Дмитрий Дмитриевич  
**Группа:** [ПИЖ-б-о-23-2]  
**Траектория:** Mobile  
**Дата начала:** 01.02.2026  
**Дата сдачи:** 29.05.2026

---

## Описание проекта

LDOE-Helper — мобильное Android-приложение с серверной частью для игроков в *Last Day On Earth: Survival*. Система решает две ключевые проблемы: отсутствие структурированного справочника по игровым механикам, ресурсам и локациям, а также отсутствие гибкого инструмента отслеживания внутриигровых событий. Приложение позволяет создавать таймеры с push-уведомлениями и обращаться к базе знаний прямо во время игры через плавающий overlay-виджет.

---

## Траектория выполнения

- [ ] Веб-разработка
- [ ] Десктоп
- [x] **Мобильная** (Android + Spring Boot)
- [ ] Enterprise

---

## Технологический стек

| Компонент | Технология |
|-----------|-----------|
| Мобильный клиент | Kotlin, Jetpack Compose, Room, Retrofit, Koin |
| Бэкенд | Java 17, Spring Boot 4.0.6, Spring Security |
| База данных | PostgreSQL 15, Hibernate 7, Spring Data JPA |
| Локальная БД (клиент) | Room (SQLite) |
| API | REST, OpenAPI / Swagger (SpringDoc) |
| Безопасность | JWT (JJWT 0.11.5), BCrypt |
| Сборка | Gradle 9.x (backend), Gradle (Android) |
| Контейнеризация | Docker, Docker Compose |
| Тестирование | JUnit 5 (Jupiter 6), Mockito 5, AssertJ, JaCoCo |

---

## Требования к окружению

| Требование | Версия |
|-----------|--------|
| Java JDK | 17+ |
| Docker Desktop | 20+ |
| Docker Compose | 2.x |
| Android Studio | 2023.x+ |
| Android SDK | API 29+ (Android 10) |

---

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/dimain1/LDOE_Helper.git
cd ldoe-helper
```

### 2. Настройка окружения

Создайте файл `.env` в корне проекта:

```env
DB_PORT=5432
DB_NAME=LDOE_helper
DB_USERNAME=test_role
DB_PASSWORD=admin
SERVER_PORT=8080
JWT_SECRET=my_super_secret_key_which_must_be_at_least_32_chars_long
JWT_ACCESS_EXPIRATION_MS=1200000
JWT_REFRESH_EXPIRATION_MS=604800000
```

### 3. Сборка и запуск через Docker Compose

```bash
# Сборка JAR
cd Backend/backend
./gradlew bootJar
cd ../..

# Запуск всех сервисов
docker-compose up --build -d
```

После запуска:
- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

### 4. Запуск только базы данных (для разработки)

```bash
docker-compose up -d db
# Backend запускается из IDE или ./gradlew bootRun
```

### 5. Сборка мобильного приложения

Откройте папку `KotlinClient` в Android Studio и соберите APK через **Build → Build APK**.  
Перед сборкой укажите IP сервера в `NetworkConfig.kt`.

---

## API Endpoints

Базовый URL: `http://localhost:8080`

| Метод | Эндпоинт | Описание | Доступ |
|-------|---------|---------|--------|
| POST | `/auth/register` | Регистрация | Публичный |
| POST | `/auth/login` | Вход, выдача токенов | Публичный |
| POST | `/auth/logout` | Выход | Публичный |
| POST | `/auth/refresh` | Обновление токенов | Публичный |
| GET | `/events` | Список событий | USER, ADMIN |
| POST | `/events` | Создать событие | USER, ADMIN |
| PUT | `/events/{id}` | Обновить событие | USER, ADMIN |
| DELETE | `/events/{id}` | Удалить событие | USER, ADMIN |
| GET | `/templates` | Список шаблонов | USER, ADMIN |
| POST | `/templates` | Создать шаблон | USER, ADMIN |
| PUT | `/templates/{id}` | Обновить шаблон | USER, ADMIN |
| DELETE | `/templates/{id}` | Удалить шаблон | USER, ADMIN |
| GET | `/content` | Игровой контент | USER, ADMIN |
| POST | `/content` | Создать контент | **ADMIN** |
| GET | `/content/types` | Типы контента | USER, ADMIN |
| POST | `/content/types` | Создать тип | **ADMIN** |
| DELETE | `/content/types/{id}` | Удалить тип | **ADMIN** |
| POST | `/content/{id}/pin` | Закрепить контент | USER, ADMIN |
| DELETE | `/content/{id}/pin` | Открепить контент | USER, ADMIN |
| GET | `/users/me` | Профиль пользователя | USER, ADMIN |
| PUT | `/users/me` | Обновить профиль | USER, ADMIN |

Полная документация: [Swagger UI](http://localhost:8080/swagger-ui.html)

---

## Структура документации

Вся документация находится в папке [`docs/`](docs/):

| Раздел | Содержание |
|--------|-----------|
| [`00-project-charter/`](docs/00-project-charter/) | Паспорт проекта, IDEF0, BUC, SWOT, ROI |
| [`01-requirements/`](docs/01-requirements/) | Use Case, Domain Model, трассировка требований |
| [`02-architecture/`](docs/02-architecture/) | PCMEF, ADR, интерфейсы, arc42 |
| [`03-database/`](docs/03-database/) | ER-диаграмма, DDL, ORM-маппинг |
| [`04-detailed-design/`](docs/04-detailed-design/) | Sequence-диаграммы, спецификация методов |
| [`05-implementation/`](docs/05-implementation/) | Реализация слоёв PCMEF |
| [`06-testing/`](docs/06-testing/) | Тест-классы, JaCoCo, результаты (90 тестов) |
| [`07-refactoring/`](docs/07-refactoring/) | Запахи кода, Data Mapper, Identity Map |
| [`08-ui/`](docs/08-ui/) | Скриншоты мобильного интерфейса |
| [`09-api/`](docs/09-api/) | OpenAPI, Swagger, примеры запросов |
| [`10-deployment/`](docs/10-deployment/) | Docker Compose, Dockerfile, инструкция запуска |
| [`11-user-guide/`](docs/11-user-guide/) | Руководство пользователя и администратора |
| [`12-final-report/`](docs/12-final-report/) | Пояснительная записка, ТЗ |

---

## Архитектура (PCMEF)

Система построена на архитектурном паттерне PCMEF (Presentation – Control – Mediator – Entity – Foundation).

### Серверная часть

| Слой | Пакет | Ответственность |
|------|-------|----------------|
| Presentation | `dto/` | DTO для API-границы |
| Control | `control/` | REST-контроллеры |
| Mediator | `mediator/` | Сервисы бизнес-логики |
| Entity | `entity/` | JPA-сущности |
| Foundation | `foundation/repository/` | Spring Data JPA репозитории |

### Мобильный клиент

| Слой | Пакет | Ответственность |
|------|-------|----------------|
| Presentation | `presentation/` | Jetpack Compose экраны |
| Control | `state_management/viewModel/` | ViewModel, управление состоянием |
| Mediator | `state_management/repository/` | Репозитории (Room + Retrofit) |
| Entity | `state_management/entity/` | Доменные модели |
| Foundation | `local_cache/` + `api_client/` | Room DAO, Retrofit API-клиент |

Подробнее: [PCMEF-диаграмма](docs/02-architecture/pcmef-diagram.md) · [ADR-001](docs/02-architecture/adr/adr-001.md)

---

## Тестирование

| Метрика | Значение |
|---------|---------|
| Всего тестов | 90 |
| Упавших тестов | 0 |
| Покрытие инструкций (JaCoCo) | 72% |
| Покрытие методов | 80% |
| Покрытие `mediator/` | 92% |
| Покрытие `control/` | 88% |

Подробнее: [06-testing/](docs/06-testing/)

---

## Статистика разработки

| Метрика | Значение |
|---------|---------|
| Всего коммитов | 64 |
| Период разработки | 01.02.2026 – 29.05.2026 |
| Средняя частота | ~9 коммитов/неделю |
| Покрытие тестами (JaCoCo) | 72% |
| Всего тестов | 90 (0 упавших) |

---

## Авторы

**Черников Дмитрий Дмитриевич** — разработчик, документация  
Группа [ПИЖ-б-о-23-2] · Email: [dima.chernikov.053@mail.ru] · GitHub: [dimain1]

---

## Лицензия

MIT License — проект распространяется под лицензией MIT. Подробности в файле [LICENSE](LICENSE).

---

## Полезные ссылки

- [Репозиторий проекта](https://github.com/[username]/ldoe-helper)
- [Документация](docs/)
- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [Пояснительная записка](docs/12-final-report/)
