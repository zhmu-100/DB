package com.db.orm.connection

import java.io.File
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test

class EnvLoaderTest {

  @Test
  fun `test get returns null for undefined key when env is absent`() {
    assumeFalse(File(".env").exists(), "Env exists, test skipped")
    val key = "NON_EXISTENT_KEY_987654321"
    val value = EnvLoader.get(key)
    assertNull(value, "If there is no key null is expected")
  }
}
