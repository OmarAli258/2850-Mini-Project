package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.time.LocalDate

data class UserSession(val userId: Int)

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static")

        get("/") {
            val books = Database.listBooks(50)
            val userId = call.sessions.get<UserSession>()?.userId ?: 0

            call.respond(
                PebbleContent(
                    "home",
                    mapOf(
                        "title" to "Library System",
                        "libraryName" to "COMP2850 Library",
                        "books" to books,
                        "userId" to userId
                    )
                )
            )
        }

        // ---------- AUTH ----------
        get("/login") {
            call.respond(PebbleContent("login", mapOf("title" to "Login")))
        }

        post("/login") {
            val form = call.receiveParameters()
            val email = form["email"]?.trim().orEmpty()
            val password = form["password"].orEmpty()

            val user = Database.authenticate(email, password)
            if (user == null) {
                call.respond(
                    PebbleContent(
                        "login",
                        mapOf(
                            "title" to "Login",
                            "error" to "Invalid email or password"
                        )
                    )
                )
                return@post
            }

            call.sessions.set(UserSession(user.id))
            call.respondRedirect("/")
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }

        // ---------- SIGNUP ----------
        get("/signup") {
            call.respond(PebbleContent("signup", mapOf("title" to "Sign Up")))
        }

        post("/signup") {
            val form = call.receiveParameters()
            val name = form["name"]?.trim().orEmpty()
            val email = form["email"]?.trim().orEmpty()
            val address = form["address"]?.trim()
            val password = form["password"].orEmpty()

            val newUserId = Database.createUser(name, email, address, password)

            if (newUserId == null) {
                call.respond(
                    PebbleContent(
                        "signup",
                        mapOf(
                            "title" to "Sign Up",
                            "error" to "That email is already in use"
                        )
                    )
                )
                return@post
            }

            call.respondRedirect("/login")
        }

        // ---------- BOOK DETAILS ----------
        get("/book/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid book id")
                return@get
            }

            val book = Database.getBook(id)
            if (book == null) {
                call.respond(HttpStatusCode.NotFound, "Book not found")
                return@get
            }

            val userId = call.sessions.get<UserSession>()?.userId ?: 0

            call.respond(
                PebbleContent(
                    "book",
                    mapOf(
                        "title" to book.title,
                        "book" to book,
                        "userId" to userId
                    )
                )
            )
        }

        // ---------- LOAN BOOK ----------
        post("/book/{id}/loan") {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                call.respondRedirect("/login")
                return@post
            }

            val bookId = call.parameters["id"]?.toIntOrNull()
            if (bookId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid book id")
                return@post
            }

            val checkoutDate = LocalDate.now().toString()
            val dueDate = LocalDate.now().plusDays(14).toString()

            Database.createLoan(session.userId, bookId, checkoutDate, dueDate)

            call.respondRedirect("/loan_confirm")
        }

        // ---------- LOANS ----------
        get("/loans") {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                call.respondRedirect("/login")
                return@get
            }

            val loans = Database.listLoansForUser(session.userId)

            call.respond(
                PebbleContent(
                    "loans",
                    mapOf(
                        "title" to "My Loans",
                        "libraryName" to "COMP2850 Library",
                        "loans" to loans,
                        "userId" to session.userId
                    )
                )
            )
        }

        // ---------- CONFIRM ----------
        get("/loan_confirm") {
            val userId = call.sessions.get<UserSession>()?.userId ?: 0

            call.respond(
                PebbleContent(
                    "loan_confirm",
                    mapOf(
                        "title" to "Loan Confirmed",
                        "userId" to userId
                    )
                )
            )
        }
    }
}