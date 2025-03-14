package com.db.orm.crud

import com.db.orm.query.SQLExecutor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ORMServiceTest {

  class FakeSQLExecutor : SQLExecutor {
    var lastQuery: String? = null
    var lastParams: List<Any>? = null

    var updateResult: Int = 0
    var queryResult: List<Map<String, Any>> = emptyList()

    override fun executeQuery(query: String): List<Map<String, Any>> {
      lastQuery = query
      return queryResult
    }

    override fun executeParameterizedQuery(
        query: String,
        params: List<Any>
    ): List<Map<String, Any>> {
      lastQuery = query
      lastParams = params
      return queryResult
    }

    override fun executeParameterizedUpdate(query: String, params: List<Any>): Int {
      lastQuery = query
      lastParams = params
      return updateResult
    }
  }

  @Test
  fun `test create success`() {
    val fakeExecutor = FakeSQLExecutor().apply { updateResult = 1 }
    val ormService = ORMService(fakeExecutor)
    val table = "users"
    val data = mapOf("name" to "John", "email" to "john@example.com")

    val result = ormService.create(table, data)
    assertTrue(result, "Метод create должен вернуть true при успешной вставке")
    assertNotNull(fakeExecutor.lastQuery)
    assertTrue(fakeExecutor.lastQuery!!.startsWith("INSERT INTO $table"))
    assertEquals(listOf("John", "john@example.com"), fakeExecutor.lastParams)
  }

  @Test
  fun `test create failure`() {
    val fakeExecutor = FakeSQLExecutor().apply { updateResult = 0 }
    val ormService = ORMService(fakeExecutor)
    val result = ormService.create("users", mapOf("name" to "John"))
    assertFalse(result, "Метод create должен вернуть false, если ни одна строка не затронута")
  }

  @Test
  fun `test read without params`() {
    val expectedResult = listOf(mapOf("id" to 1, "name" to "Alice"))
    val fakeExecutor = FakeSQLExecutor().apply { queryResult = expectedResult }
    val ormService = ORMService(fakeExecutor)
    val query = "SELECT * FROM users"

    val result = ormService.read(query)
    assertEquals(expectedResult, result)
    assertEquals(query, fakeExecutor.lastQuery)
  }

  @Test
  fun `test read with params`() {
    val expectedResult = listOf(mapOf("id" to 1, "name" to "Alice"))
    val fakeExecutor = FakeSQLExecutor().apply { queryResult = expectedResult }
    val ormService = ORMService(fakeExecutor)
    val query = "SELECT * FROM users WHERE id = ?"
    val params = listOf("1")

    val result = ormService.read(query, params)
    assertEquals(expectedResult, result)
    assertEquals(query, fakeExecutor.lastQuery)
    assertEquals(params, fakeExecutor.lastParams)
  }

  @Test
  fun `test update success`() {
    val fakeExecutor = FakeSQLExecutor().apply { updateResult = 1 }
    val ormService = ORMService(fakeExecutor)
    val table = "users"
    val data = mapOf("name" to "Bob")
    val condition = "id = ?"
    val conditionParams = listOf("1")

    val result = ormService.update(table, data, condition, conditionParams)
    assertTrue(result, "Метод update должен вернуть true при успешном обновлении")
    assertNotNull(fakeExecutor.lastQuery)
    assertTrue(fakeExecutor.lastQuery!!.startsWith("UPDATE $table SET"))
    assertEquals(listOf("Bob", "1"), fakeExecutor.lastParams)
  }

  @Test
  fun `test update failure`() {
    val fakeExecutor = FakeSQLExecutor().apply { updateResult = 0 }
    val ormService = ORMService(fakeExecutor)
    val result = ormService.update("users", mapOf("name" to "Bob"), "id = ?", listOf("1"))
    assertFalse(result, "Метод update должен вернуть false, если ни одна строка не затронута")
  }

  @Test
  fun `test delete success`() {
    val fakeExecutor = FakeSQLExecutor().apply { updateResult = 1 }
    val ormService = ORMService(fakeExecutor)
    val table = "users"
    val condition = "id = ?"
    val conditionParams = listOf("1")

    val result = ormService.delete(table, condition, conditionParams)
    assertTrue(result, "Метод delete должен вернуть true при успешном удалении")
    assertNotNull(fakeExecutor.lastQuery)
    assertTrue(fakeExecutor.lastQuery!!.startsWith("DELETE FROM $table WHERE"))
    assertEquals(conditionParams, fakeExecutor.lastParams)
  }

  @Test
  fun `test delete failure`() {
    val fakeExecutor = FakeSQLExecutor().apply { updateResult = 0 }
    val ormService = ORMService(fakeExecutor)
    val result = ormService.delete("users", "id = ?", listOf("1"))
    assertFalse(result, "Метод delete должен вернуть false, если ни одна строка не затронута")
  }

  @Test
  fun `test create exception handling`() {
    val throwingExecutor =
        object : SQLExecutor {
          override fun executeQuery(query: String): List<Map<String, Any>> =
              throw RuntimeException("forced error")

          override fun executeParameterizedQuery(
              query: String,
              params: List<Any>
          ): List<Map<String, Any>> = throw RuntimeException("forced error")

          override fun executeParameterizedUpdate(query: String, params: List<Any>): Int =
              throw RuntimeException("forced error")
        }
    val ormService = ORMService(throwingExecutor)
    val exception =
        assertThrows<RuntimeException> { ormService.create("users", mapOf("name" to "John")) }
    assertTrue(exception.message?.contains("Error while creating row") == true)
  }
}
