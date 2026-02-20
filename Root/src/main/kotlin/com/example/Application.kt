package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(Pebble)

        routing {
            get("/") {
                call.respond(
                    PebbleContent(
                        "home.peb",
                        mapOf("title" to "Library Home")
                    )
                )
            }
        }

    }.start(wait = true)
}   