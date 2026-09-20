package com.homebudget.expenses.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homebudget.expenses.data.Transaction
import com.homebudget.expenses.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val showDialog by viewModel.showAddDialog.collectAsState()
    val dialogType by viewModel.dialogType.collectAsState()

    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = { viewModel.showAddTransactionDialog(TransactionType.EXPENSE) },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة مصروف")
                }
                FloatingActionButton(
                    onClick = { viewModel.showAddTransactionDialog(TransactionType.INCOME) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة دخل")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // بطاقة الرصيد
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الرصيد المتوفر", style = MaterialTheme.typography.titleMedium)
                    Text(String.format("%.2f", balance), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("المدخولات", color = Color(0xFF2E7D32))
                            Text(String.format("%.2f", totalIncome), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("المصروفات", color = Color(0xFFD32F2F))
                            Text(String.format("%.2f", totalExpense), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("آخر العمليات", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start))

            // قائمة العمليات
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactions) { transaction ->
                    TransactionItem(transaction, onDelete = { viewModel.deleteTransaction(transaction) })
                }
            }
        }
    }

    if (showDialog) {
        AddTransactionDialog(type = dialogType, onDismiss = { viewModel.hideDialog() }, onAdd = { amt, cat, note, date -> viewModel.addTransaction(amt, cat, note, date) })
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
    val isIncome = transaction.type == TransactionType.INCOME

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, fontWeight = FontWeight.Bold)
                Text(String.format("%.2f ر.س", transaction.amount), color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                Text(dateFormat.format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall)
                if (transaction.note.isNotBlank()) Text(transaction.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(type: TransactionType, onDismiss: () -> Unit, onAdd: (Double, String, String, Long) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val showDatePicker = remember { mutableStateOf(false) }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = { TextButton(onClick = { selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis(); showDatePicker.value = false }) { Text("موافق") } },
            dismissButton = { TextButton(onClick = { showDatePicker.value = false }) { Text("إلغاء") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.INCOME) "إضافة دخل جديد" else "إضافة مصروف جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(if (type == TransactionType.INCOME) "مصدر الرصيد" else "تصنيف المصروف") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { showDatePicker.value = true }) {
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
                    Text("التاريخ: ${dateFormat.format(Date(selectedDate))}")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && category.isNotBlank()) {
                    onAdd(amt, category, note, selectedDate)
                }
            }) { Text("إضافة") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
