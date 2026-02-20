package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.pebble.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(PebbleContent("home.peb", mapOf("title" to "Home")))
        }
    }
}