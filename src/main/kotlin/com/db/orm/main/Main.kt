package com.db.orm.main

import com.db.orm.connection.DatabaseConnection

fun main() {
    try {
        val connection = DatabaseConnection.getConnection()
        if (connection.isValid(2)) {
            println("connection gud")
        } else {
            println("connection not alyo")
        }
    } catch (e: Exception) {
        println("boom: ${e.message}")
    } finally {
        DatabaseConnection.closeConnection()
    }
}
