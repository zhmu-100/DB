plugins {
  kotlin("jvm") version "1.8.0"
  kotlin("plugin.serialization") version "1.8.10"
  id("io.ktor.plugin") version "2.2.4"
  application
  id("com.ncorti.ktfmt.gradle") version "0.11.0"
  jacoco
}

group = "com.db.orm"

version = "0.1.0"

repositories { mavenCentral() }

dependencies {
  implementation(kotlin("stdlib"))

  implementation("io.ktor:ktor-server-core-jvm:2.2.4")
  implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
  implementation("ch.qos.logback:logback-classic:1.2.11")

  implementation("io.ktor:ktor-server-content-negotiation-jvm:2.2.4")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.4")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

  implementation("org.postgresql:postgresql:42.5.4")

  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("io.ktor:ktor-server-tests-jvm:2.2.4")
  testImplementation("io.mockk:mockk:1.13.5")
}

tasks.test { useJUnitPlatform() }

configure<JacocoPluginExtension> { toolVersion = "0.8.8" }

tasks.named<JacocoReport>("jacocoTestReport") {
  reports {
    html.required.set(true)
    xml.required.set(false)
    csv.required.set(false)
  }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
  violationRules {
    rule {
      limit {
        minimum = 0.50.toBigDecimal()
        counter = "LINE"
      }
    }
  }
}

tasks.check { dependsOn(tasks.named("jacocoTestCoverageVerification")) }

application { mainClass.set("com.db.orm.api.ApiServerKt") }
