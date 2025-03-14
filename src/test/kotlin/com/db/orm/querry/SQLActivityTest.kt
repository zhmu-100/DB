package com.db.orm.query

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SQLActivityTest {

  private val executor: SQLExecutor = SQLActivity()

  @BeforeEach
  fun setup() {
    val createTableQuery =
        """
            CREATE TABLE IF NOT EXISTS test_table (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100),
                email VARCHAR(100)
            );
        """
            .trimIndent()
    executor.executeParameterizedUpdate(createTableQuery, emptyList())
  }

  @AfterEach
  fun tearDown() {
    val dropTableQuery = "DROP TABLE IF EXISTS test_table;"
    executor.executeParameterizedUpdate(dropTableQuery, emptyList())
  }

  @Test
  fun testExecuteQuery() {
    val insertQuery = "INSERT INTO test_table (name, email) VALUES (?, ?);"
    val affectedRows =
        executor.executeParameterizedUpdate(insertQuery, listOf("Alice", "alice@example.com"))
    assertTrue(affectedRows > 0, "Expected at least one affected row on insert")

    val selectQuery = "SELECT * FROM test_table;"
    val results = executor.executeQuery(selectQuery)
    assertTrue(results.isNotEmpty(), "Expected non-empty result set")
    val row = results.first()
    assertEquals("Alice", row["name"]?.toString(), "Name should be 'Alice'")
    assertEquals(
        "alice@example.com", row["email"]?.toString(), "Email should be 'alice@example.com'")
  }

  @Test
  fun testExecuteParameterizedQuery() {
    executor.executeParameterizedUpdate(
        "INSERT INTO test_table (name, email) VALUES (?, ?);", listOf("Bob", "bob@example.com"))
    executor.executeParameterizedUpdate(
        "INSERT INTO test_table (name, email) VALUES (?, ?);",
        listOf("Charlie", "charlie@example.com"))

    val query = "SELECT * FROM test_table WHERE name = ?;"
    val results = executor.executeParameterizedQuery(query, listOf("Bob"))
    assertEquals(1, results.size, "Expected exactly one record for Bob")
    val row = results[0]
    assertEquals("Bob", row["name"]?.toString(), "Name should be 'Bob'")
    assertEquals("bob@example.com", row["email"]?.toString(), "Email should be 'bob@example.com'")
  }

  @Test
  fun testExecuteParameterizedUpdate() {
    executor.executeParameterizedUpdate(
        "INSERT INTO test_table (name, email) VALUES (?, ?);", listOf("Dave", "dave@example.com"))
    val updateQuery = "UPDATE test_table SET email = ? WHERE name = ?;"
    val affectedRows =
        executor.executeParameterizedUpdate(updateQuery, listOf("dave.new@example.com", "Dave"))
    assertTrue(affectedRows > 0, "Expected at least one affected row on update")

    val selectQuery = "SELECT email FROM test_table WHERE name = ?;"
    val results = executor.executeParameterizedQuery(selectQuery, listOf("Dave"))
    assertEquals(1, results.size, "Expected exactly one record for Dave")
    assertEquals(
        "dave.new@example.com",
        results[0]["email"]?.toString(),
        "Email should be updated to 'dave.new@example.com'")
  }
}
