package com.db.orm

import io.github.cdimascio.dotenv.Dotenv
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DatabaseConfigTest {

  @Test
  fun `test loadCustom with valid environment variables`() {
    val mockEnv = mockk<Dotenv>()
    every { mockEnv["DB_HOST"] } returns "test-host"
    every { mockEnv["DB_PORT"] } returns "5433"
    every { mockEnv["DB_NAME"] } returns "test-db"
    every { mockEnv["DB_USER"] } returns "test-user"
    every { mockEnv["DB_PASSWORD"] } returns "test-password"

    val config = DatabaseConfig.loadCustom(mockEnv)

    assertEquals("test-host", config.host)
    assertEquals(5433, config.port)
    assertEquals("test-db", config.database)
    assertEquals("test-user", config.user)
    assertEquals("test-password", config.password)
  }

  @Test
  fun `test loadCustom with default values`() {
    val mockEnv = mockk<Dotenv>()
    every { mockEnv["DB_HOST"] } returns null
    every { mockEnv["DB_PORT"] } returns null
    every { mockEnv["DB_NAME"] } returns null
    every { mockEnv["DB_USER"] } returns null
    every { mockEnv["DB_PASSWORD"] } returns null

    val config = DatabaseConfig.loadCustom(mockEnv)

    assertEquals("localhost", config.host)
    assertEquals(5432, config.port)
    assertEquals("postgres", config.database)
    assertEquals("postgres", config.user)
    assertEquals("", config.password)
  }
}
