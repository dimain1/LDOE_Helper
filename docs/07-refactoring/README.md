# Этап 7 — Рефакторинг

## Описание

Документ описывает выявленные «запахи кода» в серверной части системы LDOE-Helper, паттерны проектирования Data Mapper и Identity Map, а также проведённые рефакторинги.

### Итог по исправлениям

| # | Запах | Статус |
|---|-------|--------|
| 1 | Дублирование `findUser` в 4 сервисах | ⏸ Оставлен — устранение требует нового компонента и выходит за рамки точечного рефакторинга |
| 2 | `BCryptPasswordEncoder` создаётся дважды вне Spring | ✅ Исправлен — вынесен в `@Bean`, инжектируется как `PasswordEncoder` |
| 3 | Magic String `"All"` в трёх местах | ✅ Исправлен — заменён константой `ALL_TYPE_NAME` |
| 4 | `GameContentService` — избыточная ответственность | ⏸ Оставлен — разбивка на два сервиса является архитектурным изменением |
| 5 | Дублирующий `orElseGet` в `create()` | ✅ Исправлен — заменён `orElseThrow`, инициализация делегирована `@PostConstruct` |

Все 67 тестов прошли после рефакторинга. Приложение успешно запускается.

---

## «Запахи кода»

### 1. Дублирование метода `findUser` (Duplicated Code)

**Расположение:** `EventService`, `EventTemplateService`, `GameContentService`, `UserService`

Одна и та же логика поиска пользователя по логину продублирована в четырёх сервисах:

```java
// EventService.java:84
private User findUser(String login) {
    return userRepository.findByLogin(login)
            .orElseThrow(() -> new RuntimeException("User not found"));
}

// EventTemplateService.java — идентично
// GameContentService.java — идентично
// UserService.java — идентично (другое имя метода)
```

**Проблема:** при изменении текста исключения или логики поиска необходимо вносить правки в четыре места.

**Рефакторинг:** вынести метод в вспомогательный класс `UserLookup` или решить через Spring-бин с инжекцией в сервисы.

---

### 2. `BCryptPasswordEncoder` создаётся дважды как поле (не Spring-бин)

**Расположение:** `AuthService` (строка 27), `UserService` (строка 16)

```java
// AuthService.java
private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// UserService.java — идентично
private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
```

**Проблема:** создаются два независимых экземпляра тяжёлого объекта. `BCryptPasswordEncoder` потокобезопасен и предназначен для использования как синглтон.

**Рефакторинг:** объявить как `@Bean` в конфигурационном классе и инжектировать через конструктор в оба сервиса.

```java
// SecurityConfig.java (после рефакторинга)
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

### 3. Magic String `"All"` (Magic Literal)

**Расположение:** `GameContentService` — три вхождения

```java
contentTypeRepository.findByName("All") 
contentTypeRepository.findByName("All")  
if ("All".equals(type.getName()))        
```

**Проблема:** строковая константа разбросана по нескольким местам. Опечатка в любом из них сломает логику защиты типа от удаления.

**Рефакторинг:** вынести в именованную константу класса:

```java
private static final String ALL_TYPE_NAME = "All";
```

---

### 4. Класс с избыточной ответственностью — `GameContentService` (Large Class)

**Расположение:** `GameContentService` (169 строк)

Один сервис выполняет три независимые функции:

| Группа методов | Ответственность |
|---------------|----------------|
| `ensureAllTypeExists`, `getAllTypes`, `createType`, `deleteType` | Управление типами контента |
| `getAll`, `create` | Управление игровым контентом |
| `pin`, `unpin` | Управление закреплённым контентом пользователя |

**Проблема:** нарушение принципа единственной ответственности (SRP). Изменения в логике типов затрагивают класс, отвечающий за контент.

**Рефакторинг:** разбить на `ContentTypeService` и `GameContentService`, оставив в последнем только работу с контентом и закреплением.

---

### 5. Дублирование инициализации типа `"All"` (Duplicated Logic)

**Расположение:** `GameContentService`

Логика «найти тип "All" или создать» реализована дважды в одном классе:

```java
// @PostConstruct ensureAllTypeExists():
contentTypeRepository.findByName("All").orElseGet(() -> {
    ContentType allType = new ContentType();
    allType.setName("All");
    return contentTypeRepository.save(allType);
});

// create():
ContentType allType = contentTypeRepository.findByName("All")
        .orElseGet(() -> {
            ContentType newAll = new ContentType();
            newAll.setName("All");
            return contentTypeRepository.save(newAll);
        });
```

**Проблема:** `@PostConstruct` гарантирует существование типа "All" при старте, поэтому вторая проверка в `create()` избыточна.

**Рефакторинг:** в `create()` заменить `orElseGet(...)` на `orElseThrow(...)` — при корректной инициализации тип всегда присутствует.

---

## Паттерн Data Mapper

### Описание

Data Mapper — паттерн, при котором объект-маппер переводит данные между доменной моделью (Entity) и транспортным представлением (DTO), не смешивая логику преобразования с бизнес-логикой.

### Реализация в проекте

В серверной части маппинг реализован как **статические методы `toDto`** внутри каждого сервиса. Это упрощённая форма паттерна: маппер встроен в Mediator-слой, а не вынесен в отдельный класс.

| Сервис | Метод | Направление |
|--------|-------|------------|
| `EventService` | `static EventDto toDto(Event event)` | `Event` → `EventDto` |
| `EventTemplateService` | `static EventTemplateDto toDto(EventTemplate t)` | `EventTemplate` → `EventTemplateDto` |
| `GameContentService` | `static GameContentDto toDto(GameContent gc, boolean pinned)` | `GameContent` → `GameContentDto` |
| `UserService` | `static UserDto toDto(User user)` | `User` → `UserDto` |

**Пример — `EventService.toDto`:**

```java
public static EventDto toDto(Event event) {
    EventDto dto = new EventDto();
    dto.setId(event.getId());
    dto.setUserId(event.getUser().getId());
    dto.setTemplateId(event.getTemplate() != null ? event.getTemplate().getId() : null);
    dto.setName(event.getName());
    dto.setDescription(event.getDescription());
    dto.setImageUrl(event.getImageUrl());
    dto.setStartTime(event.getStartTime());
    dto.setEndTime(event.getEndTime());
    return dto;
}
```

Метод является чистой функцией: не имеет побочных эффектов, не обращается к репозиториям, легко тестируется.

### Обоснование выбора

Вынесение маппинга в отдельные классы (`EventMapper`, `GameContentMapper` и т.д.) оправдано при двунаправленном маппинге (DTO → Entity) или при использовании фреймворка MapStruct. В данном проекте маппинг однонаправлен (только Entity → DTO), поэтому встроенные статические методы достаточны и не усложняют структуру без необходимости.

---

## Паттерн Identity Map

### Описание

Identity Map — паттерн, гарантирующий, что каждый объект загружается из хранилища только один раз за транзакцию. Повторные обращения по тому же идентификатору возвращают уже загруженный экземпляр из кэша, а не создают новый.

### Реализация в серверной части (Hibernate)

Hibernate реализует Identity Map автоматически через **Persistence Context** (`EntityManager`). В рамках одной транзакции (`@Transactional`) повторный вызов `findById` с тем же `id` не генерирует второй SQL-запрос — возвращается экземпляр из кэша первого уровня.

**Пример — `EventService.update`:**

```java
@Transactional
public EventDto update(String login, Long eventId, EventUpdateRequest request) {
    Event event = findOwnedEvent(login, eventId);   // SELECT * FROM events WHERE id = ?
    // ...
    return toDto(eventRepository.save(event));       // UPDATE events SET ...
    // повторный findById(eventId) внутри той же транзакции не вызовет второй SELECT
}
```

Hibernate гарантирует, что за время транзакции существует не более одного объекта `Event` с данным `id` — изменения, применённые к этому объекту, будут видны во всём контексте без повторного запроса к БД.

### Реализация в мобильной части (Room)

В мобильном клиенте Identity Map реализован через **Room Database** и **DAO-интерфейсы**. Room использует SQLite как единственный источник истины: один и тот же `id` всегда ссылается на одну запись в таблице. Обновление сущности через `eventDao.updateEvent(...)` гарантирует консистентность — все последующие запросы через `Flow` получат актуальное состояние.

Дополнительно, `Flow`-потоки в Room автоматически уведомляют подписчиков об изменениях: ViewModel не хранит устаревшие копии объектов.

```kotlin
// EventRepositoryImpl.kt
override fun getAllEventsWithTemplate(): Flow<List<Event>> =
    session.pipe { id ->
        eventDao.getAllEventsWithTemplate(id)    // Room возвращает Flow — единый источник истины
            .map { list -> list.map { it.toModel() } }
    }
```
