package com.db.orm.crud

/** Интерфейс сервиса ORM для выполнения базовых CRUD операций над базой данных. */
interface IORMService {

  /**
   * Создает новую запись в указанной таблице.
   *
   * @param table Имя таблицы.
   * @param data Пара ключ-значение, представляющая данные для вставки.
   * @return true, если запись успешно создана; false в противном случае.
   */
  suspend fun create(table: String, data: Map<String, String>): Boolean

  /**
   * Выполняет SELECT запрос для получения записей из таблицы.
   *
   * @param table Имя таблицы.
   * @param columns Список колонок для выборки (по умолчанию – все).
   * @param filters Карта фильтров (ключ-значение) для условия WHERE.
   * @return Список записей в виде карты (ключ–значение).
   */
  suspend fun read(
      table: String,
      columns: List<String> = listOf("*"),
      filters: Map<String, String> = emptyMap()
  ): List<Map<String, Any?>>

  /**
   * Обновляет записи в указанной таблице.
   *
   * @param table Имя таблицы.
   * @param data Пара ключ-значение с новыми данными.
   * @param condition Условие WHERE в виде строки с подстановочными знаками (?).
   * @param conditionParams Список параметров для условия.
   * @return true, если обновление прошло успешно; false в противном случае.
   */
  suspend fun update(
      table: String,
      data: Map<String, String>,
      condition: String,
      conditionParams: List<String>
  ): Boolean

  /**
   * Удаляет записи из указанной таблицы.
   *
   * @param table Имя таблицы.
   * @param condition Условие WHERE в виде строки с подстановочными знаками (?).
   * @param conditionParams Список параметров для условия.
   * @return true, если удаление прошло успешно; false в противном случае.
   */
  suspend fun delete(table: String, condition: String, conditionParams: List<String>): Boolean
}
