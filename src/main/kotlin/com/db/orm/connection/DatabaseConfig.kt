package com.db.orm.connection

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
) {
  companion object {
    fun load(): DatabaseConfig {
      val host = EnvLoader.get("DB_HOST") ?: "localhost"
      val port = EnvLoader.get("DB_PORT")?.toIntOrNull() ?: 5432
      val database = EnvLoader.get("DB_NAME") ?: "postgres"
      val user = EnvLoader.get("DB_USER") ?: "postgres"
      val password = EnvLoader.get("DB_PASSWORD") ?: ""
      return DatabaseConfig(host, port, database, user, password)
    }
  }
}
