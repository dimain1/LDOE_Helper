# Спецификация интерфейсов

Взаимодействие между слоями с архитектуре PCMEF происходит через следующие интерфейсы:

- ViewModel(Presentation - State management)

Слой Presentation взаимодействует со слоем State Management через классы наследники класса ViewModel
```java
public class UserViewModel : ViewModel{
    fun fun1();
    fun fun2();
}
```

- IRepository(State management - Local cache)

```java
public interface ILocalCacheRepository {
    void saveToCache(Entity user);
    Entity getFromCache(long id);
}
```

- INetworkClient(State management - API client)

```java
public interface INetworkClient {
    fun doRequest(request: Any): BaseResponse
}
```

- IController(API client - Control)

```java
public interface IController{
    void handleRequest(RequestData data);
}
```

- IService(Control - Mediator)

```java
public interface IService{
    fun exutableUseCase(string... args);
}
```

- IRepository(Mediator - Foundation)

```java
public interface IRepository {
    UserEntity findById(long id);
    void update(UserEntity entity);
}
```

- IEntity(Mediator - Entity)

```java
public interface IUser extends IEntity {
    void changePassword(String newHash);
    boolean canPlaceOrder();
    String getRole();
}

public interface IEntity {
    long getId();
}
```