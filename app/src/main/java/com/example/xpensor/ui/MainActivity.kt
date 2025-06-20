package com.example.xpensor.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xpensor.R
import com.example.xpensor.adapters.TransactionAdapter
import com.example.xpensor.data.SettingsDataStore
import com.example.xpensor.databinding.ActivityMainBinding
import com.example.xpensor.model.Transaction
import com.example.xpensor.viewmodels.TransactionViewModel
import com.example.xpensor.viewmodels.TransactionViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var settingsDataStore: SettingsDataStore
    private var isAsc: Boolean = false
    private var searchView: SearchView? = null

    private val vm: TransactionViewModel by viewModels {
        TransactionViewModelFactory(application)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayUseLogoEnabled(true)
        }

        setupToolbar()
        setupViews()
        observeSettings()
        observeTransactions()
        setupListeners()
    }

    private fun setupViews() {
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter { transaction ->
            startDetailedActivity(transaction.id)
        }
        binding.recyclerview.adapter = transactionAdapter
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_search -> true
                else -> false
            }
        }
    }

    private fun observeSettings() {
        settingsDataStore = SettingsDataStore(this)
        settingsDataStore.preferenceFlow.asLiveData().observe(this) { asc ->
            isAsc = asc
            refreshTransactionList()
        }
    }

    private fun observeTransactions() {
        vm.getAllTransactions(isAsc).observe(this) { transactions ->
            vm.updateDashboard(transactions)
            updateUI(transactions)
        }
    }

    private fun setupListeners() {
        binding.addBtn.setOnClickListener { startAddTransactionActivity() }
        binding.sortButton.setOnClickListener { toggleSortOrder() }
    }

    private fun startDetailedActivity(transactionId: Int) {
        val intent = Intent(this, DetailedActivity::class.java)
            .apply { putExtra("transactionId", transactionId) }
        startActivity(intent)
    }

    private fun startAddTransactionActivity() {
        startActivity(Intent(this, AddTransactionActivity::class.java))
    }

    private fun toggleSortOrder() {
        lifecycleScope.launch {
            settingsDataStore.saveLayoutToPreferencesStore(!isAsc, this@MainActivity)
        }
    }

    private fun updateUI(transactions: List<Transaction>) {
        transactionAdapter.submitList(transactions)
        binding.balance.text = vm.formattedAmount(vm.totalAmount)
        binding.budget.text = vm.formattedAmount(vm.budgetAmount)
        binding.expense.text = vm.formattedAmount(vm.expenseAmount)
    }

    private fun refreshTransactionList() {
        val query = searchView?.query?.toString() ?: ""
        searchDatabase(query)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        val item = menu.findItem(R.id.menu_search)
        searchView = item.actionView as? SearchView
        searchView?.apply {
            isSubmitButtonEnabled = true
            setOnQueryTextListener(this@MainActivity)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?) = true

    override fun onQueryTextChange(query: String?): Boolean {
        query?.let { searchDatabase(it) }
        return true
    }

    private fun searchDatabase(query: String) {
        val searchQuery = "%$query%"
        vm.searchDatabase(searchQuery, isAsc).observe(this) { transactions ->
            updateUI(transactions)
        }
    }
}
