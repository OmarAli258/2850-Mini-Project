plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("io.ktor.plugin") version "2.3.12"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor core
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")

    // Routing + HTML
    implementation("io.ktor:ktor-server-html-builder-jvm:2.3.12")

    // Pebble templates
    implementation("io.ktor:ktor-server-pebble-jvm:2.3.12")

    // Sessions
    implementation("io.ktor:ktor-server-sessions-jvm:2.3.12")

    // Logging
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.12")

    // Exposed (DB)
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")

    // H2 database (simple local DB file)
    implementation("com.h2database:h2:2.2.224")

    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.12")
    testImplementation(kotlin("test"))
}