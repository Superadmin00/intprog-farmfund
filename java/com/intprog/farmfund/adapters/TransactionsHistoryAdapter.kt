package com.intprog.farmfund.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.ProjectDetailsActivity
import com.intprog.farmfund.databinding.ItemTransactionBinding
import com.intprog.farmfund.dataclasses.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

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
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())  // Combine date and time format
            formatter.format(it)
        } ?: "-"
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        with(holder.binding) {
            when (transaction.transactionType) {
                "Donation" -> {
                    transacType.text = "Donation"
                    transacAmount.text = "PHP ${String.format("%.2f", transaction.transactionAmount)}"
                    transacIcon.setImageResource(R.drawable.ic_donate)
                    transacType.setTextColor(ContextCompat.getColor(root.context, R.color.green))
                    //transacContainer.setBackgroundColor(ContextCompat.getColor(root.context, R.color.blue))
                }
                "Withdrawal" -> {
                    transacType.text = "Withdrawal"
                    transacAmount.text = "PHP ${String.format("%.2f", transaction.transactionAmount)}"
                    transacIcon.setImageResource(R.drawable.ic_withdraw)
                    transacType.setTextColor(ContextCompat.getColor(root.context, R.color.withdrawnStatus))
                }
                "Voucher Redemption" -> {
                    transacType.text = "Voucher Redemption"
                    transacAmount.text = "${transaction.transactionAmount} FundPoints"
                    transacIcon.setImageResource(R.drawable.ic_voucher_redeem)
                    transacType.setTextColor(ContextCompat.getColor(root.context, R.color.activeStatus))
                }
            }
            transacDate.text = formatDateTime(transaction.transactionDateTime)
            transacStatus.text = transaction.transactionStatus.uppercase()

            root.setOnClickListener {
                showTransactionDetailsDialog(it.context, transaction)
            }
        }
    }


    override fun getItemCount() = transactions.size

    private fun showTransactionDetailsDialog(context: Context, transaction: Transaction) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaction_details, null)
        val dialog = Dialog(context).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        with(dialogView) {
            if (transaction.transactionType != "Voucher Redemption") {
                findViewById<TextView>(R.id.transactionTypeText).text = transaction.transactionType
                findViewById<TextView>(R.id.transactionAmountText).text = "PHP ${String.format("%.2f", transaction.transactionAmount)}"
                findViewById<TextView>(R.id.payMethodVouchTypeText).text = transaction.paymentMethod
                findViewById<TextView>(R.id.transacDateTimeText).text = formatDateTime(transaction.transactionDateTime)

                val projectVoucherText = findViewById<TextView>(R.id.projectVoucherText)
                val projectId = transaction.projId
                // Retrieve project details from the database
                val db = FirebaseFirestore.getInstance()
                db.collection("projects").document(projectId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val projTitle = document.getString("projTitle")
                            projectVoucherText.text = projTitle ?: "Project not found"
                        } else {
                            projectVoucherText.text = "Project not found"
                        }
                    }
                    .addOnFailureListener {
                        projectVoucherText.text = "Project not found"
                    }

                val transacDetailsBTN: TextView = findViewById(R.id.transacDetailsBTN)
                transacDetailsBTN.text = "Visit Project"
                transacDetailsBTN.setOnClickListener {
                    if (projectVoucherText.text == "Project not found") {
                        Toast.makeText(context, "Project not found.", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(context, ProjectDetailsActivity::class.java)
                        intent.putExtra("projId2", projectId)
                        dialog.dismiss()
                        context.startActivity(intent)
                    }
                }
            } else {
                findViewById<TextView>(R.id.transactionTypeText).text = transaction.transactionType
                findViewById<TextView>(R.id.transactionAmountText).text = "${transaction.transactionAmount} FundPoints"
                findViewById<TextView>(R.id.transacDateTimeText).text = formatDateTime(transaction.transactionDateTime)

                findViewById<TextView>(R.id.projectVoucherLabel).text = "Voucher Name"
                findViewById<TextView>(R.id.payMethodVouchTypeLabel).text = "Voucher Type"

                val projectVoucherText:TextView = findViewById(R.id.projectVoucherText)
                val payMethodVouchTypeText:TextView = findViewById(R.id.payMethodVouchTypeText)

                val voucherId = transaction.voucherId
                // Retrieve project details from the database
                val db = FirebaseFirestore.getInstance()
                db.collection("vouchers").document(voucherId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val voucherBrand = document.getString("voucherBrand")
                            val voucherReward = document.get("voucherReward") as? Number
                            val voucherType = document.getString("voucherType")

                            projectVoucherText.text = "$voucherReward $voucherBrand"
                            payMethodVouchTypeText.text = "$voucherType"
                        } else {
                            projectVoucherText.text = "Voucher Not Found"
                            payMethodVouchTypeText.text = "Voucher Not Found"
                        }
                    }
                    .addOnFailureListener {
                        projectVoucherText.text = "Voucher Not Found"
                        payMethodVouchTypeText.text = "Voucher Not Found"
                    }

                val transacDetailsBTN: TextView = findViewById(R.id.transacDetailsBTN)
                transacDetailsBTN.text = "Okay"
                transacDetailsBTN.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }
}