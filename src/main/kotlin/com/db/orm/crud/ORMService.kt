package com.db.orm.crud

import com.db.orm.query.SQLActivity
import com.db.orm.query.SQLExecutor

class ORMService(private val executor: SQLExecutor = SQLActivity()) {

    /**
     * Создание новой записи в таблице.
     *
     * @param table Имя таблицы.
     * @param data Словарь, где ключ — имя колонки, значение — значение для вставки.
     * @return true, если вставка прошла успешно (затронута хотя бы одна строка), иначе false.
     */
    fun create(table: String, data: Map<String, String>): Boolean {
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

    /**
     * Чтение данных.
     *
     * @param query SQL запрос для выборки данных.
     * @param params Список параметров для параметризованного запроса.
     * @return Список строк, где каждая строка представлена в виде Map<ИмяКолонки, Значение>.
     */
    fun read(query: String, params: List<String> = emptyList()): List<Map<String, Any>> {
        return if (params.isEmpty()) {
            executor.executeQuery(query)
        } else {
            executor.executeParameterizedQuery(query, params)
        }
    }

    /**
     * Обновление записей в таблице.
     *
     * @param table Имя таблицы.
     * @param data Словарь новых значений для колонок.
     * @param condition Условие обновления (например, "id = ?").
     * @param conditionParams Список значений для параметров условия.
     * @return true, если обновление затронуло хотя бы одну строку, иначе false.
     */
    fun update(table: String, data: Map<String, String>, condition: String, conditionParams: List<String>): Boolean {
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

    /**
     * Удаление записей из таблицы.
     *
     * @param table Имя таблицы.
     * @param condition Условие удаления (например, "id = ?").
     * @param conditionParams Список значений для параметров условия.
     * @return true, если удаление затронуло хотя бы одну строку, иначе false.
     */
    fun delete(table: String, condition: String, conditionParams: List<String>): Boolean {
        val query = "DELETE FROM $table WHERE $condition;"
        return try {
            val affectedRows = executor.executeParameterizedUpdate(query, conditionParams)
            affectedRows > 0
        } catch (e: Exception) {
            throw RuntimeException("Error deleting data: ${e.message}", e)
        }
    }
}
