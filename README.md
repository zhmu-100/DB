# DB ORM Service

This project is a standalone microservice built in Kotlin that provides a simple ORM layer for PostgreSQL.

## Environment Setup

Create an `.env` file in the project root with these keys:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=test_orm
DB_USER=postgres
DB_PASSWORD=password
PORT=8081

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
LOGGER_ACTIVITY_CHANNEL=logger:activity
LOGGER_ERROR_CHANNEL=logger:error
```

## Running the Service

1. Ensure PostgreSQL is running.
2. Create and configure the `.env` file.
3. Start the service:

   ```bash
   ./gradlew run
   ```

   The service listens on port **8080**.

## Testing

Clean build with:

```bash
./gradlew clean
```

Run tests with:

```bash
./gradlew test
```

Create report with:

```bash
./gradlew jacocoTestReport
```

## Endpoints (Port: 8080)

- **POST /create**
  Inserts a new record.
  **Example JSON:**

  ```json
  {
    "table": "users",
    "data": {
      "name": "John Doe",
      "email": "john.doe@example.com"
    }
  }
  ```

- **POST /read**
  Performs a parameterized SELECT query by providing the table name, a list of columns to select (default is all
  columns), and optional filters. Example JSON:
- **Example JSON:**

  ```json
  {
    "table": "users",
    "columns": ["id", "name", "email"],
    "filters": {
      "name": "John Doe"
    }
  }
  ```

- **PUT /update**
  Updates records.
  **Example JSON:**

  ```json
  {
    "table": "users",
    "data": { "email": "new.email@example.com" },
    "condition": "id = ?",
    "conditionParams": ["1"]
  }
  ```

- **DELETE /delete**
  Deletes records.
  **Example JSON:**

  ```json
  {
    "table": "users",
    "condition": "id = ?",
    "conditionParams": ["1"]
  }
  ```
