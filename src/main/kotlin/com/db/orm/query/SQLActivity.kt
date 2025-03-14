package com.db.orm.query

import com.db.orm.connection.DatabaseConnection
import java.sql.ResultSet

class SQLActivity : SQLExecutor {

    override fun executeQuery(query: String): List<Map<String, Any>> {
        DatabaseConnection.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(query).use { resultSet ->
                    return resultSetToList(resultSet)
                }
            }
        }
    }

    override fun executeParameterizedQuery(query: String, params: List<Any>): List<Map<String, Any>> {
        DatabaseConnection.getConnection().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                params.forEachIndexed { index, param ->
                    preparedStatement.setObject(index + 1, param)
                }
                preparedStatement.executeQuery().use { resultSet ->
                    return resultSetToList(resultSet)
                }
            }
        }
    }

    override fun executeParameterizedUpdate(query: String, params: List<Any>): Int {
        DatabaseConnection.getConnection().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                params.forEachIndexed { index, param ->
                    preparedStatement.setObject(index + 1, param)
                }
                return preparedStatement.executeUpdate()
            }
        }
    }

    private fun resultSetToList(rs: ResultSet): List<Map<String, Any>> {
        val metaData = rs.metaData
        val columnCount = metaData.columnCount
        val resultList = mutableListOf<Map<String, Any>>()

        while (rs.next()) {
            val row = mutableMapOf<String, Any>()
            for (i in 1..columnCount) {
                val columnName = metaData.getColumnName(i)
                row[columnName] = rs.getObject(i)
            }
            resultList.add(row)
        }
        return resultList
    }
}
