package com.example.xpensor.model

import android.icu.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val label: String,
    val amount: Double,
    val description: String,
    val date: Date
)

fun Transaction.getFormattedDate(): String =
    SimpleDateFormat("EEEE, dd MMM yyyy").format(date)

fun Transaction.getFormattedAmount(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount)
}
