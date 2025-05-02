import io.github.cdimascio.dotenv.Dotenv

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
) {
  companion object {
    fun load(): DatabaseConfig {
      return loadCustom(Dotenv.configure().load())
    }

    internal fun loadCustom(env: Dotenv): DatabaseConfig {
      return DatabaseConfig(
          host = env["DB_HOST"] ?: "localhost",
          port = env["DB_PORT"]?.toIntOrNull() ?: 5432,
          database = env["DB_NAME"] ?: "postgres",
          user = env["DB_USER"] ?: "postgres",
          password = env["DB_PASSWORD"] ?: "")
    }
  }
}
