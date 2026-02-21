package com.example

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Very small SQLite helper for the mini-project.
 *
 * - Uses a local file `library.db` (created automatically)
 * - Creates tables: users, books, loans
 * - Inserts a bit of seed data if tables are empty
 */
object Database {
    private const val DB_URL = "jdbc:sqlite:library.db"

    fun init() {
        // Create tables + seed on startup
        withConnection { conn ->
            conn.createStatement().use { st ->
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        address TEXT,
                        password TEXT NOT NULL
                    );
                    """.trimIndent()
                )

                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS books (
                        book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        location TEXT NOT NULL
                    );
                    """.trimIndent()
                )

                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS loans (
                        loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        book_id INTEGER NOT NULL,
                        checkout_date TEXT NOT NULL,
                        return_date TEXT,
                        FOREIGN KEY(user_id) REFERENCES users(user_id),
                        FOREIGN KEY(book_id) REFERENCES books(book_id)
                    );
                    """.trimIndent()
                )
            }

            seedIfEmpty(conn)
        }
    }

    private fun seedIfEmpty(conn: Connection) {
        // Users
        val userCount = conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) AS c FROM users").use { rs ->
                rs.next(); rs.getInt("c")
            }
        }
        if (userCount == 0) {
            conn.prepareStatement(
                "INSERT INTO users(name,email,address,password) VALUES(?,?,?,?)"
            ).use { ps ->
                ps.setString(1, "Demo User")
                ps.setString(2, "demo@leeds.ac.uk")
                ps.setString(3, "Leeds")
                ps.setString(4, "password")
                ps.executeUpdate()
            }
        }

        // Books
        val bookCount = conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) AS c FROM books").use { rs ->
                rs.next(); rs.getInt("c")
            }
        }
        if (bookCount == 0) {
            val seed = listOf(
                "Clean Code" to "Shelf A1",
                "Design Patterns" to "Shelf B2",
                "Operating Systems" to "Shelf C3",
                "Algorithms" to "Shelf D4",
                "Database Systems" to "Shelf E5",
                "Kotlin in Action" to "Shelf F6"
            )
            conn.prepareStatement("INSERT INTO books(title,location) VALUES(?,?)").use { ps ->
                for ((title, location) in seed) {
                    ps.setString(1, title)
                    ps.setString(2, location)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
        }
    }

    fun authenticate(email: String, password: String): User? {
        return withConnection { conn ->
            conn.prepareStatement(
                "SELECT user_id,name,email,address FROM users WHERE email=? AND password=?"
            ).use { ps ->
                ps.setString(1, email)
                ps.setString(2, password)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return@withConnection null
                    User(
                        id = rs.getInt("user_id"),
                        name = rs.getString("name"),
                        email = rs.getString("email"),
                        address = rs.getString("address")
                    )
                }
            }
        }
    }

    fun getUserById(userId: Int): User? {
        return withConnection { conn ->
            conn.prepareStatement(
                "SELECT user_id,name,email,address FROM users WHERE user_id=?"
            ).use { ps ->
                ps.setInt(1, userId)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return@withConnection null
                    User(
                        id = rs.getInt("user_id"),
                        name = rs.getString("name"),
                        email = rs.getString("email"),
                        address = rs.getString("address")
                    )
                }
            }
        }
    }

    fun listBooks(limit: Int = 12): List<Book> {
        return withConnection { conn ->
            conn.prepareStatement(
                "SELECT book_id,title,location FROM books ORDER BY book_id ASC LIMIT ?"
            ).use { ps ->
                ps.setInt(1, limit)
                ps.executeQuery().use { rs ->
                    rs.toList { r ->
                        Book(
                            id = r.getInt("book_id"),
                            title = r.getString("title"),
                            location = r.getString("location")
                        )
                    }
                }
            }
        }
    }

    fun getBook(bookId: Int): Book? {
        return withConnection { conn ->
            conn.prepareStatement(
                "SELECT book_id,title,location FROM books WHERE book_id=?"
            ).use { ps ->
                ps.setInt(1, bookId)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return@withConnection null
                    Book(
                        id = rs.getInt("book_id"),
                        title = rs.getString("title"),
                        location = rs.getString("location")
                    )
                }
            }
        }
    }

    fun createLoan(userId: Int, bookId: Int, checkoutDate: String, returnDate: String?): Int {
        return withConnection { conn ->
            conn.prepareStatement(
                "INSERT INTO loans(user_id,book_id,checkout_date,return_date) VALUES(?,?,?,?)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { ps ->
                ps.setInt(1, userId)
                ps.setInt(2, bookId)
                ps.setString(3, checkoutDate)
                ps.setString(4, returnDate)
                ps.executeUpdate()
                ps.generatedKeys.use { keys ->
                    if (keys.next()) keys.getInt(1) else 0
                }
            }
        }
    }

    fun listLoansForUser(userId: Int): List<Loan> {
        return withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT l.loan_id, l.checkout_date, l.return_date,
                       b.book_id, b.title, b.location
                FROM loans l
                JOIN books b ON b.book_id = l.book_id
                WHERE l.user_id=?
                ORDER BY l.loan_id DESC
                """.trimIndent()
            ).use { ps ->
                ps.setInt(1, userId)
                ps.executeQuery().use { rs ->
                    rs.toList { r ->
                        Loan(
                            id = r.getInt("loan_id"),
                            checkoutDate = r.getString("checkout_date"),
                            returnDate = r.getString("return_date"),
                            book = Book(
                                id = r.getInt("book_id"),
                                title = r.getString("title"),
                                location = r.getString("location")
                            )
                        )
                    }
                }
            }
        }
    }

    private inline fun <T> withConnection(block: (Connection) -> T): T {
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.autoCommit = true
            return block(conn)
        }
    }

    private inline fun <T> ResultSet.toList(mapper: (ResultSet) -> T): List<T> {
        val out = mutableListOf<T>()
        while (this.next()) out += mapper(this)
        return out
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val address: String?
)

data class Book(
    val id: Int,
    val title: String,
    val location: String
)

data class Loan(
    val id: Int,
    val checkoutDate: String,
    val returnDate: String?,
    val book: Book
)
