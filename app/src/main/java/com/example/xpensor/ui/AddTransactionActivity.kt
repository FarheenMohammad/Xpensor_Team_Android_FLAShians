package com.example.xpensor.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.xpensor.R
import com.example.xpensor.databinding.ActivityAddTransactionBinding
import com.example.xpensor.model.Transaction
import com.example.xpensor.viewmodels.TransactionViewModel
import com.example.xpensor.viewmodels.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private val vm: TransactionViewModel by viewModels {
        TransactionViewModelFactory(application)
    }

    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Add Expense"

        setupUI()
    }

    private fun setupUI() {
        setupRootView()
        setupDropdown()
        setupDatePicker()
        setupButtons()
    }

    private fun setupRootView() {
        binding.addRootView.setOnClickListener { hideKeyboard(it) }
    }

    private fun hideKeyboard(view: View) {
        this.window.decorView.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupDropdown() {
        val labelExpense = resources.getStringArray(R.array.labelExpense)
        val labelIncome = resources.getStringArray(R.array.labelIncome)
        arrayAdapter = ArrayAdapter(this, R.layout.dropdown, labelExpense)
        binding.labelInput.setAdapter(arrayAdapter)

        binding.expense.setOnClickListener { updateDropdown(labelExpense) }
        binding.income.setOnClickListener { updateDropdown(labelIncome) }

        binding.labelInput.addTextChangedListener {
            if (it!!.isNotEmpty()) binding.labelLayout.error = null
        }

        binding.amountInput.addTextChangedListener {
            if (it!!.isNotEmpty()) binding.amountLayout.error = null
        }
    }

    private fun updateDropdown(labels: Array<String>) {
        if (binding.labelInput.text.toString() !in labels.toList()) {
            binding.labelInput.setText("")
            arrayAdapter = ArrayAdapter(this, R.layout.dropdown, labels)
            binding.labelInput.setAdapter(arrayAdapter)
        }
    }

    private fun setupDatePicker() {
        val initialDate = Date()
        binding.calendarDate.setText(formatDate(initialDate))

        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.calendarDate.setText(formatDate(cal.time))
        }

        binding.calendarDate.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
        return sdf.format(date)
    }

    private fun setupButtons() {
        binding.addTransactionBtn.setOnClickListener { validateAndAddTransaction() }
        binding.closeBtn.setOnClickListener { finish() }
    }

    private fun validateAndAddTransaction() {
        val label = binding.labelInput.text.toString()
        val description = binding.descriptionInput.text.toString()
        val amount = binding.amountInput.text.toString().toDoubleOrNull()
        val date = Date()

        when {
            label.isEmpty() -> binding.labelLayout.error = "Please enter a valid label"
            amount == null -> binding.amountLayout.error = "Please enter a valid amount"
            else -> {
                val finalAmount = if (binding.expense.isChecked) -amount else amount
                val transaction = Transaction(0, label, finalAmount, description, date)
                insertTransaction(transaction)
            }
        }
    }

    private fun insertTransaction(transaction: Transaction) {
        vm.insertTransaction(transaction)
        navigateToMainActivity("Transaction Created")
    }

    private fun navigateToMainActivity(message: String) {
        startActivity(Intent(this, MainActivity::class.java))
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
