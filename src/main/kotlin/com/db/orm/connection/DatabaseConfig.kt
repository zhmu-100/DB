package com.db.orm.connection

import io.github.cdimascio.dotenv.dotenv

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
) {
  companion object {
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
