package com.example.routes

import com.example.models.UsersTable
import com.example.repositories.StatisticsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.statisticsRoutes(repository: StatisticsRepository) {
    authenticate {
        route("/statistics") {
            // Registrar uma revisão de flashcard
            post("/review") {
                try {
                    val request = call.receive<Map<String, Any>>()
                    val flashcardId = request["flashcardId"] as Int
                    val success = request["success"] as Boolean
                    val intervalDays = request["intervalDays"] as? Int ?: 1

                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: throw IllegalArgumentException("Usuário não autenticado")

                    // Obter userId do username
                    val userId = transaction {
                        UsersTable
                            .select { UsersTable.username eq username }
                            .first()[UsersTable.id].value
                    }

                    repository.recordReview(flashcardId, userId, success, intervalDays)
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Revisão registrada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // Listar estatísticas do usuário
            get {
                val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                    ?: throw IllegalArgumentException("Usuário não autenticado")

                val userId = transaction {
                    UsersTable
                        .select { UsersTable.username eq username }
                        .first()[UsersTable.id].value
                }

                val stats = repository.getStatistics(userId)
                call.respond(stats)
            }
        }
    }
}