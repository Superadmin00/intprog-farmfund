package com.intprog.farmfund.activities

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
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
    private lateinit var adapter: TransactionsHistoryAdapter
    private lateinit var sorterSpinner: Spinner

    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionsHistoryAdapter(emptyList())
        transactionsRecyclerView.adapter = adapter

        sorterSpinner = findViewById(R.id.sorterSpinner)
        setupSorterSpinner()

        swipeRefreshLayout.isRefreshing = true

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            fetchTransactions(user.uid)
        }

        swipeRefreshLayout.setOnRefreshListener {
            user?.uid?.let { fetchTransactions(it) }
        }

        val clickBackButton = findViewById<ImageButton>(R.id.backButton)
        clickBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setupSorterSpinner() {
        val transactionTypes = arrayOf("All", "Donation", "Voucher Redemption")
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            transactionTypes
        ).also { it.setDropDownViewResource(R.layout.spinner_item) }

        sorterSpinner.adapter = adapter

        sorterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentFilter = parent.getItemAtPosition(position).toString()
                auth.currentUser?.uid?.let { fetchTransactions(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun fetchTransactions(userId: String) {
        var query: Query = db.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("transactionDateTime", Query.Direction.DESCENDING)

        if (currentFilter != "All") {
            query = query.whereEqualTo("transactionType", currentFilter)
        }

        query.get()
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