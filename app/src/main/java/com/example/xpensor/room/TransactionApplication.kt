package com.example.xpensor.room

import android.app.Application

class TransactionApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
