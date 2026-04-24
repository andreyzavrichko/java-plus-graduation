# Explore With Me - Main Service

Основной сервис платформы для поиска и организации событий.

## Технологический стек

- **Java 21** — язык программирования
- **Spring Boot 3.3** — фреймворк для разработки приложения
- **Spring Cloud** — инфраструктура микросервисов (Eureka, Config, Gateway)
- **Spring Data JPA** — работа с БД
- **PostgreSQL** — система управления БД
- **QueryDSL** — построение динамических запросов
- **MapStruct** — маппинг DTO ↔ Entity
- **Lombok** — уменьшение boilerplate кода
- **Maven** — система управления зависимостями

## Архитектура

Многомодульный проект `explore-with-me`:

```
java-plus-graduation/
├── core/
│   └── main-service        # основной сервис (текущий модуль)
├── stats/
│   ├── stats-client        # клиент сервиса статистики
│   ├── stats-dto           # DTO для обмена данными со статистикой
│   └── stats-server        # сервис статистики
└── infra/
    ├── config-server       # централизованное управление конфигурацией
    ├── discovery-server    # сервис регистрации и обнаружения (Eureka)
    └── gateway-server      # API-шлюз (порт 8080)
```

### Порядок запуска сервисов

| Порядок | Сервис | Порт |
|---------|--------|------|
| 1 | `discovery-server` | `8761` (фиксированный) |
| 2 | `config-server` | случайный (регистрируется в Eureka) |
| 3 | `gateway-server` | `8080` (фиксированный) |
| 4 | `stats-server` | случайный (регистрируется в Eureka) |
| 5 | `main-service` | случайный (регистрируется в Eureka) |

> ⚠️ Нарушение порядка приведёт к падению сервиса при старте (`fail-fast: true`)

## Схема базы данных

### Основные таблицы

| Таблица | Описание |
|---------|---------|
| `users` | Пользователи системы (id, name, email) |
| `categories` | Категории событий (id, name) |
| `events` | События с полной информацией (id, title, description, state, initiator_id, category_id и т.д.) |
| `participation_requests` | Запросы на участие в событиях (id, event_id, requester, status) |
| `compilations` | Подборки событий (id, title, pinned) |
| `compilation_event` | Связь событий и подборок (many-to-many) |
| `comments` | Комментарии к событиям (id, text, author_id, event_id) |

### Ограничения и правила

- События не могут быть в прошлом (`CHECK event_date > CURRENT_DATE`)
- Статусы событий: `PUBLISHED`, `PENDING`, `CANCELED`
- Событие опубликовано только если `state = 'PUBLISHED' AND published_on IS NOT NULL`
- Удаление категории и инициатора запрещено, если на них ссылаются события
- При удалении события каскадно удаляются запросы и комментарии

## Конфигурация

Конфигурация управляется централизованно через `config-server`.  
Файл: `infra/config-server/src/main/resources/config/core/main-service/application.yaml`

**База данных:**
- Host: `ewm-db` (Docker) / `localhost:6543` (dev-профиль)
- DB: `ewm-main`
- Пользователь: `main` / Пароль: `main`

**Приложение:**
- Порт: случайный (`server.port: 0`) — адрес определяется через Eureka
- Сервис статистики: обнаруживается по имени `stats-server` через Eureka
- DDL-auto: `none` (схема управляется через `schema.sql`)

**Профили:**
- `default` — продакшн-конфиг (подключение к `ewm-db` в Docker)
- `dev` — локальная разработка (подключение к `localhost:6543`)

## Установка и запуск

### Локально (без Docker)

1. Запустить PostgreSQL на `localhost:6543` с БД `ewm-main`
2. Запустить `discovery-server`, затем `config-server`
3. Собрать проект:
   ```bash
   mvn package -DskipTests
   ```
4. Запустить с dev-профилем:
   ```bash
   java -jar target/*.jar --spring.profiles.active=dev
   ```

### Через Docker Compose (все сервисы)

```bash
# Сборка JAR
mvn package -DskipTests

# Запуск всей инфраструктуры
docker-compose up --build
```

Все запросы к API направляются через gateway: `http://localhost:8080`

## Основные компоненты

- **Entities** — JPA сущности (User, Event, Category, ParticipationRequest и т.д.)
- **Repositories** — Spring Data JPA репозитории с QueryDSL поддержкой
- **Services** — бизнес-логика
- **Controllers** — REST API endpoints (Public / Private / Admin)
- **DTOs** — объекты передачи данных с MapStruct маппингом

## API

Полная спецификация API: `ewm-main-service-spec.json` (OpenAPI 3.0)

Доступ через gateway: `http://localhost:8080`

| Группа | Префикс |
|--------|---------|
| Public | `/categories/**`, `/events/**`, `/compilations/**` |
| Private | `/users/**` |
| Admin | `/admin/**` |
