# Explore With Me Plus

Афиша событий: пользователи создают события (выставки, походы в кино, поездки),
находят компанию и подают заявки на участие. Администраторы модерируют события,
ведут категории, подборки и пользователей.

## Сервисы

| Сервис | БД                                         | Назначение                          |
|---|--------------------------------------------|-------------------------------------|
| `stats-server`| `ewm-stats`                                | Сбор и выдача статистики просмотров |
| `main-service` | `ewm-main`|  Основная бизнес-логика продукта    |


## Схема БД


### `ewm-main` — основная БД

```mermaid
erDiagram
    USERS ||--o{ EVENTS : "initiator_id"
    CATEGORIES ||--o{ EVENTS : "category_id"
    USERS ||--o{ REQUESTS : "requester_id"
    EVENTS ||--o{ REQUESTS : "event_id"
    COMPILATIONS ||--o{ COMPILATION_EVENTS : "compilation_id"
    EVENTS ||--o{ COMPILATION_EVENTS : "event_id"

    USERS {
        bigint id "PK"
        varchar name "250"
        varchar email "UK, 254"
    }

    CATEGORIES {
        bigint id "PK"
        varchar name "UK, 50"
    }

    EVENTS {
        bigint id "PK"
        varchar annotation "2000"
        bigint category_id "FK"
        varchar description "7000"
        timestamp event_date
        timestamp created_on
        bigint initiator_id "FK"
        double lat
        double lon
        boolean paid
        integer participant_limit
        boolean request_moderation
        varchar state "20"
        timestamp published_on
        varchar title "120"
    }

    REQUESTS {
        bigint id "PK"
        bigint event_id "FK"
        bigint requester_id "FK"
        timestamp created
        varchar status "20"
    }

    COMPILATIONS {
        bigint id "PK"
        varchar title "50"
        boolean pinned
    }

    COMPILATION_EVENTS {
        bigint compilation_id "PK, FK"
        bigint event_id "PK, FK"
    }
```

### `ewm-stats` — БД статистики

```mermaid
erDiagram
    ENDPOINT_HIT {
        bigint id "PK"
        varchar app "255"
        varchar uri "512"
        varchar ip "45"
        timestamp timestamp
    }
```


