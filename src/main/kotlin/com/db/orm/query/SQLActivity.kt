package com.db.orm.query

import com.db.orm.connection.DatabaseConnection
import java.sql.PreparedStatement
import java.sql.ResultSet

class SQLActivity : SQLExecutor {

  override fun executeQuery(query: String): List<Map<String, Any?>> {
    DatabaseConnection.getConnection().use { connection ->
      connection.createStatement().use { statement ->
        statement.executeQuery(query).use { resultSet ->
          return resultSetToList(resultSet)
        }
      }
    }
  }

  override fun executeParameterizedQuery(
      query: String,
      params: List<Any>
  ): List<Map<String, Any?>> {
    DatabaseConnection.getConnection().use { connection ->
      connection.prepareStatement(query).use { preparedStatement ->
        setParameters(preparedStatement, params)
        preparedStatement.executeQuery().use { resultSet ->
          return resultSetToList(resultSet)
        }
      }
    }
  }

  override fun executeParameterizedUpdate(query: String, params: List<Any>): Int {
    DatabaseConnection.getConnection().use { connection ->
      connection.prepareStatement(query).use { preparedStatement ->
        setParameters(preparedStatement, params)
        return preparedStatement.executeUpdate()
      }
    }
  }

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
