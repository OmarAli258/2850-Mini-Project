package com.example

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.util.date.GMTDate
import java.time.LocalDate

fun Application.configureRouting() {
    routing {

        // Serve /static/style.css from src/main/resources/static/style.css
        staticResources("/static", "static")

        fun currentUser(call: ApplicationCall): User? {
            val raw = call.request.cookies["userId"] ?: return null
            val id = raw.toIntOrNull() ?: return null
            return Database.getUserById(id)
        }

        // HOME
        get("/") {
            val user = currentUser(call)
            val books = Database.listBooks(limit = 9)
            val popular = books.take(3)
            val coming = books.drop(3).take(3)
            val recent = books.drop(6).take(3)

            call.respond(
                PebbleContent(
                    "home.peb",
                    mapOf<String, Any>(
                        "title" to "Home",
                        "user" to (user ?: ""),
                        "popular" to popular,
                        "coming" to coming,
                        "recent" to recent,
                        "libraryName" to "Library"
                    )
                )
            )
        }

        // LOGIN PAGE
        get("/login") {
            call.respond(
                PebbleContent(
                    "login.peb",
                    mapOf<String, Any>(
                        "title" to "Login",
                        "libraryName" to "Library"
                    )
                )
            )
        }

        // LOGIN SUBMIT
        post("/login") {
            val params = call.receiveParameters()
            val email = (params["email"] ?: "").trim()
            val password = (params["password"] ?: "").trim()

            if (email.isBlank() || password.isBlank()) {
                call.respond(
                    PebbleContent(
                        "login.peb",
                        mapOf<String, Any>(
                            "title" to "Login",
                            "libraryName" to "Library",
                            "error" to "Please enter email and password"
                        )
                    )
                )
                return@post
            }

            val user = Database.authenticate(email, password)
            if (user == null) {
                call.respond(
                    PebbleContent(
                        "login.peb",
                        mapOf<String, Any>(
                            "title" to "Login",
                            "libraryName" to "Library",
                            "error" to "Wrong email or password (try demo@leeds.ac.uk / password)"
                        )
                    )
                )
                return@post
            }

            call.response.cookies.append(
                Cookie(
                    name = "userId",
                    value = user.id.toString(),
                    path = "/",
                    httpOnly = true
                )
            )
            call.respondRedirect("/")
        }

        get("/logout") {
            call.response.cookies.append(
                Cookie(
                    name = "userId",
                    value = "",
                    path = "/",
                    expires = GMTDate.START
                )
            )
            call.respondRedirect("/")
        }

        // BOOKS (keep route for compatibility)
        get("/books") { call.respondRedirect("/") }

        // BOOK DETAILS
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

            call.respond(
                PebbleContent(
                    "book.peb",
                    mapOf<String, Any>(
                        "title" to "Book Details",
                        "user" to (currentUser(call) ?: ""),
                        "book" to book,
                        "libraryName" to "Library"
                    )
                )
            )
        }

        // LOAN CONFIRM
        get("/loan/confirm") {
            val user = currentUser(call)
            if (user == null) {
                call.respondRedirect("/login")
                return@get
            }

            val bookId = call.request.queryParameters["bookId"]?.toIntOrNull()
            if (bookId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing bookId")
                return@get
            }
            val book = Database.getBook(bookId)
            if (book == null) {
                call.respond(HttpStatusCode.NotFound, "Book not found")
                return@get
            }

            val checkout = LocalDate.now().toString()
            val due = LocalDate.now().plusDays(14).toString()
            val loanId = Database.createLoan(user.id, bookId, checkout, due)

            call.respond(
                PebbleContent(
                    "loan_confirm.peb",
                    mapOf<String, Any>(
                        "title" to "Loan Confirmed",
                        "user" to user,
                        "loanId" to loanId,
                        "book" to book,
                        "checkout" to checkout,
                        "due" to due,
                        "libraryName" to "Library"
                    )
                )
            )
        }

        // MY LOANS
        get("/loans") {
            val user = currentUser(call)
            if (user == null) {
                call.respondRedirect("/login")
                return@get
            }
            val loans = Database.listLoansForUser(user.id)
            call.respond(
                PebbleContent(
                    "loans.peb",
                    mapOf<String, Any>(
                        "title" to "My Loans",
                        "user" to user,
                        "loans" to loans,
                        "libraryName" to "Library"
                    )
                )
            )
        }
    }
}