package com.example

import io.ktor.server.application.*
import io.ktor.server.pebble.*

fun Application.configureTemplates() {
    install(Pebble) {
        // looks for templates in: src/main/resources/templates
    }
}