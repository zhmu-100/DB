package com.db.orm.connection

import io.github.cdimascio.dotenv.dotenv

/**
 * Конфигурация для подключения к базе данных PostgreSQL.
 *
 * Содержит следующие параметры:
 * - [host]: адрес сервера базы данных.
 * - [port]: порт сервера базы данных.
 * - [database]: имя базы данных.
 * - [user]: имя пользователя для подключения.
 * - [password]: пароль для подключения.
 *
 * Метод [load] загружает значения из переменных окружения с использованием библиотеки dotenv.
 */
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
) {
  companion object {
    /**
     * Загружает конфигурацию из переменных окружения.
     *
     * - DB_HOST (по умолчанию: "localhost")
     * - DB_PORT (по умолчанию: 5432)
     * - DB_NAME (по умолчанию: postgres, idk)
     * - DB_USER (по умолчанию: postgres)
     * - DB_PASSWORD (по умолчанию: пустая строка)
     *
     * @return Экземпляр [DatabaseConfig] с загруженными параметрами.
     */
    fun load(): DatabaseConfig {
      val dotenv = dotenv()

      return DatabaseConfig(
          host = dotenv["DB_HOST"] ?: "localhost",
          port = dotenv["DB_PORT"]?.toIntOrNull() ?: 5432,
          database = dotenv["DB_NAME"] ?: "postgres",
          user = dotenv["DB_USER"] ?: "postgres",
          password = dotenv["DB_PASSWORD"] ?: "")
    }
  }
}
