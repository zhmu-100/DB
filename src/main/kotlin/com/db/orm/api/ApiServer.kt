package com.db.orm.api

import com.db.orm.connection.DatabaseConnection
import com.db.orm.crud.ORMService
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main() {
    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }
        environment.monitor.subscribe(ApplicationStopped) {
            DatabaseConnection.close()
        }
        routing {
            val ormService = ORMService()

            post("/create") {
                try {
                    val request = call.receive<CreateRequest>()
                    val success = ormService.create(request.table, request.data)
                    call.respond(mapOf("success" to success))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Error: ${e.localizedMessage}")
                }
            }

            post("/read") {
                try {
                    val request = call.receive<ReadRequest>()
                    val results = ormService.read(request.query, request.params)
                    call.respond(results)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Error: ${e.localizedMessage}")
                }
            }

            put("/update") {
                try {
                    val request = call.receive<UpdateRequest>()
                    val success = ormService.update(
                        request.table,
                        request.data,
                        request.condition,
                        request.conditionParams
                    )
                    call.respond(mapOf("success" to success))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Error: ${e.localizedMessage}")
                }
            }

            delete("/delete") {
                try {
                    val request = call.receive<DeleteRequest>()
                    val success = ormService.delete(
                        request.table,
                        request.condition,
                        request.conditionParams
                    )
                    call.respond(mapOf("success" to success))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Error: ${e.localizedMessage}")
                }
            }
        }
    }
    server.start(wait = true)
}

@Serializable
data class CreateRequest(
    val table: String,
    val data: Map<String, String>
)

@Serializable
data class ReadRequest(
    val query: String,
    val params: List<String> = emptyList()
)

@Serializable
data class UpdateRequest(
    val table: String,
    val data: Map<String, String>,
    val condition: String,
    val conditionParams: List<String>
)

@Serializable
data class DeleteRequest(
    val table: String,
    val condition: String,
    val conditionParams: List<String>
)
