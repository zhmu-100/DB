package com.db.orm.api

import com.db.orm.crud.IORMService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiServerTest {

  private val testJson = Json { prettyPrint = true }

  // Dummy-реализация IORMService для тестирования
  private val dummyService =
      object : IORMService {
        override fun create(table: String, data: Map<String, String>): Boolean = true

        override fun read(query: String, params: List<String>): List<Map<String, Any>> =
            listOf(mapOf("name" to "John Doe", "email" to "john.doe@example.com"))

        override fun update(
            table: String,
            data: Map<String, String>,
            condition: String,
            conditionParams: List<String>
        ): Boolean = true

        override fun delete(
            table: String,
            condition: String,
            conditionParams: List<String>
        ): Boolean = true
      }

  private fun Application.testModule() {
    install(ContentNegotiation) { json(testJson) }
    apiModule(dummyService)
  }

  @Test
  fun testCreateEndpoint() = testApplication {
    application { testModule() }
    val response =
        client.post("/create") {
          contentType(ContentType.Application.Json)
          setBody(
              """
                {
                  "table": "users",
                  "data": {
                    "name": "Alex",
                    "email": "aboba@mail.ru"
                  }
                }
                """
                  .trimIndent())
        }
    assertEquals(HttpStatusCode.OK, response.status)
    val body = response.bodyAsText()
    assertTrue(body.contains("true"), "Response body should contain 'true'")
  }

  @Test
  fun testUpdateEndpoint() = testApplication {
    application { testModule() }
    val response =
        client.put("/update") {
          contentType(ContentType.Application.Json)
          setBody(
              """
                {
                  "table": "users",
                  "data": {
                    "email": "new@test.com"
                  },
                  "condition": "name = ?",
                  "conditionParams": ["Test"]
                }
                """
                  .trimIndent())
        }
    assertEquals(HttpStatusCode.OK, response.status)
    val body = response.bodyAsText()
    assertTrue(body.contains("true"), "Response body should contain 'true'")
  }

  @Test
  fun testDeleteEndpoint() = testApplication {
    application { testModule() }
    val response =
        client.delete("/delete") {
          contentType(ContentType.Application.Json)
          setBody(
              """
                {
                  "table": "users",
                  "condition": "name = ?",
                  "conditionParams": ["Test"]
                }
                """
                  .trimIndent())
        }
    assertEquals(HttpStatusCode.OK, response.status)
    val body = response.bodyAsText()
    assertTrue(body.contains("true"), "Response body should contain 'true'")
  }
}
