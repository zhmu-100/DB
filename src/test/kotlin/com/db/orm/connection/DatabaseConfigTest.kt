package com.db.orm.connection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DatabaseConfigTest {

  @Test
  fun `test load returns expected values from environment or defaults`() {
    val config = DatabaseConfig.load()

    val expectedHost = EnvLoader.get("DB_HOST") ?: "localhost"
    val expectedPort = EnvLoader.get("DB_PORT")?.toIntOrNull() ?: 5432
    val expectedDatabase = EnvLoader.get("DB_NAME") ?: "postgres"
    val expectedUser = EnvLoader.get("DB_USER") ?: "postgres"
    val expectedPassword = EnvLoader.get("DB_PASSWORD") ?: ""

    assertEquals(expectedHost, config.host, "Expected: $expectedHost")
    assertEquals(expectedPort, config.port, "Expected: $expectedPort")
    assertEquals(expectedDatabase, config.database, "Expected: $expectedDatabase")
    assertEquals(expectedUser, config.user, "Expected: $expectedUser")
    assertEquals(expectedPassword, config.password, "Expected: $expectedPassword")
  }

  @Test
  fun `test load returns non-null and valid values`() {
    val config = DatabaseConfig.load()

    assertNotNull(config.host, "Host не должен быть null")
    assertTrue(config.port > 0, "Порт должен быть положительным числом")
    assertNotNull(config.database, "Database не должна быть null")
    assertNotNull(config.user, "User не должен быть null")
    assertNotNull(config.password, "Password не должен быть null")
  }
}
