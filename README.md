# payment-service

Сервис получения платежей из stub и сохранения в БД.

---


## Базовый URL

http://localhost:8080/api/v1

---


## API

---


### POST /payments/fetch

Запрашивает платеж из stub, сохраняет в БД и возвращает результат.

Пример ответа:
```json
{
"id": 42,
"externalId": "a3f1c2d4-1234-4abc-9def-000000000001",
"fromAccount": "ACC-042731",
"toAccount": "ACC-198203",
"amount": 1337.42,
"currency": "USD",
"status": "COMPLETED",
"description": "Online subscription",
"externalCreatedAt": "2024-05-01T12:00:00Z",
"savedAt": "2024-05-01T12:00:00.123Z"
}
```
---


#### Ошибки:
```json
{
"message": "Stub service is unavailable",
"timestamp": "2024-05-01T12:00:00Z"
}
```

---

### GET /payments?limit=10

Возвращает список последних платежей (по убыванию savedAt).

---

## Модель

---

### PaymentResponseDto:

id (long)
externalId (UUID)
fromAccount, toAccount
amount
currency (USD | EUR | RUB | GBP | CHF)
status (COMPLETED | PENDING | PROCESSING | FAILED)
description
externalCreatedAt
savedAt

---

### ErrorDto:

message
timestamp
Запуск

---

## Сборка и запуск:

`docker-compose up -d --build
`
---

## Остановка:

`docker-compose down
`
---

## Пересборка сервиса:

`docker-compose up -d --build payment-service
`
---

## Нагрузочный тест (k6)
`docker-compose --profile load-test up k6 --abort-on-container-exit
`
---