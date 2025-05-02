package com.db.orm.connection

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

  init {
    repeat(INITIAL_POOL_SIZE) {
      val conn = createNewConnection()
      connectionPool.offer(conn)
      totalConnections.incrementAndGet()
    }
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
    return try {
      DriverManager.getConnection(url, config.user, config.password)
    } catch (e: SQLException) {
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
    val conn =
        connectionPool.poll()
            ?: run {
              if (totalConnections.get() < MAX_POOL_SIZE) {
                totalConnections.incrementAndGet()
                createNewConnection()
              } else {
                connectionPool.poll(30, TimeUnit.SECONDS)
                    ?: throw RuntimeException("Timeout waiting for a database connection")
              }
            }
    return PooledConnection(conn)
  }

  /**
   * Возвращает соединение в пул.
   *
   * @param connection Соединение, которое нужно вернуть.
   */
  internal fun releaseConnection(connection: Connection) {
    connectionPool.offer(connection)
  }

  /** Закрывает все соединения в пуле. */
  fun close() {
    connectionPool.forEach { conn ->
      try {
        conn.close()
      } catch (e: SQLException) {
        e.printStackTrace()
      }
    }
  }

  /** Обертка над соединением, которая при вызове [close] возвращает соединение в пул. */
  private class PooledConnection(private val connection: Connection) : Connection by connection {
    override fun close() {
      releaseConnection(connection)
    }
  }
}
