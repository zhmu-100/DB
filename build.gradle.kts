plugins {
  kotlin("jvm") version "1.8.0"
  kotlin("plugin.serialization") version "1.8.10"
  id("io.ktor.plugin") version "2.2.4"
  application
  id("com.ncorti.ktfmt.gradle") version "0.11.0"
  jacoco
  id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.db.orm"

version = "0.1.0"

repositories { mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation(kotlin("stdlib"))

  implementation("io.ktor:ktor-server-core:2.3.0")
  implementation("io.ktor:ktor-server-netty:2.3.0")
  implementation("io.ktor:ktor-server-content-negotiation:2.3.0")

  implementation("io.insert-koin:koin-core:3.3.0")
  implementation("io.insert-koin:koin-ktor:3.3.0")

  implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")

  implementation("io.ktor:ktor-server-core-jvm:2.2.4")
  implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
  implementation("ch.qos.logback:logback-classic:1.2.11")

  implementation("io.ktor:ktor-server-content-negotiation-jvm:2.2.4")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.4")
  implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

  implementation("com.github.poplopok:Logger:1.0.6")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

  implementation("org.postgresql:postgresql:42.5.4")

  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("ch.qos.logback:logback-classic:1.2.11")

  testImplementation("io.mockk:mockk:1.13.5") 
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
  testImplementation("io.ktor:ktor-server-tests:2.2.4")
  testImplementation("io.kotest:kotest-assertions-core:5.8.1")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) 
}

configure<JacocoPluginExtension> { toolVersion = "0.8.8" }

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "com/db/orm/api/**",
                    "com/db/orm/connection/DatabaseConnection.class",
                    "com/db/orm/query/SQLActivity.class",
                    "com/db/orm/crud/IORMSerivce.class",
                    "com/db/orm/query/SQLExecutor.class"
                )
            }
        })
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit {
                minimum = 0.5.toBigDecimal()
                counter = "LINE"
            }
        }
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "com/db/orm/api/**",
                    "com/db/orm/connection/DatabaseConnection.class",
                    "com/db/orm/query/SQLActivity.class",
                    "com/db/orm/crud/IORMSerivce.class",
                    "com/db/orm/query/SQLExecutor.class"
                )
            }
        })
    )
}


tasks.check { dependsOn(tasks.named("jacocoTestCoverageVerification")) }

application { mainClass.set("com.db.orm.api.ApiServerKt") }
