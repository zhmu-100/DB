plugins {
    kotlin("jvm") version "1.8.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "com.db.orm"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.postgresql:postgresql:42.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.db.orm.MainKt")
}
