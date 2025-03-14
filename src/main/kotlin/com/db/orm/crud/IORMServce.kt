package com.db.orm.crud

interface IORMService {
  fun create(table: String, data: Map<String, String>): Boolean

  fun read(query: String, params: List<String> = emptyList()): List<Map<String, Any>>

  fun update(
      table: String,
      data: Map<String, String>,
      condition: String,
      conditionParams: List<String>
  ): Boolean

  fun delete(table: String, condition: String, conditionParams: List<String>): Boolean
}
