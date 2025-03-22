package com.db.orm.crud

interface IORMService {
  suspend fun create(table: String, data: Map<String, String>): Boolean

  suspend fun read(
      table: String,
      columns: List<String> = listOf("*"),
      filters: Map<String, String> = emptyMap()
  ): List<Map<String, Any?>>

  suspend fun update(
      table: String,
      data: Map<String, String>,
      condition: String,
      conditionParams: List<String>
  ): Boolean

  suspend fun delete(table: String, condition: String, conditionParams: List<String>): Boolean
}
