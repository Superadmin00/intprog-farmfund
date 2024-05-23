package com.intprog.farmfund.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.PaymentMethodAdapter
import com.intprog.farmfund.dataclasses.PaymentMethod
import com.intprog.farmfund.dataclasses.Project
import com.intprog.farmfund.dataclasses.Transaction

class WithdrawFundsActivity : AppCompatActivity() {

    private lateinit var bankRecyclerView: RecyclerView
    private lateinit var project: Project
    private lateinit var paymentMethods: List<PaymentMethod>
    private lateinit var adapter: PaymentMethodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_funds)

        project = intent.getSerializableExtra("project") as Project

        val projectNameTextView = findViewById<TextView>(R.id.projTitleWithdraw)
        projectNameTextView.text = project.projTitle

        val projectWithdrawableFunds = findViewById<TextView>(R.id.withdrawableFunds)
        projectWithdrawableFunds.text = String.format("%.2f", project.projFundsReceived)

        val projectWithdrawStatus = findViewById<TextView>(R.id.projectStatusWithdraw)
        projectWithdrawStatus.text = project.projStatus

        val projectImageView = findViewById<ImageView>(R.id.projFirstImageDonate)
        if (project.imageUrls.isNotEmpty()) {
            val imageUrl = project.imageUrls[0]
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                .into(projectImageView)
        } else {
            projectImageView.setImageResource(R.drawable.ic_launcher_background)
        }

        bankRecyclerView = findViewById(R.id.withdrawBankRecyclerView)
        bankRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch payment methods and set up the adapter
        fetchPaymentMethods()

        val withdrawButton = findViewById<Button>(R.id.withdrawButton)
        withdrawButton.setOnClickListener {
            val selectedPaymentMethod = adapter.retrieveSelectedPaymentMethod()
            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (project.projStatus == "Finished") {
                val dialog = WithdrawalSuccessDialog(this, project, projectWithdrawableFunds, selectedPaymentMethod)
                dialog.show()
            } else if (project.projStatus == "Withdrawn") {
                Toast.makeText(this, "Project funds already withdrawn.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Project funds can only be withdrawn when the project is finished.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchPaymentMethods() {
        // Fetch the payment methods from your data source (e.g., Firebase Firestore)
        val db = FirebaseFirestore.getInstance()
        db.collection("paymentMethods")
            .get()
            .addOnSuccessListener { result ->
                paymentMethods = result.toObjects(PaymentMethod::class.java)
                adapter = PaymentMethodAdapter(paymentMethods)
                bankRecyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load payment methods.", Toast.LENGTH_SHORT).show()
            }
    }

    class WithdrawalSuccessDialog(
        context: Context,
        private val project: Project,
        private val withdrawableFundsTextView: TextView,
        private val selectedPaymentMethod: PaymentMethod
    ) : Dialog(context) {
        init {
            // Set custom dialog properties
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_withdrawal_succesful)

            val auth: FirebaseAuth = FirebaseAuth.getInstance()

            // Update project status to Withdrawn and projFundsReceived to 0
            val db = FirebaseFirestore.getInstance()
            val withdrawalAmount = project.projFundsReceived
            db.collection("projects").document(project.projId)
                .update("projStatus", "Withdrawn", "projFundsReceived", 0.0)
                .addOnSuccessListener {
                    // Update the TextView to show 0.00
                    withdrawableFundsTextView.text = String.format("%.2f", 0.0)

                    // Start NavigatorActivity
                    val intent = Intent(context, NavigatorActivity::class.java)
                    context.startActivity(intent)

                    // If you want to finish the current activity (optional)
                    if (context is Activity) {
                        context.finish()
                    }

                    // Create a new transaction
                    val user = auth.currentUser
                    if (user != null) {
                        val transaction = Transaction(
                            transactionId = "",
                            userId = user.uid,
                            projId = project.projId,
                            voucherId = "", // Leave this as blank for a donation/withdrawal transaction
                            transactionType = "Withdrawal",
                            transactionAmount = withdrawalAmount,
                            paymentMethod = selectedPaymentMethod.paymethodName,
                            transactionDateTime = com.google.firebase.Timestamp.now(), // Use the current date and time
                            transactionStatus = "Completed"
                        )

                        // Add the transaction to the "transactions" collection
                        val transactionDocRef = db.collection("transactions").document()
                        transaction.transactionId = transactionDocRef.id // Set the transactionId to the document ID

                        transactionDocRef.set(transaction)
                            .addOnSuccessListener {
                                Log.d("WithdrawFundsActivity", "Transaction recorded successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("WithdrawFundsActivity", "Failed to record transaction: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update project status.", Toast.LENGTH_SHORT).show()
                }

            val closeDialogButton = findViewById<Button>(R.id.closeDialogBTN)
            closeDialogButton.setOnClickListener {
                // Close the dialog
                dismiss()
            }
        }
    }
}