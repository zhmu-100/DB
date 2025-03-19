package com.db.orm.crud

interface IORMService {
  fun create(table: String, data: Map<String, String>): Boolean

  fun read(
      table: String,
      columns: List<String> = listOf("*"),
      filters: Map<String, String> = emptyMap()
  ): List<Map<String, Any>>

  fun update(
      table: String,
      data: Map<String, String>,
      condition: String,
      conditionParams: List<String>
  ): Boolean

  fun delete(table: String, condition: String, conditionParams: List<String>): Boolean
}
