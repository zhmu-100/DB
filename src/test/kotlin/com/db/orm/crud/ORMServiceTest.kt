package com.db.orm.crud

import com.db.orm.query.SQLExecutor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ORMServiceTest {

  private val sqlExecutor = mockk<SQLExecutor>()
  private val ormService = ORMService(sqlExecutor)

  @Test
  fun `test create returns true when successful`() {
    runBlocking {
      coEvery { sqlExecutor.executeParameterizedUpdate(any(), any()) } returns 1

      val result = ormService.create("users", mapOf("name" to "John", "age" to "30"))

      assertTrue(result)
      coVerify { sqlExecutor.executeParameterizedUpdate(any(), any()) }
    }
  }

  @Test
  fun `test create returns false when failed`() {
    runBlocking {
      coEvery { sqlExecutor.executeParameterizedUpdate(any(), any()) } returns 0

      val result = ormService.create("users", mapOf("name" to "John", "age" to "30"))

      assertTrue(!result)
      coVerify { sqlExecutor.executeParameterizedUpdate(any(), any()) }
    }
  }

  @Test
  fun `test read returns list of records`() {
    runBlocking {
      val mockData =
          listOf(
              mapOf("id" to 1, "name" to "John", "age" to 30),
              mapOf("id" to 2, "name" to "Jane", "age" to 25))
      coEvery { sqlExecutor.executeParameterizedQuery(any(), any()) } returns mockData

      val result = ormService.read("users", listOf("id", "name", "age"))

      assertTrue(result.isNotEmpty())
      coVerify { sqlExecutor.executeParameterizedQuery(any(), any()) }
    }
  }

  @Test
  fun `test update returns true when successful`() {
    runBlocking {
      coEvery { sqlExecutor.executeParameterizedUpdate(any(), any()) } returns 1

      val result = ormService.update("users", mapOf("age" to "31"), "id = ?", listOf("1"))

      assertTrue(result)
      coVerify { sqlExecutor.executeParameterizedUpdate(any(), any()) }
    }
  }

  @Test
  fun `test update returns false when failed`() {
    runBlocking {
      coEvery { sqlExecutor.executeParameterizedUpdate(any(), any()) } returns 0

      val result = ormService.update("users", mapOf("age" to "31"), "id = ?", listOf("1"))

      assertTrue(!result)
      coVerify { sqlExecutor.executeParameterizedUpdate(any(), any()) }
    }
  }

  @Test
  fun `test delete returns true when successful`() {
    runBlocking {
      coEvery { sqlExecutor.executeParameterizedUpdate(any(), any()) } returns 1

      val result = ormService.delete("users", "id = ?", listOf("1"))

      assertTrue(result)
      coVerify { sqlExecutor.executeParameterizedUpdate(any(), any()) }
    }
  }

  @Test
  fun `test delete returns false when failed`() {
    runBlocking {
      coEvery { sqlExecutor.executeParameterizedUpdate(any(), any()) } returns 0

      val result = ormService.delete("users", "id = ?", listOf("1"))

      assertTrue(!result)
      coVerify { sqlExecutor.executeParameterizedUpdate(any(), any()) }
    }
  }
}
