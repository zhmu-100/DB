package com.db.orm.connection

import com.db.orm.logging.LoggerProvider
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Менеджер пула соединений с бд.
 *
 * Реализует инициализацию пула, выдачу соединений через метод [getConnection], возвращение
 * соединений в пул через [releaseConnection] и полное закрытие пула методом [close].
 *
 * Соединения оборачиваются в [PooledConnection] для возврата в пул при вызове метода [close].
 */
object DatabaseConnection {
  private const val INITIAL_POOL_SIZE = 10
  private const val MAX_POOL_SIZE = 20
  private val connectionPool = LinkedBlockingQueue<Connection>(MAX_POOL_SIZE)
  private val totalConnections = AtomicInteger(0)
  private val logger = LoggerProvider.logger

  init {
    logger.logActivity(
        "Инициализация пула соединений с БД",
        additionalData =
            mapOf(
                "initialPoolSize" to INITIAL_POOL_SIZE.toString(),
                "maxPoolSize" to MAX_POOL_SIZE.toString()))

    repeat(INITIAL_POOL_SIZE) {
      try {
        val conn = createNewConnection()
        connectionPool.offer(conn)
        totalConnections.incrementAndGet()
      } catch (e: Exception) {
        logger.logError(
            "Ошибка при создании соединения с БД",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
      }
    }

    logger.logActivity(
        "Пул соединений с БД инициализирован",
        additionalData = mapOf("connectionsCreated" to totalConnections.get().toString()))
  }

  /**
   * Создает новое соединение с базой данных, используя конфигурацию из [DatabaseConfig].
   *
   * @return Новое соединение [Connection].
   * @throws RuntimeException если возникла ошибка подключения.
   */
  private fun createNewConnection(): Connection {
    val config = DatabaseConfig.load()
    val url = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"

    logger.logActivity(
        "Создание нового соединения с БД",
        additionalData =
            mapOf(
                "host" to config.host,
                "port" to config.port.toString(),
                "database" to config.database))

    return try {
      DriverManager.getConnection(url, config.user, config.password)
    } catch (e: SQLException) {
      logger.logError(
          "Ошибка при подключении к БД: host=${config.host}, port=${config.port}, database=${config.database}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw RuntimeException("Error while connecting to db: ${e.message}", e)
    }
  }

  /**
   * Возвращает соединение из пула. Если пул пуст, пытается создать новое соединение, если общее
   * количество соединений меньше [MAX_POOL_SIZE]. В противном случае ожидает до 30 секунд.
   *
   * @return Соединение, обернутое в [PooledConnection].
   * @throws RuntimeException если время ожидания превышено.
   */
  fun getConnection(): Connection {
    logger.logActivity("Запрос соединения из пула")

    val conn =
        connectionPool.poll()
            ?: run {
              if (totalConnections.get() < MAX_POOL_SIZE) {
                logger.logActivity(
                    "Создание нового соединения (пул пуст)",
                    additionalData =
                        mapOf(
                            "currentConnections" to totalConnections.get().toString(),
                            "maxPoolSize" to MAX_POOL_SIZE.toString()))
                totalConnections.incrementAndGet()
                createNewConnection()
              } else {
                logger.logActivity(
                    "Ожидание освобождения соединения",
                    additionalData =
                        mapOf(
                            "timeout" to "30 seconds",
                            "currentConnections" to totalConnections.get().toString()))
                connectionPool.poll(30, TimeUnit.SECONDS)
                    ?: run {
                      logger.logError(
                          "Превышено время ожидания соединения с БД",
                          errorMessage = "Timeout waiting for a database connection",
                      )
                      throw RuntimeException("Timeout waiting for a database connection")
                    }
              }
            }

    logger.logActivity("Соединение получено из пула")
    return PooledConnection(conn)
  }

  /**
   * Возвращает соединение в пул.
   *
   * @param connection Соединение, которое нужно вернуть.
   */
  internal fun releaseConnection(connection: Connection) {
    logger.logActivity("Возврат соединения в пул")
    connectionPool.offer(connection)
  }

  /** Закрывает все соединения в пуле. */
  fun close() {
    logger.logActivity(
        "Закрытие пула соединений",
        additionalData = mapOf("totalConnections" to totalConnections.get().toString()))

    var closedConnections = 0
    var failedConnections = 0

    connectionPool.forEach { conn ->
      try {
        conn.close()
        closedConnections++
      } catch (e: SQLException) {
        failedConnections++
        logger.logError(
            "Ошибка при закрытии соединения с БД",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        e.printStackTrace()
      }
    }

    logger.logActivity(
        "Пул соединений закрыт",
        additionalData =
            mapOf(
                "closedConnections" to closedConnections.toString(),
                "failedConnections" to failedConnections.toString()))
  }

  /** Обертка над соединением, которая при вызове [close] возвращает соединение в пул. */
  private class PooledConnection(private val connection: Connection) : Connection by connection {
    override fun close() {
      releaseConnection(connection)
    }
  }
}
