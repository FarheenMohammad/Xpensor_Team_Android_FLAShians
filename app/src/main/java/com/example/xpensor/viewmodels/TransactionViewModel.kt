package com.example.xpensor.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.xpensor.repository.TransactionRepository
import com.example.xpensor.room.AppDatabase
import com.example.xpensor.model.Transaction
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(AppDatabase.getDatabase(application))

    var totalAmount = 0.0
    var budgetAmount = 0.0
    var expenseAmount = 0.0

    fun getAllTransactions(isAsc: Boolean): LiveData<List<Transaction>> {
        return repository.getAllTransactions(isAsc).asLiveData()
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun deleteTransaction(transactionId: Int) = viewModelScope.launch {
        repository.deleteTransaction(transactionId)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun getTransactionById(transactionId: Int): LiveData<Transaction> {
        return repository.getTransactionById(transactionId).asLiveData()
    }

    fun searchDatabase(searchQuery: String, isAsc: Boolean): LiveData<List<Transaction>> {
        return repository.searchTransactions(searchQuery, isAsc).asLiveData()
    }

    fun updateDashboard(transactions: List<Transaction>) {
        totalAmount = transactions.sumOf { it.amount }
        budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        expenseAmount = totalAmount - budgetAmount
    }

    fun formattedAmount(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(amount)
    }

}
