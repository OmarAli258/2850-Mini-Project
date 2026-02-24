package com.example

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

data class User(val id: Int, val name: String, val email: String)
data class Book(val id: Int, val title: String, val shelf: String, val description: String)
data class Loan(val id: Int, val title: String, val checkoutDate: String, val dueDate: String)

object Database {
    private const val JDBC_URL = "jdbc:sqlite:library.db"

    fun init() {
        withConnection { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        address TEXT,
                        password TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                stmt.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS books (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        shelf TEXT NOT NULL,
                        description TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                stmt.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS loans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        book_id INTEGER NOT NULL,
                        checkout_date TEXT NOT NULL,
                        due_date TEXT NOT NULL,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(book_id) REFERENCES books(id)
                    )
                    """.trimIndent()
                )
            }

            seedBooks(conn)
        }
    }

    private fun seedBooks(conn: Connection) {
        val count = conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM books").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        if (count > 0) return

        val books = listOf(
            Triple("Clean Code", "Shelf A1", "A popular library book."),
            Triple("Design Patterns", "Shelf B2", "A popular library book."),
            Triple("Operating Systems", "Shelf C3", "A popular library book."),
            Triple("Algorithms", "Shelf D4", "A popular library book."),
            Triple("Database Systems", "Shelf E5", "A popular library book."),
            Triple("Kotlin in Action", "Shelf F6", "A popular library book.")
        )

        conn.prepareStatement("INSERT INTO books(title, shelf, description) VALUES (?,?,?)").use { ps ->
            for ((title, shelf, desc) in books) {
                ps.setString(1, title)
                ps.setString(2, shelf)
                ps.setString(3, desc)
                ps.executeUpdate()
            }
        }
    }

    private fun <T> withConnection(block: (Connection) -> T): T {
        DriverManager.getConnection(JDBC_URL).use { conn ->
            return block(conn)
        }
    }

    fun createUser(name: String, email: String, address: String?, password: String): Int? {
        return withConnection { conn ->
            try {
                conn.prepareStatement(
                    "INSERT INTO users(name,email,address,password) VALUES(?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { ps ->
                    ps.setString(1, name)
                    ps.setString(2, email)
                    ps.setString(3, address)
                    ps.setString(4, password)
                    ps.executeUpdate()

                    ps.generatedKeys.use { keys ->
                        if (keys.next()) keys.getInt(1) else null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun authenticate(email: String, password: String): User? {
        return withConnection { conn ->
            conn.prepareStatement(
                "SELECT id,name,email FROM users WHERE email=? AND password=?"
            ).use { ps ->
                ps.setString(1, email)
                ps.setString(2, password)

                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email")
                        )
                    } else null
                }
            }
        }
    }

    fun listBooks(limit: Int): List<Book> {
        return withConnection { conn ->
            conn.prepareStatement("SELECT * FROM books LIMIT ?").use { ps ->
                ps.setInt(1, limit)
                ps.executeQuery().use { rs ->
                    val books = mutableListOf<Book>()
                    while (rs.next()) {
                        books.add(
                            Book(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("shelf"),
                                rs.getString("description")
                            )
                        )
                    }
                    books
                }
            }
        }
    }

    fun getBook(id: Int): Book? {
        return withConnection { conn ->
            conn.prepareStatement("SELECT * FROM books WHERE id=?").use { ps ->
                ps.setInt(1, id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("shelf"),
                            rs.getString("description")
                        )
                    } else null
                }
            }
        }
    }

    fun createLoan(userId: Int, bookId: Int, checkoutDate: String, dueDate: String) {
        withConnection { conn ->
            conn.prepareStatement(
                "INSERT INTO loans(user_id,book_id,checkout_date,due_date) VALUES(?,?,?,?)"
            ).use { ps ->
                ps.setInt(1, userId)
                ps.setInt(2, bookId)
                ps.setString(3, checkoutDate)
                ps.setString(4, dueDate)
                ps.executeUpdate()
            }
        }
    }

    fun listLoansForUser(userId: Int): List<Loan> {
        return withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT loans.id, books.title, checkout_date, due_date
                FROM loans
                JOIN books ON loans.book_id = books.id
                WHERE user_id = ?
                """.trimIndent()
            ).use { ps ->
                ps.setInt(1, userId)
                ps.executeQuery().use { rs ->
                    val loans = mutableListOf<Loan>()
                    while (rs.next()) {
                        loans.add(
                            Loan(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("checkout_date"),
                                rs.getString("due_date")
                            )
                        )
                    }
                    loans
                }
            }
        }
    }
}