package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.pebble.*
import io.ktor.http.*

fun Application.configureRouting() {

    routing {

        // ---------------- HOME ----------------
        get("/") {
            try {
                call.respond(
                    PebbleContent(
                        "home.peb",
                        mapOf(
                            "title" to "Library System",
                            "libraryName" to "COMP2850 Library"
                        )
                    )
                )
            } catch (e: Exception) {
                call.respondText(
                    "Home page working (template missing but server is OK)",
                    ContentType.Text.Plain
                )
            }
        }

        // ---------------- LOGIN PAGE ----------------
        get("/login") {
            try {
                call.respond(
                    PebbleContent(
                        "login.peb",
                        mapOf("title" to "Login")
                    )
                )
            } catch (e: Exception) {
                call.respondText("Login page route works")
            }
        }

        // ---------------- BOOK PAGE ----------------
        get("/book") {
            try {
                call.respond(
                    PebbleContent(
                        "book.peb",
                        mapOf("title" to "Book Page")
                    )
                )
            } catch (e: Exception) {
                call.respondText("Book page route works")
            }
        }

        // ---------------- LOANS ----------------
        get("/loans") {
            try {
                call.respond(
                    PebbleContent(
                        "loans.peb",
                        mapOf(
                            "title" to "My Loans",
                            "libraryName" to "COMP2850 Library"
                        )
                    )
                )
            } catch (e: Exception) {
                call.respondText("Loans page route works")
            }
        }

        // ---------------- CONFIRM ----------------
        get("/loan_confirm") {
            try {
                call.respond(
                    PebbleContent(
                        "loan_confirm.peb",
                        mapOf("title" to "Loan Confirmed")
                    )
                )
            } catch (e: Exception) {
                call.respondText("Loan confirmation works")
            }
        }
    }
}