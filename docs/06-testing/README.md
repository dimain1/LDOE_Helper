# Этап 6 — Тестирование

## Описание

Документ описывает стратегию тестирования серверной части системы LDOE-Helper, покрытие тестами, используемые инструменты и результаты запуска. Тестируются сервисный слой (Mediator) и слой контроллеров (Control).

---

## Стратегия тестирования

Применяются **модульные (unit) тесты** с полной изоляцией тестируемого класса.

| Слой | Подход | Инструмент изоляции |
|------|--------|---------------------|
| **Mediator** (сервисы) | `@ExtendWith(MockitoExtension.class)` — Spring-контекст не поднимается | `@Mock` — Mockito-моки зависимостей |
| **Control** (контроллеры) | `MockMvcBuilders.standaloneSetup()` — только MVC-инфраструктура | `@Mock` — Mockito-моки сервисов |

**Особенности Control-тестов:** Spring Boot 4.0 удалил `@WebMvcTest`, поэтому контроллеры тестируются через `standaloneSetup`. `@AuthenticationPrincipal` разрешается добавлением `AuthenticationPrincipalArgumentResolver` и установкой `SecurityContextHolder` в `@BeforeEach`. Метод-уровневая безопасность (`@PreAuthorize`) в standalone-режиме не применяется.

| Инструмент | Версия | Назначение |
|-----------|--------|-----------|
| JUnit 5 (JUnit Jupiter) | 6.0.3 | Фреймворк тестирования |
| Mockito | 5.x | Создание тест-дублёров |
| AssertJ | 3.x | Читаемые assertion-цепочки |
| Spring MockMvc | 7.x | HTTP-тестирование контроллеров |
| JaCoCo | 0.8.x | Измерение покрытия кода |

Интеграционные и end-to-end тесты в рамках курсовой работы не реализованы.

---

## Тест-классы — Mediator (сервисный слой)

### `AuthServiceTest` — 10 тестов

Тестирует `AuthService`: аутентификацию, регистрацию, обновление токенов и выход.

| Тест | Проверяемое поведение |
|------|-----------------------|
| `login_validCredentials_returnsAccessAndRefreshTokens` | Корректные логин и пароль → возвращается пара токенов, refresh сохраняется в БД |
| `login_wrongPassword_throwsUnauthorized` | Неверный пароль → `401 UNAUTHORIZED` |
| `login_unknownUser_throwsUnauthorized` | Несуществующий логин → `401 UNAUTHORIZED` |
| `register_newLogin_savesUserAndReturnsIt` | Новый логин → пользователь сохранён и возвращён |
| `register_duplicateLogin_throwsConflict` | Занятый логин → `409 CONFLICT` |
| `register_defaultRoleNotFound_throwsInternalServerError` | Роль отсутствует в БД → `500 INTERNAL_SERVER_ERROR` |
| `logout_callsDeleteByToken` | Выход → refresh-токен удалён из БД |
| `refresh_validToken_returnsNewTokenPair` | Действующий refresh → выдана новая пара токенов |
| `refresh_tokenNotFound_throwsUnauthorized` | Несуществующий токен → `401 UNAUTHORIZED` |
| `refresh_expiredToken_throwsUnauthorizedAndDeletesToken` | Истёкший токен → `401`, токен удалён из БД |

### `EventServiceTest` — 13 тестов

Тестирует `EventService`: чтение, создание, обновление и удаление событий.

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getMyEvents_returnsUserEvents` | Возвращает список событий текущего пользователя |
| `getMyEvents_noEvents_returnsEmptyList` | У пользователя нет событий → пустой список |
| `getMyEvents_unknownLogin_throws` | Неизвестный логин → `RuntimeException("User not found")` |
| `create_withoutTemplate_savesEventAndReturnsDto` | Создание без шаблона → событие сохранено, `templateRepository` не вызывался |
| `create_withTemplate_loadsAndSetsTemplate` | Передан `templateId` → шаблон загружен и привязан |
| `create_templateNotFound_throws` | Несуществующий `templateId` → `RuntimeException("Template not found")` |
| `update_ownedEvent_updatesNameAndDescription` | Владелец обновляет поля → изменения сохранены |
| `update_nullFields_doesNotOverwriteExistingValues` | Null-поля в запросе → существующие значения не затронуты |
| `update_eventNotFound_throws` | Несуществующий `eventId` → `RuntimeException("Event not found")` |
| `update_callerIsNotOwner_throwsAccessDenied` | Чужое событие → `RuntimeException("Access denied")` |
| `delete_ownedEvent_callsRepositoryDelete` | Владелец удаляет → `eventRepository.delete()` вызван |
| `delete_callerIsNotOwner_throwsAndDoesNotDelete` | Чужое событие → исключение, `delete()` не вызван |
| `delete_eventNotFound_throws` | Несуществующий `eventId` → `RuntimeException("Event not found")` |

### `EventTemplateServiceTest` — 13 тестов

Тестирует `EventTemplateService`: CRUD-операции над шаблонами событий, загрузку изображений.

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getMyTemplates_returnsCreatorTemplates` | Возвращает шаблоны, созданные пользователем |
| `getMyTemplates_unknownLogin_throws` | Неизвестный логин → исключение |
| `create_withoutImage_savesTemplateAndReturnsDto` | Создание без файла → `imageStorageService` не вызывался |
| `create_withImage_callsImageStorage` | Передан файл → `imageStorageService.saveImage()` вызван |
| `create_withEmptyMultipartFile_doesNotCallImageStorage` | Пустой файл (0 байт) → `imageStorageService` не вызывался |
| `update_ownedTemplate_updatesName` | Обновление имени владельцем → изменение применено |
| `update_ownedTemplate_updatesDuration` | Обновление длительности → изменение применено |
| `update_nullFields_doesNotOverwriteExistingValues` | Null-поля → существующие значения сохранены |
| `update_templateNotFound_throws` | Несуществующий ID → исключение |
| `update_callerIsNotCreator_throwsAccessDenied` | Чужой шаблон → `RuntimeException("Access denied")` |
| `update_withImage_callsImageStorage` | Передан файл при обновлении → `saveImage()` вызван |
| `delete_ownedTemplate_deletesFromRepository` | Владелец удаляет → `templateRepository.delete()` вызван |
| `delete_callerIsNotCreator_throwsAndDoesNotDelete` | Чужой шаблон → исключение, `delete()` не вызван |

### `GameContentServiceTest` — 16 тестов

Тестирует `GameContentService`: управление игровым контентом, типами, закреплением.

| Тест | Проверяемое поведение |
|------|-----------------------|
| `ensureAllTypeExists_typeAlreadyPresent_doesNotCreateNew` | Тип `"All"` существует → `save()` не вызывался |
| `ensureAllTypeExists_typeMissing_createsAndSaves` | Тип `"All"` отсутствует → создан и сохранён |
| `getAllTypes_returnsDtoForEachType` | Возвращает DTO для каждого типа контента |
| `createType_newName_savesAndReturnsDto` | Новое имя типа → тип создан и возвращён |
| `createType_duplicateName_throwsRuntimeException` | Дублирующееся имя → `RuntimeException("уже существует")` |
| `deleteType_typeNotFound_throws` | Несуществующий ID типа → исключение |
| `deleteType_allType_throwsAndDoesNotDelete` | Попытка удалить тип `"All"` → исключение, `deleteById()` не вызван |
| `deleteType_regularType_callsDeleteById` | Обычный тип → `contentTypeRepository.deleteById()` вызван |
| `getAll_returnsDtosWithCorrectPinnedFlag` | Возвращает контент с корректным флагом `pinned` |
| `getAll_unknownLogin_throws` | Неизвестный логин → исключение |
| `create_withoutFile_savesContentWithAllType` | Создание без файла → тип `"All"` добавлен автоматически |
| `pin_contentNotYetPinned_savesNewPin` | Ещё не закреплён → `pinnedRepository.save()` вызван |
| `pin_alreadyPinned_doesNotSaveAgain` | Уже закреплён → повторный `save()` не вызывается |
| `pin_contentNotFound_throws` | Несуществующий контент → исключение |
| `unpin_callsDeleteByUserAndContent` | Открепить → `deleteByUserAndContent()` вызван |
| `unpin_contentNotFound_throws` | Несуществующий контент → исключение |

### `JwtServiceTest` — 6 тестов

Тестирует `JwtService` без Spring-контекста: генерацию и разбор JWT-токенов.

| Тест | Проверяемое поведение |
|------|-----------------------|
| `generateAccessToken_returnsNonBlankString` | Access-токен непустой |
| `generateRefreshToken_returnsNonBlankString` | Refresh-токен непустой |
| `extractLogin_fromAccessToken_returnsOriginalLogin` | Логин, извлечённый из access-токена, совпадает с исходным |
| `extractLogin_fromRefreshToken_returnsOriginalLogin` | Логин, извлечённый из refresh-токена, совпадает с исходным |
| `generateTokens_forSameLogin_accessAndRefreshAreDifferent` | Access и refresh для одного логина различны |
| `generateAccessToken_differentLogins_produceDifferentTokens` | Токены для разных логинов различны |

### `UserServiceTest` — 8 тестов

Тестирует `UserService`: получение профиля, обновление данных, смену пароля.

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getMe_existingUser_returnsDto` | Существующий логин → корректный DTO с полями |
| `getMe_unknownLogin_throwsRuntimeException` | Неизвестный логин → `RuntimeException("User not found")` |
| `updateMe_newLogin_updatesLoginField` | Новый логин в запросе → поле обновлено, `save()` вызван |
| `updateMe_newEmail_updatesEmailField` | Новый email → поле обновлено |
| `updateMe_nullLogin_doesNotChangeLogin` | `null` в поле логина → логин не изменён |
| `updateMe_blankLogin_doesNotChangeLogin` | Пробелы в поле логина → логин не изменён |
| `changePassword_correctOldPassword_encodesNew` | Верный старый пароль → новый пароль сохранён в BCrypt |
| `changePassword_wrongOldPassword_throwsAndDoesNotSave` | Неверный старый пароль → исключение, `save()` не вызван |

### `BackendApplicationTests` — 1 тест

Проверяет, что Spring-контекст поднимается без ошибок (`contextLoads`).

---

## Тест-классы — Control (слой контроллеров)

Тесты используют `MockMvcBuilders.standaloneSetup()` — Spring-контекст и фильтры безопасности не подключаются. Проверяются маршрутизация, десериализация запроса, вызов сервиса с правильными аргументами и сериализация ответа.

### `AuthControllerTest` — 5 тестов

| Тест | Проверяемое поведение |
|------|-----------------------|
| `register_validRequest_returns200WithUserId` | Корректный запрос → `200` с ID созданного пользователя |
| `login_validCredentials_returnsTokenPair` | Верные данные → `200` с парой токенов в теле |
| `login_wrongCredentials_returns401` | Неверный пароль → `401 UNAUTHORIZED` |
| `logout_validToken_returns200AndCallsService` | Запрос на выход → `200`, `authService.logout()` вызван |
| `refresh_validToken_returnsNewTokenPair` | Действующий refresh → `200` с новой парой токенов |

### `EventControllerTest` — 4 теста

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getMyEvents_authenticated_returnsEventList` | `GET /events` → `200` со списком событий |
| `create_validRequest_returnsCreatedDto` | `POST /events` → `200` с DTO созданного события |
| `update_ownedEvent_returnsUpdatedDto` | `PUT /events/{id}` → `200` с обновлённым DTO |
| `delete_ownedEvent_returns200AndCallsService` | `DELETE /events/{id}` → `200`, `delete()` вызван с нужным id |

### `EventTemplateControllerTest` — 4 теста

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getMyTemplates_authenticated_returnsTemplateList` | `GET /templates` → `200` со списком шаблонов |
| `create_withoutImage_returnsCreatedDto` | `POST /templates` (multipart) → `200` с DTO шаблона |
| `update_ownedTemplate_returnsUpdatedDto` | `PUT /templates/{id}` (multipart) → `200` с обновлённым DTO |
| `delete_ownedTemplate_returns200AndCallsService` | `DELETE /templates/{id}` → `200`, `delete()` вызван |

### `GameContentControllerTest` — 7 тестов

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getAll_authenticated_returnsContentList` | `GET /content` → `200` с контентом и флагом `pinned` |
| `create_validRequest_returnsCreatedDto` | `POST /content` (multipart) → `200` с DTO контента |
| `getAllTypes_returnsTypeList` | `GET /content/types` → `200` со списком типов |
| `createType_validName_returnsCreatedType` | `POST /content/types` → `200` с созданным типом |
| `deleteType_validId_returns200AndCallsService` | `DELETE /content/types/{id}` → `200`, `deleteType()` вызван |
| `pin_authenticated_returns200AndCallsService` | `POST /content/{id}/pin` → `200`, `pin()` вызван |
| `unpin_authenticated_returns200AndCallsService` | `DELETE /content/{id}/pin` → `200`, `unpin()` вызван |

### `UserControllerTest` — 3 теста

| Тест | Проверяемое поведение |
|------|-----------------------|
| `getMe_authenticated_returnsUserDto` | `GET /users/me` → `200` с профилем пользователя |
| `updateMe_validRequest_returnsUpdatedDto` | `PUT /users/me` → `200` с обновлёнными данными |
| `changePassword_validRequest_returns200AndCallsService` | `PUT /users/me/password` → `200`, `changePassword()` вызван |

---

## Результаты запуска

```
90 tests completed, 0 failed
BUILD SUCCESSFUL
```

| Статус | Количество |
|--------|-----------|
| ✅ Пройдено | 90 |
| ❌ Упало | 0 |
| Всего | 90 |

---

## Покрытие кода (JaCoCo)

Отчёт сгенерирован командой `./gradlew test`. Конфигурация JaCoCo подключена в `build.gradle`.

### Скриншот HTML-отчёта

![JaCoCo report](./images/jacoco-report.png)

### Покрытие по пакетам

| Пакет | Инструкций покрыто | % |
|-------|--------------------|---|
| `dto/event` | 142 / 142 | **100%** |
| `dto/template` | 93 / 93 | **100%** |
| `mediator` | 1111 / 1213 | **92%** |
| `dto/auth` | 66 / 74 | **89%** |
| `control` | 174 / 198 | **88%** |
| `dto/gameContent` | 98 / 109 | **90%** |
| `dto/user` | 61 / 80 | **76%** |
| `entity` | 282 / 413 | **68%** |
| `security` | 0 / 182 | 0% |
| `config` | 0 / 226 | 0% |

### Итого по проекту

| Метрика | Покрыто | Всего | % |
|---------|---------|-------|---|
| Инструкции | 2027 | 2796 | **72%** |
| Ветви | 54 | 102 | **53%** |
| Строки | 539 | 721 | **75%** |
| Методы | 282 | 354 | **80%** |
| Классы | 35 | 50 | **70%** |

**Пакеты `security` и `config` имеют нулевое покрытие** — они не тестируются намеренно: `JwtFilter` и `SecurityConfig` требуют полного Spring Security контекста, а `DataInitializer`/`WebConfig` — инфраструктурные классы без бизнес-логики.
