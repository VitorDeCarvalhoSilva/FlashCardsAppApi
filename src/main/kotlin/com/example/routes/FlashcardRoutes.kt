package com.example.routes

import com.example.models.*
import com.example.repositories.FlashcardRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.flashcardRoutes(repository: FlashcardRepository) {
    authenticate {
        route("/flashcards") {
            // Criar flashcard básico
            post("/basic") {
                try {
                    val flashcard = call.receive<BasicFlashcard>()
                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: throw IllegalArgumentException("Usuário não autenticado")
                    val id = repository.save(flashcard, username)
                    call.respond(HttpStatusCode.Created, mapOf("id" to id))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // Criar flashcard cloze
            post("/cloze") {
                try {
                    val flashcard = call.receive<ClozeFlashcard>()
                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: throw IllegalArgumentException("Usuário não autenticado")

                    // Validação específica para Cloze
                    if (!flashcard.fullText.contains(flashcard.hiddenPart)) {
                        throw IllegalArgumentException("Parte oculta deve estar contida no texto completo")
                    }

                    val id = repository.save(flashcard, username)
                    call.respond(HttpStatusCode.Created, mapOf("id" to id))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // Criar flashcard quiz
            post("/quiz") {
                try {
                    val flashcard = call.receive<QuizFlashcard>()
                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: throw IllegalArgumentException("Usuário não autenticado")

                    // Validação adicional se necessário
                    if (flashcard.options.size < 2) {
                        throw IllegalArgumentException("Quiz precisa de pelo menos 2 opções")
                    }

                    val id = repository.save(flashcard, username)
                    call.respond(HttpStatusCode.Created, mapOf("id" to id))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // Listar flashcards por tipo
            get("/{type}") {
                val type = call.parameters["type"]?.lowercase()
                    ?: throw IllegalArgumentException("Tipo inválido")

                // Validação de tipos permitidos
                if (type !in listOf("basic", "cloze", "quiz")) {
                    throw IllegalArgumentException("Tipo não suportado: $type")
                }

                val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                    ?: throw IllegalArgumentException("Usuário não autenticado")

                val flashcards = repository.getByType(username, type)
                call.respond(flashcards)
            }
        }
    }
}