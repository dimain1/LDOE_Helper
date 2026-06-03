# ORM-маппинг

## Описание

Документ описывает соответствие JPA-сущностей таблицам PostgreSQL, используемые аннотации и ключевые решения по маппингу. Схема управляется Hibernate с настройкой `spring.jpa.hibernate.ddl-auto=update`.

---

## Сущности и таблицы

| JPA-сущность | Таблица | Пакет |
|-------------|---------|-------|
| `UserRole` | `user_role` | `entity` |
| `User` | `users` | `entity` |
| `Settings` | `settings` | `entity` |
| `RefreshToken` | `refresh_tokens` | `entity` |
| `ContentType` | `content_type` | `entity` |
| `GameContent` | `game_content` | `entity` |
| `UserPinnedContent` | `user_pinned_content` | `entity` |
| `EventTemplate` | `event_template` | `entity` |
| `Event` | `events` | `entity` |

---

## Ключевые решения маппинга

### Стратегия генерации ID

Все сущности используют `GenerationType.IDENTITY` — PostgreSQL `BIGSERIAL`.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

---

### JSONB-колонки

Две сущности используют PostgreSQL JSONB через `@JdbcTypeCode(SqlTypes.JSON)`:

**`Settings.preferences`** — произвольные настройки пользователя:
```java
@Column(columnDefinition = "jsonb")
@JdbcTypeCode(SqlTypes.JSON)
private Map<String, Object> preferences = new HashMap<>();
```

**`GameContent.attributes`** — произвольные атрибуты игрового объекта:
```java
@Column(name = "attributes", columnDefinition = "jsonb")
@JdbcTypeCode(SqlTypes.JSON)
private Map<String, Object> attributes = new HashMap<>();
```

Преимущество: добавление нового типа контента (новые атрибуты) не требует DDL-миграции.

---

### Связи

#### `User → UserRole` (N:1)
```java
@ManyToOne
@JoinColumn(name = "role_id", nullable = false)
private UserRole role;
```

#### `Settings → User` (1:1)
```java
@OneToOne
@JoinColumn(name = "user_id", unique = true)
private User user;
```

#### `RefreshToken → User` (1:1, CASCADE DELETE)
```java
@OneToOne
@OnDelete(action = OnDeleteAction.CASCADE)
@JoinColumn(name = "user_id")
private User user;
```

#### `Event → User` (N:1)
```java
@ManyToOne
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

#### `Event → EventTemplate` (N:1, SET NULL при удалении)
```java
@ManyToOne
@OnDelete(action = OnDeleteAction.SET_NULL)
@JoinColumn(name = "template_id")
private EventTemplate template;    // nullable
```

#### `EventTemplate → User` (N:1, SET NULL при удалении)
```java
@ManyToOne
@JoinColumn(name = "creator_id")
private User creator;
```

#### `GameContent ↔ ContentType` (M:N через `type_to_content`)
```java
@ManyToMany
@JoinTable(
    name = "type_to_content",
    joinColumns        = @JoinColumn(name = "content_id"),
    inverseJoinColumns = @JoinColumn(name = "type_id")
)
private Set<ContentType> types = new HashSet<>();
```

#### `UserPinnedContent` (составной PK: user_id + content_id)
```java
@Entity
@Table(name = "user_pinned_content")
@IdClass(UserPinnedContent.PK.class)
public class UserPinnedContent {
    @Id @ManyToOne @JoinColumn(name = "user_id")   private User user;
    @Id @ManyToOne @JoinColumn(name = "content_id") private GameContent content;
}
```

---

## Dialect и дополнительные настройки

```properties
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql                      = true
spring.jpa.properties.hibernate.format_sql = true
spring.jpa.hibernate.ddl-auto            = update
```

---

## Бизнес-логика при регистрации

Назначение роли первому пользователю реализовано в `AuthService.register()`, а не на уровне БД — это позволяет логике корректно работать при любом порядке старта сервисов (Hibernate создаёт таблицы позже, чем инициализируется PostgreSQL).

```java
// AuthService.java
String roleName = userRepository.count() == 0 ? "ADMIN" : "USER";

UserRole role = userRoleRepository.findByName(roleName)
    .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Роль '" + roleName + "' не найдена"));
```

| Условие | Назначаемая роль |
|---------|----------------|
| `users` пуста (первая регистрация) | `ADMIN` |
| В `users` уже есть записи | `USER` |

---

## Каскадные операции

| Событие | Сущность | Поведение |
|---------|---------|-----------|
| Удаление `User` | `refresh_tokens` | CASCADE — запись токена удаляется |
| Удаление `User` | `events` | CASCADE — события пользователя удаляются |
| Удаление `User` | `user_pinned_content` | CASCADE — закреплённый контент удаляется |
| Удаление `EventTemplate` | `events.template_id` | SET NULL — событие сохраняется без шаблона |
| Удаление `User` (creator) | `event_template.creator_id` | SET NULL — шаблон сохраняется без автора |
| Удаление `GameContent` | `type_to_content` | CASCADE (через DDL) |
| Удаление `GameContent` | `user_pinned_content` | CASCADE (через DDL) |
