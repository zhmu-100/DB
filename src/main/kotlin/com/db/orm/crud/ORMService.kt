package com.db.orm.crud

import com.db.orm.logging.LoggerProvider
import com.db.orm.query.SQLActivity
import com.db.orm.query.SQLExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Реализация интерфейса [IORMService] для выполнения CRUD операций над бд. */
class ORMService(private val executor: SQLExecutor = SQLActivity()) : IORMService {
  private val logger = LoggerProvider.logger

  /**
   * Создает новую запись в указанной таблице.
   *
   * @param table Имя таблицы.
   * @param data Данные для вставки в виде карты ключ–значение.
   * @return true, если вставка прошла успешно, иначе false.
   */
  override suspend fun create(table: String, data: Map<String, String>): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "ORM: Создание записи",
            additionalData = mapOf("table" to table, "fieldsCount" to data.size.toString()))

        val columns = data.keys.joinToString(", ")
        val placeholders = data.keys.joinToString(", ") { "?" }
        val values = data.values.toList()
        val query = "INSERT INTO $table ($columns) VALUES ($placeholders);"

        try {
          val affectedRows = executor.executeParameterizedUpdate(query, values)
          val success = affectedRows > 0

          logger.logActivity(
              "ORM: Результат создания записи",
              additionalData =
                  mapOf(
                      "table" to table,
                      "success" to success.toString(),
                      "affectedRows" to affectedRows.toString()))

          success
        } catch (e: Exception) {
          logger.logError(
              "ORM: Ошибка при создании записи: table=$table, query=$query",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Выполняет SELECT запрос для получения записей из таблицы.
   *
   * @param table Имя таблицы.
   * @param columns Список колонок для выборки (по умолчанию все).
   * @param filters Карта фильтров для условия WHERE.
   * @return Список записей в виде карты ключ–значение.
   */
  override suspend fun read(
      table: String,
      columns: List<String>,
      filters: Map<String, String>
  ): List<Map<String, Any?>> =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "ORM: Чтение данных",
            additionalData =
                mapOf(
                    "table" to table,
                    "columns" to columns.joinToString(","),
                    "filtersCount" to filters.size.toString()))

        val columnStr = if (columns.isEmpty()) "*" else columns.joinToString(", ")
        val queryBuilder = StringBuilder("SELECT $columnStr FROM $table")
        val params = mutableListOf<Any>()
        if (filters.isNotEmpty()) {
          val conditions = filters.entries.joinToString(" AND ") { "${it.key} = ?" }
          queryBuilder.append(" WHERE $conditions")
          params.addAll(filters.values)
        }

        try {
          val results = executor.executeParameterizedQuery(queryBuilder.toString(), params)

          logger.logActivity(
              "ORM: Результат чтения данных",
              additionalData = mapOf("table" to table, "rowsReturned" to results.size.toString()))

          results
        } catch (e: Exception) {
          logger.logError(
              "ORM: Ошибка при чтении данных: table=$table, query=${queryBuilder.toString()}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Обновляет записи в указанной таблице.
   *
   * @param table Имя таблицы.
   * @param data Новые данные в виде карты ключ–значение.
   * @param condition Условие WHERE с подстановочными знаками (?).
   * @param conditionParams Параметры для условия.
   * @return true, если обновление прошло успешно, иначе false.
   */
  override suspend fun update(
      table: String,
      data: Map<String, String>,
      condition: String,
      conditionParams: List<String>
  ): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "ORM: Обновление данных",
            additionalData =
                mapOf(
                    "table" to table,
                    "fieldsCount" to data.size.toString(),
                    "condition" to condition))

        val setClause = data.keys.joinToString(", ") { "$it = ?" }
        val values = data.values.toList()
        val query = "UPDATE $table SET $setClause WHERE $condition;"

        try {
          val affectedRows = executor.executeParameterizedUpdate(query, values + conditionParams)
          val success = affectedRows > 0

          logger.logActivity(
              "ORM: Результат обновления данных",
              additionalData =
                  mapOf(
                      "table" to table,
                      "success" to success.toString(),
                      "affectedRows" to affectedRows.toString()))

          success
        } catch (e: Exception) {
          logger.logError(
              "ORM: Ошибка при обновлении данных: table=$table, query=$query",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Удаляет записи из указанной таблицы.
   *
   * @param table Имя таблицы.
   * @param condition Условие WHERE с подстановочными знаками (?).
   * @param conditionParams Параметры для условия.
   * @return true, если удаление прошло успешно, иначе false.
   */
  override suspend fun delete(
      table: String,
      condition: String,
      conditionParams: List<String>
  ): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "ORM: Удаление данных",
            additionalData = mapOf("table" to table, "condition" to condition))

        val query = "DELETE FROM $table WHERE $condition;"

        try {
          val affectedRows = executor.executeParameterizedUpdate(query, conditionParams)
          val success = affectedRows > 0

          logger.logActivity(
              "ORM: Результат удаления данных",
              additionalData =
                  mapOf(
                      "table" to table,
                      "success" to success.toString(),
                      "affectedRows" to affectedRows.toString()))

          success
        } catch (e: Exception) {
          logger.logError(
              "ORM: Ошибка при удалении данных: table=$table, query=$query",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }
}
