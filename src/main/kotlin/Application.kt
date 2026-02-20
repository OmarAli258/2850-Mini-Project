package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.Level

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
    }

    configureTemplates()
    configureSessions()
    configureDatabase()
    configureRouting()
}
