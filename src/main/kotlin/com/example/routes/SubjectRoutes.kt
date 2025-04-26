// src/main/kotlin/com/example/routes/SubjectRoutes.kt
package com.example.routes

import com.example.models.Subject
import com.example.repositories.SubjectRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.subjectRoutes(repository: SubjectRepository) {
    route("/subjects") {
        // Criar assunto
        post {
            try {
                val request = call.receive<Subject>()
                val id = repository.create(request.name)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Listar todos os assuntos
        get {
            val subjects = repository.getAll()
            call.respond(subjects)
        }
    }
}