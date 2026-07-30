package com.shinytracker.core.database.entities

import androidx.room.Entity

@Entity(tableName = "caught", primaryKeys = ["dexId", "formId", "costumeId", "shiny"])
data class CaughtEntity(
    val dexId: Int,
    val formId: Int,
    val costumeId: Int,
    val shiny: Boolean,
    val name: String,
    val caughtAt: Long,
)
