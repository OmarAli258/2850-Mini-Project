package com.example

import io.ktor.server.application.*
import io.ktor.server.pebble.*

fun Application.configureTemplates() {
    install(Pebble)
}
