package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.pebble.*
import io.ktor.server.sessions.*

data class LoginRequest(val username: String)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(PebbleContent("home.peb", mapOf(
                "title" to "Home",
                "user" to call.sessions.get<UserSession>()?.username,
            )))
        }

        get("/books") {
            call.respond(PebbleContent("books.peb", mapOf(
                "title" to "Books",
                "books" to getAllBooks(),
                "user" to call.sessions.get<UserSession>()?.username,
            )))
        }

        get("/books/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val book = id?.let(::getBookById)

            if (book == null) {
                call.respond(HttpStatusCode.NotFound, "Book not found")
                return@get
            }

            call.respond(PebbleContent("book.peb", mapOf(
                "title" to book.title,
                "book" to book,
                "user" to call.sessions.get<UserSession>()?.username,
            )))
        }

        get("/login") {
            call.respond(PebbleContent("login.peb", mapOf("title" to "Login")))
        }

        post("/login") {
            val params = call.receiveParameters()
            val username = params["username"].orEmpty().trim()

            if (username.isBlank()) {
                call.respond(PebbleContent("login.peb", mapOf(
                    "title" to "Login",
                    "error" to "Username is required.",
                )))
                return@post
            }

            call.sessions.set(UserSession(1, username))
            call.respondRedirect("/")
        }

        post("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }
    }
}
