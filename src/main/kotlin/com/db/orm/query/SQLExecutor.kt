package com.db.orm.query

interface SQLExecutor {
    fun executeQuery(query: String): List<Map<String, Any>>
    fun executeParameterizedQuery(query: String, params: List<Any>): List<Map<String, Any>>
    fun executeParameterizedUpdate(query: String, params: List<Any>): Int
}
