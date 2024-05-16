package com.intprog.farmfund.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.farmfund.R
import com.intprog.farmfund.dataclasses.Transaction

class TransactionsHistoryAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsHistoryAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transactions_history, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.transactionIcon.setImageResource(transaction.icon)
        holder.transactionTitle.text = transaction.title
        holder.transactionType.text = transaction.type
        holder.transactionDate.text = transaction.date
        holder.transactionTime.text = transaction.time
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionIcon: ImageView = itemView.findViewById(R.id.transacIcon)
        val transactionTitle: TextView = itemView.findViewById(R.id.transacTitle)
        val transactionType: TextView = itemView.findViewById(R.id.transacType)
        val transactionDate: TextView = itemView.findViewById(R.id.transacDate)
        val transactionTime: TextView = itemView.findViewById(R.id.transacTime)
    }
}