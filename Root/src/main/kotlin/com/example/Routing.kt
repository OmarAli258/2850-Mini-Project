package com.example

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        // Serve /static/style.css from src/main/resources/static/style.css
        staticResources("/static", "static")

        // HOME
        get("/") {
            call.respond(PebbleContent("home.peb", mapOf("title" to "Home")))
        }

        // LOGIN PAGE
        get("/login") {
            call.respond(
                PebbleContent(
                    "login.peb",
                    mapOf(
                        "title" to "Login",
                        "error" to ""
                    )
                )
            )
        }

        // LOGIN SUBMIT (simple test login)
        post("/login") {
            val params = call.receiveParameters()
            val username = (params["username"] ?: "").trim()
            val password = (params["password"] ?: "").trim()

            if (username.isBlank() || password.isBlank()) {
                call.respond(
                    PebbleContent(
                        "login.peb",
                        mapOf(
                            "title" to "Login",
                            "error" to "Please enter username and password"
                        )
                    )
                )
            } else {
                // No real auth yet — just redirect to books/home
                call.respondRedirect("/books")
            }
        }

        // BOOKS (we just show home for now, wireframe style)
        get("/books") {
            call.respond(PebbleContent("home.peb", mapOf("title" to "Home")))
        }

        // BOOK DETAILS
        get("/book/{id}") {
            val id = call.parameters["id"] ?: "0"
            call.respond(
                PebbleContent(
                    "book.peb",
                    mapOf(
                        "title" to "Book Details",
                        "bookId" to id
                    )
                )
            )
        }

        // LOAN CONFIRM
        get("/loan/confirm") {
            call.respond(PebbleContent("loan_confirm.peb", mapOf("title" to "Loan Confirmed")))
        }

        // MY LOANS
        get("/loans") {
            call.respond(PebbleContent("loans.peb", mapOf("title" to "My Loans")))
        }
    }
}

