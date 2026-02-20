package com.example

import io.ktor.server.application.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object BooksTable : IntIdTable("books") {
    val title = varchar("title", 255)
    val author = varchar("author", 255)
    val summary = text("summary")
}

data class Book(val id: Int, val title: String, val author: String, val summary: String)

fun Application.configureDatabase() {
    Database.connect("jdbc:h2:file:./build/website-db;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(BooksTable)

        if (BooksTable.selectAll().empty()) {
            seedBooks()
        }
    }
}

fun getAllBooks(): List<Book> = transaction {
    BooksTable.selectAll().map(::toBook)
}

fun getBookById(id: Int): Book? = transaction {
    BooksTable.selectAll().where { BooksTable.id eq id }.map(::toBook).singleOrNull()
}

private fun toBook(row: ResultRow): Book = Book(
    id = row[BooksTable.id].value,
    title = row[BooksTable.title],
    author = row[BooksTable.author],
    summary = row[BooksTable.summary],
)

private fun seedBooks() {
    listOf(
        Triple("Atomic Habits", "James Clear", "Practical system for building good habits and breaking bad ones."),
        Triple("Clean Code", "Robert C. Martin", "Guidelines for writing readable, maintainable software."),
        Triple("Deep Work", "Cal Newport", "Strategies for focused work and better productivity in a distracted world."),
    ).forEach { (title, author, summary) ->
        BooksTable.insert {
            it[BooksTable.title] = title
            it[BooksTable.author] = author
            it[BooksTable.summary] = summary
        }
    }
}
