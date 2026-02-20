package com.example

import io.ktor.server.application.*
import io.ktor.server.sessions.*

data class UserSession(
    val userId: Int,
    val username: String
)
fun Application.configureSessions() {
    install(Sessions) {
        cookie<UserSession>("SESSION") {
            cookie.path = "/"
            cookie.httpOnly = true
        }
    }
}