package com.example.repositories

import com.example.models.StatisticsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class StatisticsRepository {
    fun recordReview(flashcardId: Int, userId: Int, success: Boolean, intervalDays: Int) {
        transaction {
            // Atualiza ou insere nova estatística
            StatisticsTable.insertIgnore {
                it[StatisticsTable.flashcardId] = flashcardId
                it[StatisticsTable.userId] = userId
                it[lastReviewed] = LocalDateTime.now()
                it[this.success] = success
                it[this.intervalDays] = intervalDays
            }
        }
    }

    fun getStatistics(userId: Int): List<Map<String, Any?>> {
        return transaction {
            StatisticsTable
                .select { StatisticsTable.userId eq userId }
                .map { row ->
                    mapOf(
                        "flashcardId" to row[StatisticsTable.flashcardId],
                        "lastReviewed" to row[StatisticsTable.lastReviewed],
                        "success" to row[StatisticsTable.success],
                        "intervalDays" to row[StatisticsTable.intervalDays]
                    )
                }
        }
    }
}