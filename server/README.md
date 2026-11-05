# Pending Server

---
Backend server for the Pending Android application.

Pending helps users keep track of unresolved responsibilities, such as waiting for replies, refunds, decisions, follow-ups, and other pending tasks.

---

## Technologies

- Java 17
- Spring Boot
- Gradle
- Spring Data JPA
- PostgreSQL
- Supabase
- JWT

---
## Features

The server supports:

- Creating Pending items
- Retrieving all Pending items
- Retrieving one Pending item by ID
- Updating Pending items
- Deleting Pending items
- Persistent PostgreSQL storage through Supabase
- Input validation
- Reminder dates
- Expected response dates
- Initiated dates
- Status tracking
---
## Pending Item Fields

Each Pending item contains:

- `id: Long`
- `ownerId: Long`
- `title: String`
- `waitingOn: String`
- `startDate: LocalDate`
- `expectedResponseDate: LocalDate`
- `status: Integer = 0`
- `notes: String`
- `priority: Integer = 0`
- `createdAt: OffsetDateTime`
- `updatedAt: OffsetDateTime`
- `resolvedAt: OffsetDateTime`
- `deletedAt: OffsetDateTime`
---

## Pending User Fields

- `id: Long`
- `email: String`
- `passwordHash: String`
- `firstName: String`
- `lastName: String`
- `createdAt: OffsetDateTime`
- `updatedAt: OffsetDateTime`

---
# API Endpoints

---

## Pending Controller

### Get all Pending items

GET

`/api/items`

### Get one Pending item

GET

`/api/items/{id}`

### Create a Pending item

POST

`/api/items`

Example request:

```json
{
  "ownerId": 1,
  "title": "Follow up with recruiter",
  "waitingOn": "Recruiter",
  "startDate": "2026-10-07",
  "expectedResponseDate": "2026-10-14",
  "status": 0,
  "notes": "Waiting for an update",
  "priority": 1
}
```

---
# Auth Controller

### Register an Account

POST

`/api/auth/register`

Example request:

```json
{
  "email": "example@wayne.edu",
  "password": "ThisIsAPassword122!",
  "firstName": "John",
  "lastName": "Smith"
}
```

Example response:
```json
{
  "userId": 1,
  "email": "example@wayne.edu",
  "firstName": "John",
  "lastName": "Smith",
  "token": "fudgfudaufiughd...",
  "message": "Login successful"
}
```

### User Login

POST

Example request:

```json
{
  "email": "example@wayne.edu",
  "password": "ThisIsAPassword122!"
}
```

Example response:
```json
{
  "userId": 1,
  "email": "example@wayne.edu",
  "firstName": "John",
  "lastName": "Smith",
  "token": "fudgfudaufiughd...",
  "message": "Login successful"
}
```

### Verify

GET

Example request:
```HTTP
Authorization: Bearer guhguewufodf8749...
```

Successful Response:
```HTTP
JWT token is valid.
```

Failed Response:
```HTTP
Invalid or expired JWT token.
```

---
# Reminder Controller

### Get All Reminders

GET

Example Request:

```HTTP
Authorization: Bearer yJhbGciOiJIUzI1NiJ9...
```

### Get Reminders by Task

GET

`/api/reminders/task/{taskId}`

Retrieves all reminders for a specific task owned by the authenticated user.

Example request:

```HTTP
Authorization: Bearer yJhbGciOiJIUzI1NiJ9...
```

### Schedule a Reminder

POST
`/api/reminders`

Schedules a new reminder for a specific task.

Example request:

```json
{
  "taskId": 18,
  "reminderTime": "2026-11-10T14:30:00Z"
}
```

Example reponse:

```json
{
  "id": 1,
  "taskId": 18,
  "reminderTime": "2026-11-10T14:30:00Z",
  "createdAt": "2026-11-05T12:27:00Z"
}
```

### Delete Reminder

DELETE

`/api/reminders/{id}`

Deletes a scheduled reminder.

Example request:

```HTTP
Authorization: Bearer yJhbGciOiJIUzI1NiJ9...
```

Example response (200 OK):

```HTTP
Reminder deleted.
```