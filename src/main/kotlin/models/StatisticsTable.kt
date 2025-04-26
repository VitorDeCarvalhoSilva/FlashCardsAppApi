package com.example.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object StatisticsTable : IntIdTable("statistics") {
    val flashcardId = integer("flashcard_id")
    val userId = integer("user_id")
    val lastReviewed = datetime("last_reviewed")
    val success = bool("success")
    val intervalDays = integer("interval_days").default(1)

    init {
        index(true, flashcardId, userId) // Índice composto para busca rápida
    }
}