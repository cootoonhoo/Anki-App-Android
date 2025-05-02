package com.AnkiAppAndroid

import android.app.Application
import com.AnkiAppAndroid.data.database.AppDatabase

class AnkiApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}