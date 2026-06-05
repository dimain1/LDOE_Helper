# Этап 10 — Развёртывание

## Описание

Документ описывает инфраструктуру развёртывания системы LDOE-Helper: контейнеризацию серверной части и базы данных через Docker Compose, конфигурацию окружения и процедуру запуска.

---

## Архитектура развёртывания

Система разворачивается в двух Docker-контейнерах в рамках одного Compose-проекта (`name: ldoe`):

```
┌─────────────────────────────────────────┐
│           Docker Compose (ldoe)         │
│                                         │
│  ┌──────────────┐   ┌────────────────┐  │
│  │  ldoe-backend│──▶│   ldoe-db      │  │
│  │  Spring Boot │   │ PostgreSQL 15  │  │
│  │  :8080       │   │ :5432          │  │
│  └──────────────┘   └────────────────┘  │
│         │                  │            │
│    volume: uploads    volume: pgdata     │
└─────────────────────────────────────────┘
         ▲
    localhost:8080
    (клиент / Swagger UI)
```

---

## Структура файлов

```
Курсовая работа 3 курс/
├── docker-compose.yml       — описание сервисов и сетей
├── .env                     — переменные окружения
├── docker/
│   └── init.sql             — SQL-триггер для первого пользователя
└── Backend/backend/
    ├── Dockerfile           — образ серверного приложения
    └── build/libs/
        └── *.jar            — собранный JAR (генерируется ./gradlew bootJar)
```

---

## Docker Compose

### `docker-compose.yml`

```yaml
name: ldoe

services:
  db:
    image: postgres:15-alpine
    container_name: ldoe-db
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data   # данные БД сохраняются между перезапусками
    ports:
      - "${DB_PORT:-5432}:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./Backend/backend
      dockerfile: Dockerfile
    container_name: ldoe-backend
    env_file: .env                        # загружает все переменные из .env
    environment:
      DB_HOST: db                         # переопределяет DB_HOST для Docker-сети
    ports:
      - "${SERVER_PORT:-8080}:8080"
    depends_on:
      db:
        condition: service_healthy        # backend стартует только после готовности БД
    volumes:
      - uploads:/app/uploads              # загруженные изображения

volumes:
  pgdata:
  uploads:
```

**Ключевые решения:**

| Решение | Обоснование |
|---------|------------|
| `depends_on: condition: service_healthy` | Исключает ошибку подключения при старте — backend ждёт готовности PostgreSQL |
| `DB_HOST: db` в `environment` | Переопределяет дефолтный `localhost` из `.env` на имя Docker-сервиса |
| `pgdata` volume | Данные БД не теряются при пересборке образа |

---

## Dockerfile

```dockerfile
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17
WORKDIR /app

COPY build/libs/*.jar app.jar
RUN mkdir -p uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Образ строится на Amazon Corretto 17 (OpenJDK). JAR копируется из `build/libs/` — поэтому перед сборкой образа необходимо собрать проект.

---

## Переменные окружения

Все настройки хранятся в файле `.env` в корне проекта. 

| Переменная | Пример | Описание |
|-----------|--------|---------|
| `DB_PORT` | `5432` | Порт PostgreSQL (публичный) |
| `DB_NAME` | `LDOE_helper` | Имя базы данных |
| `DB_USERNAME` | `test_role` | Пользователь PostgreSQL |
| `DB_PASSWORD` | `admin` | Пароль PostgreSQL |
| `SERVER_PORT` | `8080` | Порт Spring Boot (публичный) |
| `JWT_SECRET` | `my_super_secret_key_...` | Секрет для подписи JWT (мин. 32 символа) |
| `JWT_ACCESS_EXPIRATION_MS` | `1200000` | TTL access-токена (20 мин) |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | TTL refresh-токена (7 дней) |

`DB_HOST` в `.env` не задаётся — он переопределяется в `docker-compose.yml` (`db`) для Docker и берёт дефолт `localhost` для локального запуска.

---

## Процедура запуска

### Полный запуск через Docker Compose

```bash
# 1. Собрать JAR (выполняется один раз или после изменений кода)
cd Backend/backend
./gradlew bootJar

# 2. Поднять все сервисы
cd ../..
docker-compose up --build -d

# 3. Проверить статус
docker-compose ps

# 4. Просмотр логов backend
docker logs ldoe-backend -f
```

После успешного старта:
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`



---

## Требования к окружению

| Компонент | Версия | Назначение |
|-----------|--------|-----------|
| Docker Desktop | 20+ | Контейнеризация |
| Docker Compose | 2.x | Оркестрация сервисов |
| JDK | 17 | Сборка JAR (`./gradlew bootJar`) |
| Gradle | 9.x | Система сборки (wrapper включён) |

---

## Управление схемой БД

Hibernate управляет схемой автоматически через настройку:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Режим `update` — при каждом старте Hibernate добавляет недостающие таблицы и колонки, не удаляя существующие данные. Это позволяет безопасно обновлять схему при деплое новой версии.

Файл `docker/init.sql` содержит PostgreSQL-триггер, который назначает первому зарегистрированному пользователю роль `ADMIN`. Триггер применяется автоматически при первом старте контейнера, когда том `pgdata` пустой. Дублирует соответствующую логику в `AuthService.register()`.
