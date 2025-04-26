// src/main/kotlin/com/example/models/Subject.kt
package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Subject(
    val id: Int? = null,
    val name: String
)

// Tabela do banco de dados
object SubjectsTable : IntIdTable("subjects") {
    val name = varchar("name", 100).uniqueIndex() // Nome único para evitar duplicatas
}