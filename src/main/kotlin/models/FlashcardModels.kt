package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime

// ---------- Modelos de Serialização (DTOs) ----------
@Serializable
sealed class Flashcard {
    abstract val id: Int?
    abstract val userId: Int
    abstract val subjectId: Int
    abstract val type: String
}

@Serializable
data class BasicFlashcard(
    override val id: Int? = null,
    override val userId: Int,
    override val subjectId: Int,
    val question: String,
    val answer: String,
    override val type: String = "basic"
) : Flashcard()

@Serializable
data class ClozeFlashcard(
    override val id: Int? = null,
    override val userId: Int,
    override val subjectId: Int,
    val fullText: String,
    val hiddenPart: String,
    override val type: String = "cloze"
) : Flashcard()

@Serializable
data class QuizFlashcard(
    override val id: Int? = null,
    override val userId: Int,
    override val subjectId: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    override val type: String = "quiz"
) : Flashcard() {
    init {
        require(options.size >= 2) { "O quiz precisa de pelo menos 2 opções" }
        require(correctIndex in options.indices) { "Índice correto inválido" }
    }
}

// ---------- Tabelas do Banco de Dados ----------
object FlashcardsTable : IntIdTable("flashcards") {
    val userId = integer("user_id")
    val subjectId = integer("subject_id")
    val type = varchar("type", length = 50)
    val data = text("data")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object SubjecttsTable : IntIdTable("subjects") { // Nome corrigido (apenas um "t")
    val name = varchar("name", length = 100).uniqueIndex() // Sintaxe correta
}