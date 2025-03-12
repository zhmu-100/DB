package com.db.orm.connection

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConnection {
    private var connection: Connection? = null

    fun getConnection(): Connection {
        if (connection == null || connection!!.isClosed) {
            connection = createConnection()
        }
        return connection!!
    }

    private fun createConnection(): Connection {
        val config = DatabaseConfig.load()
        val url = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
        return try {
            DriverManager.getConnection(url, config.user, config.password)
        } catch (e: SQLException) {
            throw RuntimeException("Error while connecting to db: ${e.message}", e)
        }
    }

    fun closeConnection() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}