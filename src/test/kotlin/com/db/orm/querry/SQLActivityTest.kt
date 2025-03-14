package com.db.orm.query

import com.db.orm.connection.DatabaseConnection
import io.mockk.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SQLActivityTest {

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `test executeQuery returns correct result`() {
    val mockConnection = mockk<Connection>(relaxed = true)
    val mockStatement = mockk<java.sql.Statement>(relaxed = true)
    val mockResultSet = mockk<ResultSet>(relaxed = true)
    val mockMetaData = mockk<ResultSetMetaData>(relaxed = true)

    mockkObject(DatabaseConnection)
    every { DatabaseConnection.getConnection() } returns mockConnection

    every { mockConnection.createStatement() } returns mockStatement
    every { mockStatement.executeQuery(any()) } returns mockResultSet

    every { mockResultSet.metaData } returns mockMetaData
    every { mockMetaData.columnCount } returns 2
    every { mockMetaData.getColumnName(1) } returns "name"
    every { mockMetaData.getColumnName(2) } returns "email"

    every { mockResultSet.next() } returnsMany listOf(true, false)
    every { mockResultSet.getObject(1) } returns "Alice"
    every { mockResultSet.getObject(2) } returns "alice@example.com"

    val executor = SQLActivity()

    val result = executor.executeQuery("SELECT * FROM test_table")

    assertEquals(1, result.size)
    val row = result[0]
    assertEquals("Alice", row["name"])
    assertEquals("alice@example.com", row["email"])
  }

  @Test
  fun `test executeParameterizedQuery returns correct result`() {
    val mockConnection = mockk<Connection>(relaxed = true)
    val mockPreparedStatement = mockk<PreparedStatement>(relaxed = true)
    val mockResultSet = mockk<ResultSet>(relaxed = true)
    val mockMetaData = mockk<ResultSetMetaData>(relaxed = true)

    mockkObject(DatabaseConnection)
    every { DatabaseConnection.getConnection() } returns mockConnection

    every { mockConnection.prepareStatement(any()) } returns mockPreparedStatement
    every { mockPreparedStatement.executeQuery() } returns mockResultSet

    every { mockResultSet.metaData } returns mockMetaData
    every { mockMetaData.columnCount } returns 2
    every { mockMetaData.getColumnName(1) } returns "name"
    every { mockMetaData.getColumnName(2) } returns "email"

    every { mockResultSet.next() } returnsMany listOf(true, false)
    every { mockResultSet.getObject(1) } returns "Bob"
    every { mockResultSet.getObject(2) } returns "bob@example.com"

    val executor = SQLActivity()

    val result =
        executor.executeParameterizedQuery("SELECT * FROM test_table WHERE name = ?", listOf("Bob"))

    assertEquals(1, result.size)
    val row = result[0]
    assertEquals("Bob", row["name"])
    assertEquals("bob@example.com", row["email"])

    verify { mockPreparedStatement.setObject(1, "Bob") }
  }

  @Test
  fun `test executeParameterizedUpdate returns update count`() {
    val mockConnection = mockk<Connection>(relaxed = true)
    val mockPreparedStatement = mockk<PreparedStatement>(relaxed = true)

    mockkObject(DatabaseConnection)
    every { DatabaseConnection.getConnection() } returns mockConnection

    every { mockConnection.prepareStatement(any()) } returns mockPreparedStatement
    every { mockPreparedStatement.executeUpdate() } returns 1

    val executor = SQLActivity()

    val updateCount =
        executor.executeParameterizedUpdate(
            "UPDATE test_table SET name = ? WHERE email = ?",
            listOf("Charlie", "charlie@example.com"))

    assertEquals(1, updateCount)
    verify { mockPreparedStatement.setObject(1, "Charlie") }
    verify { mockPreparedStatement.setObject(2, "charlie@example.com") }
  }
}
