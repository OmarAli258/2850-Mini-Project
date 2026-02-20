package com.example

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 200) // practice only
    val isAdmin = bool("is_admin").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Books : Table("books") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 200)
    val author = varchar("author", 200)
    val totalCopies = integer("total_copies").default(1)
    val availableCopies = integer("available_copies").default(1)
    override val primaryKey = PrimaryKey(id)
}

object Loans : Table("loans") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val bookId = integer("book_id").references(Books.id)
    val loanedAt = long("loaned_at")
    val returnedAt = long("returned_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

fun Application.configureDatabase() {
    Database.connect(
        url = "jdbc:h2:file:./data/librarydb;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    transaction {
        SchemaUtils.create(Users, Books, Loans)
        seedDataIfEmpty()
    }
}

private fun seedDataIfEmpty() {
    // Seed users
    val userCount = Users.selectAll().count()
    if (userCount == 0L) {
        Users.insert {
            it[username] = "admin"
            it[passwordHash] = "admin"   // practice only: plain text
            it[isAdmin] = true
        }
        Users.insert {
            it[username] = "user"
            it[passwordHash] = "user"    // practice only: plain text
            it[isAdmin] = false
        }
    }

    // Seed books
    val bookCount = Books.selectAll().count()
    if (bookCount == 0L) {
        Books.insert {
            it[title] = "Clean Code"
            it[author] = "Robert C. Martin"
            it[totalCopies] = 3
            it[availableCopies] = 3
        }
        Books.insert {
            it[title] = "Introduction to Algorithms"
            it[author] = "Cormen, Leiserson, Rivest, Stein"
            it[totalCopies] = 2
            it[availableCopies] = 2
        }
        Books.insert {
            it[title] = "Designing Data-Intensive Applications"
            it[author] = "Martin Kleppmann"
            it[totalCopies] = 1
            it[availableCopies] = 1
        }
    }
}