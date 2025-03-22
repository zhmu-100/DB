package com.db.orm.query

interface SQLExecutor {
  suspend fun executeQuery(query: String): List<Map<String, Any?>>

  suspend fun executeParameterizedQuery(query: String, params: List<Any>): List<Map<String, Any?>>

  suspend fun executeParameterizedUpdate(query: String, params: List<Any>): Int
}
