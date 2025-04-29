package com.db.orm.crud

import com.db.orm.query.SQLActivity
import com.db.orm.query.SQLExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Реализация интерфейса [IORMService] для выполнения CRUD операций над бд. */
class ORMService(private val executor: SQLExecutor = SQLActivity()) : IORMService {

  /**
   * Создает новую запись в указанной таблице.
   *
   * @param table Имя таблицы.
   * @param data Данные для вставки в виде карты ключ–значение.
   * @return true, если вставка прошла успешно, иначе false.
   */
  override suspend fun create(table: String, data: Map<String, String>): Boolean =
      withContext(Dispatchers.IO) {
        val columns = data.keys.joinToString(", ")
        val placeholders = data.keys.joinToString(", ") { "?" }
        val values = data.values.toList()
        val query = "INSERT INTO $table ($columns) VALUES ($placeholders);"
        val affectedRows = executor.executeParameterizedUpdate(query, values)
        affectedRows > 0
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
        val columnStr = if (columns.isEmpty()) "*" else columns.joinToString(", ")
        val queryBuilder = StringBuilder("SELECT $columnStr FROM $table")
        val params = mutableListOf<Any>()
        if (filters.isNotEmpty()) {
          val conditions = filters.entries.joinToString(" AND ") { "${it.key} = ?" }
          queryBuilder.append(" WHERE $conditions")
          params.addAll(filters.values)
        }
          val foods = executor.executeParameterizedQuery(queryBuilder.toString(), params)
          if (table != "foods") return@withContext foods
          return@withContext foods.map { food ->
              val foodId = food["id"]?.toString() ?: ""
              val vitamins = getVitaminsForFood(foodId)
              val minerals = getMineralsForFood(foodId)

              food.toMutableMap().apply {
                  put("vitamins", vitamins)
                  put("minerals", minerals)
              }
          }
      }

    /**
     * Получает список витаминов, связанных с продуктом.
     *
     * @param foodId Идентификатор продукта.
     * @return Список витаминов с их количеством и единицами измерения.
     */
    private suspend fun getVitaminsForFood(foodId: String): List<Map<String, Any?>> {
        val query = """
            SELECT v.id, v.name, v.unit, fv.amount 
            FROM food_vitamins fv
            JOIN vitamins v ON fv.vitamin_id = v.id
            WHERE fv.food_id = ?
        """.trimIndent()

        return executor.executeParameterizedQuery(query, listOf(foodId)).map {
            mapOf(
                "id" to it["id"],
                "name" to it["name"],
                "amount" to it["amount"],
                "unit" to it["unit"]
            )
        }
    }

    /**
     * Получает список минералов, связанных с продуктом.
     *
     * @param foodId Идентификатор продукта.
     * @return Список минералов с их количеством и единицами измерения.
     */
    private suspend fun getMineralsForFood(foodId: String): List<Map<String, Any?>> {
        val query = """
            SELECT m.id, m.name, m.unit, fm.amount 
            FROM food_minerals fm
            JOIN minerals m ON fm.mineral_id = m.id
            WHERE fm.food_id = ?
        """.trimIndent()

        return executor.executeParameterizedQuery(query, listOf(foodId)).map {
            mapOf(
                "id" to it["id"],
                "name" to it["name"],
                "amount" to it["amount"],
                "unit" to it["unit"]
            )
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
        val setClause = data.keys.joinToString(", ") { "$it = ?" }
        val values = data.values.toList()
        val query = "UPDATE $table SET $setClause WHERE $condition;"
        val affectedRows = executor.executeParameterizedUpdate(query, values + conditionParams)
        affectedRows > 0
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
        val query = "DELETE FROM $table WHERE $condition;"
        val affectedRows = executor.executeParameterizedUpdate(query, conditionParams)
        affectedRows > 0
      }
}
