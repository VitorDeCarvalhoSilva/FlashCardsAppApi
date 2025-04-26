package com.example.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.User
import com.example.models.UsersTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {
    route("/auth") {
        // Registrar usuário
        post("/register") {
            val userRequest = call.receive<User>() // Usa o DTO User com 'password'

            // Verificar se o username já existe
            val existingUser = transaction {
                UsersTable.select { UsersTable.username eq userRequest.username }.firstOrNull()
            }

            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Usuário já existe"))
                return@post
            }

            // Hash da senha BRUTA
            val hashedPassword = BCrypt.hashpw(userRequest.password, BCrypt.gensalt())

            // Salvar no banco (usa passwordHash da tabela)
            transaction {
                UsersTable.insert {
                    it[username] = userRequest.username
                    it[passwordHash] = hashedPassword // Campo da tabela
                }
            }

            call.respond(HttpStatusCode.Created, mapOf("message" to "Usuário registrado com sucesso"))
        }

        // Login
        post("/login") {
            val loginRequest = call.receive<User>() // Usa o DTO User com 'password'

            val user = transaction {
                UsersTable.select { UsersTable.username eq loginRequest.username }.firstOrNull()
            }

            //teste para tentar retornar id do usuário
            val id = transaction {
                UsersTable.select { UsersTable.username eq loginRequest.username }
                    .firstOrNull()
                    ?.get(UsersTable.id)
                    ?.value
            }

            println("ID do usuário: $id")



            // Verificar senha BRUTA com hash
            if (user == null || !BCrypt.checkpw(loginRequest.password, user[UsersTable.passwordHash])) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciais inválidas"))
                return@post
            }

            // Gerar token JWT
            val token = JWT.create()
                .withIssuer("sua_api")
                .withAudience("seus_usuarios")
                .withClaim("username", loginRequest.username)
                .sign(Algorithm.HMAC256("sua_chave_secreta_super_segura"))

            println("Respondendo com id=$id, username=${loginRequest.username}, token=$token")


            //Cria a um user e passa o objeto no retorno, unica maneira que aceita o valor do id
            val data = User(id, loginRequest.username, loginRequest.password, token)

            call.respond(mapOf
                (
                "data" to data
                )
            )
        }
    }
}