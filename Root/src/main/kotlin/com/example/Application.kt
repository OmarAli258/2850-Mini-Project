package com.example

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    Database.init()

    install(Sessions) {
        cookie<UserSession>("USER_SESSION")
    }

    configureTemplates()
    configureRouting()
}