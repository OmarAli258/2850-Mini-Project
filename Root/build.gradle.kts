plugins {
    kotlin("jvm") version "2.2.20"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.example.ApplicationKt")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
    implementation("io.ktor:ktor-server-pebble-jvm:2.3.12")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.12")

    // Simple embedded DB for the mini-project
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(
        org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    )
}