package com.AnkiAppAndroid.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baralhos")
data class Baralho(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val mongoId: String? = null // ID do baralho no MongoDB/API
)