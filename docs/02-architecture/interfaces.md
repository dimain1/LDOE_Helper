# Спецификация интерфейсов

## Описание

Документ описывает контракты между слоями архитектуры PCMEF. На серверной стороне слои взаимодействуют через конструкторное внедрение зависимостей (Spring DI). На клиентской стороне — через интерфейсы репозиториев и Retrofit-контракты.

---

## Серверная сторона

### Control → Mediator

Контроллеры получают сервисы через конструктор. Контракт — публичный метод сервиса.

```java
// Пример: EventController зависит от EventService
@RestController
public class EventController {
    private final EventService eventService;          // инжектируется Spring
    // ...
}
```

Методы сервисов принимают и возвращают **DTO**, не JPA-сущности — это граница слоя.

---

### Mediator → Foundation

Сервисы зависят от Spring Data JPA-репозиториев. Контракт — интерфейс `JpaRepository<Entity, ID>`.

```java
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserUsername(String username);
}

// В EventService:
private final EventRepository eventRepository;       // инжектируется Spring
```

---

### Mediator → Entity

Сервисы читают и изменяют JPA-сущности напрямую, используя сеттеры. Сущности не содержат бизнес-логики.

---

### Security → Control (сквозной)

`JwtFilter` выполняется **до** попадания запроса в контроллер. Извлекает токен, валидирует через `JwtService`, помещает `Authentication` в `SecurityContext`.

```java
public class JwtFilter extends OncePerRequestFilter {
    // извлекает Bearer-токен, вызывает JwtService.validateToken()
    // устанавливает UsernamePasswordAuthenticationToken в SecurityContextHolder
}
```

---

## Клиентская сторона (Android)

### Presentation → ViewModel

Compose-экраны подписываются на `StateFlow` / `SharedFlow` ViewModel через `collectAsState()`.

```kotlin
class EventViewModel(private val repo: EventRepository) : ViewModel() {
    val events: StateFlow<List<EventUi>> = repo.getEvents().stateIn(...)
    fun createEvent(request: EventCreateRequest) { /* ... */ }
}
```

---

### ViewModel → Repository

ViewModel вызывает методы репозитория, скрывающего источник данных.

```kotlin
interface EventRepository {
    suspend fun getEvents(): Flow<List<EventUi>>
    suspend fun create(request: EventCreateRequest): Result<EventUi>
    suspend fun delete(id: Long): Result<Unit>
}
```

---

### Repository → Room (Local Cache)

```kotlin
@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE user_id = :userId")
    fun getAll(userId: Long): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: EventEntity)
}
```

---

### Repository → Retrofit (API Client)

```kotlin
interface EventApiService {
    @GET("events")
    suspend fun getMyEvents(@Header("Authorization") token: String): List<EventDto>

    @POST("events")
    suspend fun create(@Body request: EventCreateRequest): EventDto

    @DELETE("events/{id}")
    suspend fun delete(@Path("id") id: Long)
}
```

JWT-токен добавляется автоматически через OkHttp-интерсептор, а не вручную в каждом методе.

---

## Диаграмма зависимостей

![Диаграмма зависимостей](./images/dependency%20diagram.png)
