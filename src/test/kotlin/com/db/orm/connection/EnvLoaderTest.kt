package com.db.orm.connection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import java.io.File

class EnvLoaderTest {

    @Test
    fun `test get returns null for undefined key when env is absent`() {
        // Предполагаем, что в рабочей директории отсутствует файл .env
        assumeFalse(File(".env").exists(), "Файл .env существует, тест пропущен")
        val key = "NON_EXISTENT_KEY_987654321"
        val value = EnvLoader.get(key)
        assertNull(value, "Для несуществующего ключа ожидается null")
    }
}
