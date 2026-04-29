# Explore With Me

Платформа для поиска и организации событий. Этап 2 — микросервисная архитектура.

## Технологический стек

- **Java 21** — язык программирования
- **Spring Boot 3.3** — фреймворк приложения
- **Spring Cloud** — инфраструктура микросервисов:
   - **Eureka** — service discovery
   - **Config Server** — централизованная конфигурация
   - **Spring Cloud Gateway** — API-шлюз
   - **OpenFeign** — декларативные HTTP-клиенты между сервисами
   - **Resilience4j** — circuit breaker + retry
- **Spring Data JPA + QueryDSL** — работа с базами данных
- **PostgreSQL** — СУБД (отдельный инстанс на каждый сервис)
- **MapStruct** — маппинг DTO ↔ Entity
- **Lombok** — устранение boilerplate
- **Maven** — многомодульная сборка

---

## Архитектура

```
java-plus-graduation/
├── core/
│   ├── api-dto           # Общие внутренние DTO и PageableFactory (shared lib)
│   ├── main-service      # Управление мероприятиями и категориями
│   ├── user-service      # Администрирование пользователей
│   ├── request-service   # Управление заявками на участие
│   └── extra-service     # Подборки (compilations) и комментарии
├── stats/
│   ├── stats-dto         # DTO сервиса статистики
│   ├── stats-client      # HTTP-клиент для записи и чтения статистики
│   └── stats-server      # Сервис сбора и хранения статистики
└── infra/
    ├── config-server     # Централизованное хранение конфигураций
    ├── discovery-server  # Eureka — регистрация и обнаружение сервисов
    └── gateway-server    # API-шлюз на порту 8080
```

### Взаимодействие сервисов

```
Клиент
  │
  ▼
Gateway (8080)
  ├──► main-service      ──Feign──► user-service (internal)
  │                      ──Feign──► request-service (internal)
  │                      ──HTTP───► stats-server (via DiscoveryClient)
  │
  ├──► user-service
  │
  ├──► request-service   ──Feign──► user-service (internal)
  │                      ──Feign──► main-service (internal)
  │
  ├──► extra-service     ──Feign──► main-service (internal)
  │                      ──Feign──► user-service (internal)
  │                      ──Feign──► request-service (internal)
  │
  └──► stats-server
```

### Маршруты Gateway

| Маршрут | Целевой сервис |
|---------|---------------|
| `GET/POST/DELETE /admin/users/**` | `user-service` |
| `GET/POST/PATCH /users/*/requests/**` | `request-service` |
| `GET/POST/PATCH/DELETE /compilations/**`, `/admin/compilations/**` | `extra-service` |
| `GET/POST/PATCH/DELETE /events/*/comments/**`, `/admin/comments/**` | `extra-service` |
| `GET/POST/PATCH/DELETE /users/*/comments/**` | `extra-service` |
| `GET/POST/PATCH /categories/**`, `/admin/categories/**` | `main-service` |
| `GET/POST/PATCH /admin/events/**`, `/users/**`, `/events/**` | `main-service` |
| `POST /hit/**`, `GET /stats/**` | `stats-server` |

### Порядок запуска

| # | Сервис | Порт |
|---|--------|------|
| 1 | `discovery-server` | `8761` (фиксированный) |
| 2 | `config-server` | `8888` (фиксированный в Docker) |
| 3 | `gateway-server` | `8080` (фиксированный) |
| 4 | `stats-server`, `main-service`, `user-service`, `request-service`, `extra-service` | случайный (Eureka) |

> ⚠️ Нарушение порядка 1→2→3 приведёт к падению при старте (`fail-fast: true`).  
> Сервисы 4-й группы запускаются параллельно в любом порядке.

---

## Базы данных

Каждый сервис имеет **собственную** базу данных — данные не разделяются.  
Кросс-сервисные ссылки хранятся как `Long id` (без `@ManyToOne` и FK).

| Сервис | БД | Порт (host) | Пользователь |
|--------|----|-------------|--------------|
| `main-service` | `ewm-main` | `ewm-db:5432` / `localhost:6543` | `main` |
| `user-service` | `ewm-users` | `user-db:5432` / `localhost:6544` | `users` |
| `request-service` | `ewm-requests` | `request-db:5432` / `localhost:6545` | `requests` |
| `extra-service` | `ewm-extra` | `extra-db:5432` / `localhost:6546` | `extra` |
| `stats-server` | `stats` | `stats-db:5432` / `localhost:6542` | `stats` |

### Схема main-service (`ewm-main`)

| Таблица | Описание |
|---------|---------|
| `categories` | Категории событий |
| `events` | События (initiator_id — Long, без FK на users) |

### Схема user-service (`ewm-users`)

| Таблица | Описание |
|---------|---------|
| `users` | Пользователи (id, name, email) |

### Схема request-service (`ewm-requests`)

| Таблица | Описание |
|---------|---------|
| `participation_requests` | Заявки (event_id, requester_id — Long, без FK) |

### Схема extra-service (`ewm-extra`)

| Таблица | Описание |
|---------|---------|
| `compilations` | Подборки событий |
| `compilation_event` | Связь подборок и event_id (Long) |
| `comments` | Комментарии (author_id, event_id — Long, без FK) |

---

## Конфигурация

Конфигурации хранятся централизованно в `config-server` и раздаются сервисам при старте.

```
infra/config-server/src/main/resources/config/
├── core/
│   ├── main-service/application.yaml
│   ├── user-service/application.yaml
│   ├── request-service/application.yaml
│   └── extra-service/application.yaml
├── stats/
│   └── stats-server/application.yaml
└── infra/
    └── gateway-server/application.yaml
```

Каждый сервис подключается к config-server через bootstrap-конфиг:
```
core/<service-name>/src/main/resources/application.yaml
```

---

## Внутренний API (inter-service)

Внутренние эндпоинты не проксируются через Gateway и используются только между сервисами через OpenFeign.

### user-service → `/internal/users`

| Метод | Путь | Описание | Используется в |
|-------|------|----------|---------------|
| `GET` | `/internal/users/{userId}` | Получить пользователя по ID | `main-service`, `extra-service` |
| `POST` | `/internal/users/batch` | Batch-получение пользователей `List<Long>` → `Map<Long, UserInternalDto>` | `main-service`, `extra-service` |
| `GET` | `/internal/users/exists/{userId}` | Проверить существование пользователя | `request-service` |

### main-service → `/internal/events`

| Метод | Путь | Описание | Используется в |
|-------|------|----------|---------------|
| `GET` | `/internal/events/{eventId}` | Получить событие по ID | `request-service`, `extra-service` |
| `POST` | `/internal/events/batch` | Batch-получение событий `List<Long>` → `Map<Long, EventInternalDto>` | `extra-service` |
| `GET` | `/internal/events/exists/{eventId}` | Проверить существование события | `request-service` |

### request-service → `/internal/requests`

| Метод | Путь | Описание | Используется в |
|-------|------|----------|---------------|
| `GET` | `/internal/requests/events/{eventId}` | Все заявки на событие | `main-service` |
| `POST` | `/internal/requests/events/{eventId}/update-statuses` | Обновить статусы заявок | `main-service` |
| `POST` | `/internal/requests/confirmed-counts` | Количество подтверждённых заявок batch `List<Long>` → `Map<Long, Long>` | `main-service`, `extra-service` |

### Устойчивость к сбоям

Все Feign-клиенты имеют fallback-реализации:

| Клиент | Fallback-поведение |
|--------|--------------------|
| `RequestFeignClient` | `confirmedCounts` → пустой `Map` (0 заявок), `getByEvent` → пустой список |
| `UserFeignClient` | `exists` → `true` (не блокировать), `getUser` → `null` |
| `EventFeignClient` | `exists` → `false`, `getEvent` → `ServiceUnavailableException` |

Circuit Breaker + Retry настроены через Resilience4j в конфиге каждого сервиса (sliding window 10, failure rate 50%, retry 3 попытки).

---

## Внешний API

Полные спецификации в формате OpenAPI 3.0 находятся в корне проекта:

- **Основной сервис**: [`ewm-main-service-spec.json`](./ewm-main-service-spec.json)
- **Сервис статистики**: [`ewm-stats-service-spec.json`](./ewm-stats-service-spec.json)

Все запросы направляются через Gateway: `http://localhost:8080`

| Группа | Префикс |
|--------|---------|
| Public | `/categories/**`, `/events/**`, `/compilations/**` |
| Private | `/users/**` |
| Admin | `/admin/**` |
| Комментарии (public) | `/events/{id}/comments/**` |
| Комментарии (private) | `/users/{id}/comments/**` |
| Комментарии (admin) | `/admin/comments/**` |
| Статистика | `/hit/**`, `/stats/**` |

---

## Установка и запуск

### Docker Compose (рекомендуется)

```bash
# Сборка всех модулей
mvn package -DskipTests

# Запуск всей инфраструктуры
docker-compose up --build
```

После старта все сервисы регистрируются в Eureka: `http://localhost:8761`  
API доступен через Gateway: `http://localhost:8080`

### Локально (без Docker)

Запускать строго в порядке:

1. Поднять PostgreSQL (5 инстансов на портах 6542–6546)
2. Запустить `discovery-server`
3. Запустить `config-server`
4. Запустить `gateway-server`
5. Запустить остальные сервисы в любом порядке

```bash
# Пример запуска одного сервиса локально
cd core/main-service
java -jar target/*.jar \
  --EUREKA_HOST=localhost \
  --POSTGRES_HOST=localhost \
  --POSTGRES_PORT=6543
```