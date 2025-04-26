package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.*
import com.example.repositories.*
import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Configuração do Banco de Dados
    val dbPath = File("flashcards.db").absolutePath
    println("📍 Banco de dados será criado em: $dbPath")
    Database.connect(
        url = "jdbc:sqlite:$dbPath",
        driver = "org.sqlite.JDBC"
    )

    // Criar tabelas (CORREÇÃO: SchemaUtils.create)
    transaction {
        SchemaUtils.create(FlashcardsTable, UsersTable, StatisticsTable, SubjectsTable)
    }

    // Configurar JWT (NOME CORRETO: configureJWT)
    configureJWT()

    // Instalar plugins e rotas (NOME CORRETO: installPlugins)
    installPlugins()
    configureRouting()
}

// Função de configuração JWT (ADICIONADA)
fun Application.configureJWT() {
    install(Authentication) {
        jwt {
            val secret = "sua_chave_secreta_super_segura"
            val issuer = "sua_api"
            val audience = "seus_usuarios"

            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("username").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

// Função para instalar plugins (ADICIONADA)
fun Application.installPlugins() {
    install(CallLogging)
    install(ContentNegotiation) {
        json()
    }
}

// Configuração de rotas (CORRIGIDA A INDENTAÇÃO)
fun Application.configureRouting() {
    val flashcardRepository = FlashcardRepository()
    val statisticsRepository = StatisticsRepository()
    val subjectRepository = SubjectRepository()

    routing {
        get("/") {
            call.respondText("API de Flashcards Online! ✅")
        }
        authRoutes()

        authenticate {
            flashcardRoutes(flashcardRepository)
            statisticsRoutes(statisticsRepository)
            subjectRoutes(subjectRepository)
        }
    }
}