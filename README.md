# Pending Server

Backend server for the Pending Android application.

Pending helps users keep track of unresolved responsibilities, such as waiting for replies, refunds, decisions, follow-ups, and other pending tasks.

## Technologies

- Java 17
- Spring Boot
- Gradle
- Spring Data JPA
- PostgreSQL
- Supabase

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

## Pending Item Fields

Each Pending item contains:

- `id`
- `title`
- `waitingOn`
- `description`
- `status`
- `initiatedDate`
- `expectedResponseDate`
- `reminderDate`

## API Endpoints

### Test server

GET

`/api/test`

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
  "title": "Follow up with recruiter",
  "waitingOn": "Recruiter",
  "description": "Waiting for an update",
  "status": "Pending",
  "initiatedDate": "2026-10-07",
  "expectedResponseDate": "2026-10-14",
  "reminderDate": "2026-10-12"
}