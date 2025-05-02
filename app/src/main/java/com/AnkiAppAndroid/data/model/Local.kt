package com.AnkiAppAndroid.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locais")
data class Local(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String
) 