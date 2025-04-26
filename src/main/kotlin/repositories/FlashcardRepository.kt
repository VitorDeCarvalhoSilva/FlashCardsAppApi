package com.example.repositories

import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class FlashcardRepository {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun save(flashcard: Flashcard, username: String): Int {
        return transaction {
            // Obter ID do usuário (CORREÇÃO DA SINTAXE)
            val userId = UsersTable
                .select { UsersTable.username eq username } // Corrigido: "eq" em vez de "&&"
                .firstOrNull()?.get(UsersTable.id)?.value // Corrigido a formatação
                ?: throw IllegalArgumentException("Usuário não encontrado")

            // Inserir flashcard
            FlashcardsTable.insert {
                it[FlashcardsTable.userId] = userId
                it[FlashcardsTable.subjectId] = flashcard.subjectId
                it[FlashcardsTable.type] = flashcard.type
                it[FlashcardsTable.data] = serializeFlashcard(flashcard)
            }[FlashcardsTable.id].value
        }
    }

    suspend fun getByType(username: String, type: String): List<Flashcard> {
        return transaction {
            // Obter ID do usuário
            val userId = UsersTable
                .select { UsersTable.username eq username }
                .firstOrNull()?.get(UsersTable.id)?.value
                ?: throw IllegalArgumentException("Usuário não encontrado")

            // Buscar flashcards
            FlashcardsTable
                .select {
                    (FlashcardsTable.userId eq userId) and
                            (FlashcardsTable.type eq type)
                }
                .map { row ->
                    deserializeFlashcard(
                        jsonData = row[FlashcardsTable.data],
                        type = type
                    )
                }
        }
    }

    // FUNÇÃO QUE ESTAVA FALTANDO (ADICIONADA)
    private fun serializeFlashcard(flashcard: Flashcard): String {
        return when (flashcard) {
            is BasicFlashcard -> json.encodeToString(flashcard)
            is ClozeFlashcard -> json.encodeToString(flashcard)
            is QuizFlashcard -> json.encodeToString(flashcard)
            else -> throw IllegalArgumentException("Tipo não suportado")
        }
    }

    // FUNÇÃO AUXILIAR PARA DESSERIALIZAÇÃO
    private fun deserializeFlashcard(jsonData: String, type: String): Flashcard {
        return when (type) {
            "basic" -> json.decodeFromString<BasicFlashcard>(jsonData)
            "cloze" -> json.decodeFromString<ClozeFlashcard>(jsonData)
            "quiz" -> json.decodeFromString<QuizFlashcard>(jsonData)
            else -> throw IllegalArgumentException("Tipo desconhecido: $type")
        }
    }
}