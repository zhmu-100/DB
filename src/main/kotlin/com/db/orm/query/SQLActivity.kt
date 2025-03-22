package com.db.orm.query

import com.db.orm.connection.DatabaseConnection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация интерфейса [SQLExecutor] для выполнения SQL запросов.
 *
 * Использует пул соединений из [DatabaseConnection] для выполнения запросов.
 *
 * Реализует методы:
 * - [executeQuery]
 * - выполнение SQL запроса без параметров.
 * - [executeParameterizedQuery]
 * - выполнение SQL запроса с параметрами.
 * - [executeParameterizedUpdate]
 * - выполнение SQL запроса на изменение данных с параметрами.
 */
class SQLActivity : SQLExecutor {

  /**
   * Выполняет SQL запрос без параметров и возвращает список записей из БД.
   *
   * @param query Строка запроса
   * @return Список записей в виде карты ключ–значение.
   */
  override suspend fun executeQuery(query: String): List<Map<String, Any?>> =
      withContext(Dispatchers.IO) {
        DatabaseConnection.getConnection().use { connection ->
          connection.createStatement().use { statement ->
            statement.executeQuery(query).use { resultSet -> resultSetToList(resultSet) }
          }
        }
      }

  /**
   * Выполняет SQL запрос с параметрами и возвращает список записей из БД.
   *
   * @param query Строка запроса
   * @param params Список параметров для запроса
   * @return Список записей в виде карты ключ–значение.
   */
  override suspend fun executeParameterizedQuery(
      query: String,
      params: List<Any>
  ): List<Map<String, Any?>> =
      withContext(Dispatchers.IO) {
        DatabaseConnection.getConnection().use { connection ->
          connection.prepareStatement(query).use { preparedStatement ->
            setParameters(preparedStatement, params)
            preparedStatement.executeQuery().use { resultSet -> resultSetToList(resultSet) }
          }
        }
      }

  /**
   * Выполняет SQL запрос на изменение данных с параметрами и возвращает количество измененных
   * записей.
   *
   * @param query Строка запроса
   * @param params Список параметров для запроса
   * @return Количество измененных записей
   */
  override suspend fun executeParameterizedUpdate(query: String, params: List<Any>): Int =
      withContext(Dispatchers.IO) {
        DatabaseConnection.getConnection().use { connection ->
          connection.prepareStatement(query).use { preparedStatement ->
            setParameters(preparedStatement, params)
            preparedStatement.executeUpdate()
          }
        }
      }

  /**
   * Устанавливает параметры в [PreparedStatement].
   *
   * Если параметр является строкой, то происходит проверка с использованием регулярных выражений
   * - Если строка соответствует целому числу, параметр устанавливается как Int.
   * - Если строка соответствует числу с плавающей точкой, параметр устанавливается как Double.
   * - Иначе параметр устанавливается как String.
   *
   * @param preparedStatement Подготовленный SQL-запрос.
   * @param params Список параметров для установки.
   */
  private fun setParameters(preparedStatement: PreparedStatement, params: List<Any>) {
    params.forEachIndexed { index, param ->
      if (param is String) {
        when {
          param.matches(Regex("^-?\\d+$")) -> preparedStatement.setInt(index + 1, param.toInt())
          param.matches(Regex("^-?\\d*\\.\\d+$")) ->
              preparedStatement.setDouble(index + 1, param.toDouble())
          else -> preparedStatement.setString(index + 1, param)
        }
      } else {
        preparedStatement.setObject(index + 1, param)
      }
    }
  }

  /**
   * Преобразует [ResultSet] в список записей.
   *
   * Каждая запись представлена как [Map], где ключ — имя столбца, а значение — объект, полученный
   * из [ResultSet].
   *
   * @param rs Результирующий набор данных.
   * @return Список записей в виде [Map].
   */
  private fun resultSetToList(rs: ResultSet): List<Map<String, Any?>> {
    val metaData = rs.metaData
    val columnCount = metaData.columnCount
    val resultList = mutableListOf<Map<String, Any?>>()

    while (rs.next()) {
      val row = mutableMapOf<String, Any?>()
      for (i in 1..columnCount) {
        val columnName = metaData.getColumnName(i)
        row[columnName] = rs.getObject(i)
      }
      resultList.add(row)
    }
    return resultList
  }
}
