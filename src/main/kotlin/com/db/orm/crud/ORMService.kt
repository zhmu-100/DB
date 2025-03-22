package com.db.orm.crud

import com.db.orm.query.SQLActivity
import com.db.orm.query.SQLExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ORMService(private val executor: SQLExecutor = SQLActivity()) : IORMService {

  override suspend fun create(table: String, data: Map<String, String>): Boolean =
      withContext(Dispatchers.IO) {
        val columns = data.keys.joinToString(", ")
        val placeholders = data.keys.joinToString(", ") { "?" }
        val values = data.values.toList()
        val query = "INSERT INTO $table ($columns) VALUES ($placeholders);"
        val affectedRows = executor.executeParameterizedUpdate(query, values)
        affectedRows > 0
      }

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
        executor.executeParameterizedQuery(queryBuilder.toString(), params)
      }

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
