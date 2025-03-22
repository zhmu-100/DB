package com.db.orm.api

import com.db.orm.connection.DatabaseConnection
import com.db.orm.crud.IORMService
import com.db.orm.crud.ORMService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Точка входа в приложение ORM API.
 *
 * Данный модуль запускает Ktor-сервер на порту 8080 и регистрирует маршруты API, реализующие CRUD
 * операции для работы с базой данных посредством [IORMService].
 *
 * Для работы используется реализация [ORMService], которая обращается к [DatabaseConnection] для
 * управления соединениями.
 */
fun main() {
  embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json(Json { prettyPrint = true }) }
        environment.monitor.subscribe(ApplicationStopped) { DatabaseConnection.close() }
        apiModule(ORMService())
      }
      .start(wait = true)
}

/**
 * Регистрирует маршруты REST API для CRUD операций.
 *
 * @param ormService Экземпляр сервиса, реализующего [IORMService].
 *
 * Определяются следующие эндпоинты:
 * - POST /create – создание новой записи.
 * - POST /read – выполнение SELECT запроса.
 * - PUT /update – обновление записей.
 * - DELETE /delete – удаление записей.
 */
fun Application.apiModule(ormService: IORMService) {
  routing {
    post("/create") {
      val request = call.receive<CreateRequest>()
      val success = ormService.create(request.table, request.data)
      call.respond(mapOf("success" to success))
    }

    post("/read") {
      val request = call.receive<ReadRequest>()
      val results: List<Map<String, Any?>> =
          ormService.read(request.table, request.columns, request.filters)
      val jsonResults =
          results.map { row ->
            JsonObject(
                row.mapValues { (_, value) ->
                  when (value) {
                    null -> JsonNull
                    is Number -> JsonPrimitive(value)
                    is Boolean -> JsonPrimitive(value)
                    else -> JsonPrimitive(value.toString())
                  }
                })
          }
      call.respond(jsonResults)
    }

    put("/update") {
      val request = call.receive<UpdateRequest>()
      val success =
          ormService.update(request.table, request.data, request.condition, request.conditionParams)
      call.respond(mapOf("success" to success))
    }

    delete("/delete") {
      val request = call.receive<DeleteRequest>()
      val success = ormService.delete(request.table, request.condition, request.conditionParams)
      call.respond(mapOf("success" to success))
    }
  }
}

/**
 * Модель запроса для создания записи.
 *
 * @property table Имя таблицы.
 * @property data Пара ключ-значение с данными для вставки.
 */
@Serializable data class CreateRequest(val table: String, val data: Map<String, String>)

/**
 * Модель запроса для выполнения SELECT запроса.
 *
 * @property table Имя таблицы.
 * @property columns Список колонок для выборки (по умолчанию все, если не указан).
 * @property filters Карта фильтров (ключ-значение), используемых в условии WHERE.
 */
@Serializable
data class ReadRequest(
    val table: String,
    val columns: List<String> = listOf("*"),
    val filters: Map<String, String> = emptyMap()
)

/**
 * Модель запроса для обновления записей.
 *
 * @property table Имя таблицы.
 * @property data Пара ключ-значение с новыми данными.
 * @property condition Условие WHERE в виде строки с подстановочными знаками (?).
 * @property conditionParams Список параметров для условия.
 */
@Serializable
data class UpdateRequest(
    val table: String,
    val data: Map<String, String>,
    val condition: String,
    val conditionParams: List<String>
)

/**
 * Модель запроса для удаления записей.
 *
 * @property table Имя таблицы.
 * @property condition Условие WHERE в виде строки с подстановочными знаками (?).
 * @property conditionParams Список параметров для условия.
 */
@Serializable
data class DeleteRequest(
    val table: String,
    val condition: String,
    val conditionParams: List<String>
)
