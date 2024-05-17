package com.intprog.farmfund.activities

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.TransactionsHistoryAdapter
import com.intprog.farmfund.dataclasses.Transaction

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var transactionsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)

        // Dummy data pa goyyyyyyyyyyyyyyyyyyyyyyy
        val transactions = listOf(
            Transaction(R.drawable.ic_deposit, "Transaction 1", "Deposit", "05/25/2024", "03:12 PM"),
            Transaction(R.drawable.ic_withdraw, "Transaction 2", "Withdraw", "05/25/2024", "03:12 PM"),
            Transaction(R.drawable.ic_deposit, "Transaction 3", "Deposit", "05/25/2024", "03:12 PM"),
            Transaction(R.drawable.ic_donate, "Transaction 4", "Donate", "05/25/2024", "03:12 PM"),
            Transaction(R.drawable.ic_deposit, "Transaction 5", "Deposit", "05/25/2024", "03:12 PM"),
            Transaction(R.drawable.ic_withdraw, "Transaction 6", "Withdraw", "05/25/2024", "03:12 PM"),
            Transaction(R.drawable.ic_donate, "Transaction 7", "Donate", "05/25/2024", "03:12 PM")
        )

        // Set layout manager for transactionsRecyclerView
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)  // Use 'this' for context
        transactionsRecyclerView.adapter = TransactionsHistoryAdapter(transactions)

        // Handle swipe to refresh functionality (optional)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
        }

        val clickBackButton = findViewById<ImageButton>(R.id.backButton)
        clickBackButton.setOnClickListener {
            finish()
        }
    }
}