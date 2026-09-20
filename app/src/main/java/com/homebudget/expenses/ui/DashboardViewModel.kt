package com.homebudget.expenses.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homebudget.expenses.data.AppDatabase
import com.homebudget.expenses.data.Transaction
import com.homebudget.expenses.data.TransactionDao
import com.homebudget.expenses.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: TransactionDao = AppDatabase.getDatabase(application).transactionDao()

    val transactions = dao.getAllTransactions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _dialogType = MutableStateFlow(TransactionType.INCOME)
    val dialogType = _dialogType.asStateFlow()

    fun showAddTransactionDialog(type: TransactionType) {
        _dialogType.value = type
        _showAddDialog.update { true }
    }

    fun hideDialog() {
        _showAddDialog.update { false }
    }

    fun addTransaction(amount: Double, category: String, note: String, date: Long) {
        viewModelScope.launch {
            val newTransaction = Transaction(
                amount = amount,
                type = _dialogType.value,
                category = category,
                date = date,
                note = note
            )
            dao.insert(newTransaction)
            hideDialog()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction)
        }
    }
}
