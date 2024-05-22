package com.intprog.farmfund.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intprog.farmfund.R
import com.intprog.farmfund.databinding.ItemTransactionBinding
import com.intprog.farmfund.dataclasses.Transaction
import java.text.SimpleDateFormat

class TransactionsHistoryAdapter(var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsHistoryAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    private fun formatDateTime(dateTime: com.google.firebase.Timestamp?): String {
        return dateTime?.toDate()?.let {
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a")  // Combine date and time format
            formatter.format(it)
        } ?: "-"
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        with(holder.binding) {
            when (transaction.transactionType) {
                "Donation" -> transacIcon.setImageResource(R.drawable.ic_donate)
                "Withdrawal" -> transacIcon.setImageResource(R.drawable.ic_withdraw)
                "Voucher Redemption" -> transacIcon.setImageResource(R.drawable.ic_voucher_redeem)
            }
            transacAmount.text = when (transaction.transactionType) {
                "Voucher Redemption" -> "${transaction.transactionAmount} FundPoints"
                else -> "PHP ${String.format("%.2f", transaction.transactionAmount?.toDouble())}"
            }
            transacType.text = transaction.transactionType
            transacDate.text = formatDateTime(transaction.transactionDateTime)
            transacStatus.text = transaction.transactionStatus.uppercase()
        }
    }

    override fun getItemCount() = transactions.size
}