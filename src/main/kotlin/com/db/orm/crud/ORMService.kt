package com.db.orm.crud

import com.db.orm.query.SQLActivity
import com.db.orm.query.SQLExecutor

class ORMService(private val executor: SQLExecutor = SQLActivity()) : IORMService {

  override fun create(table: String, data: Map<String, String>): Boolean {
    val columns = data.keys.joinToString(", ")
    val placeholders = data.keys.joinToString(", ") { "?" }
    val values = data.values.toList()

    val query = "INSERT INTO $table ($columns) VALUES ($placeholders);"
    return try {
      val affectedRows = executor.executeParameterizedUpdate(query, values)
      affectedRows > 0
    } catch (e: Exception) {
      throw RuntimeException("Error while creating row: ${e.message}", e)
    }
  }

  override fun read(query: String, params: List<String>): List<Map<String, Any>> {
    return if (params.isEmpty()) {
      executor.executeQuery(query)
    } else {
      executor.executeParameterizedQuery(query, params)
    }
  }

  override fun update(
      table: String,
      data: Map<String, String>,
      condition: String,
      conditionParams: List<String>
  ): Boolean {
    val setClause = data.keys.joinToString(", ") { "$it = ?" }
    val values = data.values.toList()
    val query = "UPDATE $table SET $setClause WHERE $condition;"
    val allParams = values + conditionParams
    return try {
      val affectedRows = executor.executeParameterizedUpdate(query, allParams)
      affectedRows > 0
    } catch (e: Exception) {
      throw RuntimeException("Error updating data: ${e.message}", e)
    }
  }

  override fun delete(table: String, condition: String, conditionParams: List<String>): Boolean {
    val query = "DELETE FROM $table WHERE $condition;"
    return try {
      val affectedRows = executor.executeParameterizedUpdate(query, conditionParams)
      affectedRows > 0
    } catch (e: Exception) {
      throw RuntimeException("Error deleting data: ${e.message}", e)
    }
  }
}
