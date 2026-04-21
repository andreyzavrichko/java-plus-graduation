# Explore With Me - Main Service

Основной сервис платформы для поиска и организации событий.

## Технологический стек

- **Java** - язык программирования
- **Spring Boot** - фреймворк для разработки приложения
- **Spring Data JPA** - работа с БД
- **PostgreSQL** - система управления БД
- **QueryDSL** - построение динамических запросов
- **MapStruct** - маппинг DTO ↔ Entity
- **Lombok** - уменьшение boilerplate кода
- **Maven** - система управления зависимостями

## Архитектура

Многомодульный проект `explore-with-me`:
- `main-service` - основной сервис (текущий модуль)
- `stats-client` - клиент для сервиса статистики
- `stats-dto` - DTO для обмена данными со статистикой

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

**База данных:**
- URL: `jdbc:postgresql://ewm-db:5432/ewm-main`
- Пользователь: `main`
- Пароль: `main`

**Приложение:**
- Порт: `8080`
- URL сервиса статистики: `http://localhost:9090`
- DDL-auto: `none` (миграции управляются через `schema.sql`)

## Установка и запуск

1. Убедитесь, что PostgreSQL запущен на `ewm-db:5432`
2. Скомпилируйте проект: `mvn clean install`
3. Запустите сервис: `mvn spring-boot:run`
4. Приложение будет доступно по `http://localhost:8080`

## Основные компоненты

- **Entities** - JPA сущности (User, Event, Category, ParticipationRequest и т.д.)
- **Repositories** - Spring Data JPA репозитории с QueryDSL поддержкой
- **Services** - бизнес-логика
- **Controllers** - REST API endpoints
- **DTOs** - объекты передачи данных с MapStruct маппингом
