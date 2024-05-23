package com.intprog.farmfund.activities

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.TransactionsHistoryAdapter
import com.intprog.farmfund.dataclasses.Transaction

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: TransactionsHistoryAdapter // Declare adapter here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionsHistoryAdapter(emptyList()) // Initialize adapter here
        transactionsRecyclerView.adapter = adapter

        swipeRefreshLayout.isRefreshing = true

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            fetchTransactions(user.uid)
        } else {
            // Handle the case where no user is logged in (optional)
        }

        swipeRefreshLayout.setOnRefreshListener {
            user?.uid?.let { fetchTransactions(it) }
        }

        val clickBackButton = findViewById<ImageButton>(R.id.backButton)
        clickBackButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchTransactions(userId: String) {
        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("transactionDateTime", Query.Direction.DESCENDING) // Order by date and time in descending order
            .get()
            .addOnSuccessListener { documents ->
                val transactions = documents.toObjects(Transaction::class.java)
                adapter.transactions = transactions
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                swipeRefreshLayout.isRefreshing = false
            }
    }
}