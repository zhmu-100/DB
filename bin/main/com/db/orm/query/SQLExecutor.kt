package com.db.orm.query

/**
 * Интерфейс для выполнения SQL запросов.
 *
 * Определяет операции:
 * - [executeQuery]
 * - выполнение SQL запроса без параметров.
 * - [executeParameterizedQuery]
 * - выполнение SQL запроса с параметрами.
 * - [executeParameterizedUpdate]
 * - выполнение SQL запроса на изменение данных с параметрами.
 */
interface SQLExecutor {

  /**
   * Выполняет SQL запрос без параметров и возвращает список записей из БД.
   *
   * @param query Строка запроса
   * @return Список записей в виде карты ключ–значение.
   */
  suspend fun executeQuery(query: String): List<Map<String, Any?>>

  /**
   * Выполняет SQL запрос с параметрами и возвращает список записей из БД.
   *
   * @param query Строка запроса
   * @param params Список параметров для запроса
   * @return Список записей в виде карты ключ–значение.
   */
  suspend fun executeParameterizedQuery(query: String, params: List<Any>): List<Map<String, Any?>>

  /**
   * Выполняет SQL запрос на изменение данных с параметрами и возвращает количество измененных
   * записей.
   *
   * @param query Строка запроса
   * @param params Список параметров для запроса
   * @return Количество измененных записей
   */
  suspend fun executeParameterizedUpdate(query: String, params: List<Any>): Int
}
