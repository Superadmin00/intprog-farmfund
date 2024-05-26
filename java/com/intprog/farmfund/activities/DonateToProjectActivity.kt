package com.intprog.farmfund.activities

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.PaymentMethodAdapter
import com.intprog.farmfund.databinding.ActivityDonateToProjectBinding
import com.intprog.farmfund.dataclasses.PaymentMethod
import com.intprog.farmfund.dataclasses.Project
import com.intprog.farmfund.dataclasses.Transaction
import com.intprog.farmfund.objects.HideKeyboardOnClick
import com.intprog.farmfund.objects.LoadingDialog

class DonateToProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonateToProjectBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private val REQUEST_CODE_IMAGE_PICKER = 100
    private var currentProject: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonateToProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Retrieve data from intent
        val projId = intent.getStringExtra("projId")
        val imageUrl = intent.getStringExtra("imageUrl")

        if (projId == null) {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.swipeRefreshLayout.isRefreshing = true

        fetchProjectDetails(projId)

        Glide.with(this).load(imageUrl).into(binding.projFirstImageDonate)

        val checkBoxes = listOf(binding.box50, binding.box100, binding.box500, binding.box1000)

        checkBoxes.forEach { checkBox ->
            checkBox.setBackgroundResource(R.drawable.bg_checkboxes_amount)
            checkBox.setOnClickListener {
                binding.customAmountEditText.text = null // Clear custom amount field
                binding.customAmountEditText.clearFocus()
                // Close keyboard
                HideKeyboardOnClick.hideKeyboardOnClick(it)

                checkBoxes.forEach { otherCheckBox ->
                    otherCheckBox.isChecked = false
                    otherCheckBox.setTextColor(ContextCompat.getColorStateList(this, R.color.black))
                }
                checkBox.isChecked = true
                checkBox.setTextColor(ContextCompat.getColorStateList(this, R.color.white))
            }
        }

        // Add a TextWatcher to your EditText
        binding.customAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkBoxes.forEach { otherCheckBox ->
                    otherCheckBox.isChecked = false
                    otherCheckBox.setTextColor(ContextCompat.getColorStateList(this@DonateToProjectActivity, R.color.black))
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        val paymentMethods = mutableListOf<PaymentMethod>()
        db.collection("paymentMethods")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val paymentMethod = document.toObject(PaymentMethod::class.java)
                    paymentMethods.add(paymentMethod)
                }
                binding.paymethodRecyclerView.layoutManager = object : LinearLayoutManager(this) {
                    override fun canScrollVertically() = false
                }
                val adapter = PaymentMethodAdapter(paymentMethods)
                binding.paymethodRecyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        binding.paymentProofBTN.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
        }

        binding.confirmPaymentBTN.setOnClickListener {
            confirmDonation()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        // Close keyboard when clicking outside the input fields
        binding.paymentLayout.setOnClickListener {
            HideKeyboardOnClick.hideKeyboardOnClick(it)
        }
        binding.mainLayout.setOnClickListener {
            HideKeyboardOnClick.hideKeyboardOnClick(it)
        }
    }

    private fun getDonationAmount(): Int {
        val amountFromCustom = binding.customAmountEditText.text.toString().toIntOrNull() ?: 0
        val amountFromCheckBox = when {
            binding.box50.isChecked -> 50
            binding.box100.isChecked -> 100
            binding.box500.isChecked -> 500
            binding.box1000.isChecked -> 1000
            else -> 0
        }
        return maxOf(amountFromCustom, amountFromCheckBox)
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_donation_successful)
        dialog.setCancelable(false)
        dialog.findViewById<Button>(R.id.closeDialogBTN).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK) {
            val imageUri = data?.data ?: return  // Handle if no image is selected

            Glide.with(this)
                .load(imageUri)
                .into(binding.paymentProofImage)
        }
    }

    private fun fetchProjectDetails(projId: String) {
        db.collection("projects").document(projId)
            .get()
            .addOnSuccessListener { document ->
                val project = document.toObject(Project::class.java)
                if (project != null) {
                    currentProject = project
                    // Use the project object to populate your views
                    binding.projTitleDonate.text = project.projTitle
                    binding.projFundGoalDonate.text = "${project.projFundsReceived} / ${project.projFundGoal}"
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun confirmDonation() {
        val project = currentProject ?: run {
            Toast.makeText(this, "Project data not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val donationAmount = getDonationAmount().toDouble()
        val paymentMethodAdapter = binding.paymethodRecyclerView.adapter as PaymentMethodAdapter
        val selectedPaymentMethod = paymentMethodAdapter.selectedPaymentMethod
        var paymentMethod: String = ""

        val currentFunds = project.projFundsReceived

        if ((donationAmount + currentFunds) > project.projFundGoal) {
            Toast.makeText(this, "Donation exceeds the project's fund goal.", Toast.LENGTH_SHORT).show()
            return
        }

        if (donationAmount == 0.0) {
            Toast.makeText(this, "Please add amount to donate.", Toast.LENGTH_SHORT).show()
            binding.customAmountEditText.error = "Please enter or select a valid amount"
            return
        }

        if (selectedPaymentMethod != null) {
            paymentMethod = selectedPaymentMethod.paymethodName // Get the payment method name or other details
        } else {
            Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show()
            return
        }

        val hasImage = binding.paymentProofImage.drawable?.let { drawable ->
            drawable is BitmapDrawable && drawable.bitmap != null
        } ?: false

        if (hasImage) {
            LoadingDialog.show(this, false)
            if (project.projFundsReceived + donationAmount <= project.projFundGoal) {
                val newFundsReceived = project.projFundsReceived + donationAmount
                val isGoalReached = newFundsReceived >= project.projFundGoal

                val updatedProjectInfo = mutableMapOf<String, Any>(
                    "projFundsReceived" to newFundsReceived,
                    "projDonorsCount" to (project.projDonorsCount + 1)
                )

                if (isGoalReached) {
                    updatedProjectInfo["projStatus"] = "Completed"
                }

                val projectDocRef = db.collection("projects").document(project.projId)

                projectDocRef.update(updatedProjectInfo)
                    .addOnSuccessListener {
                        auth.currentUser?.let { user ->
                            val userDocRef = db.collection("users").document(user.uid)

                            userDocRef.get()
                                .addOnSuccessListener { document ->
                                    val currentFundPoints = document.getDouble("fundPoints") ?: 0.0
                                    val newFundPoints = currentFundPoints + (donationAmount * 0.1)

                                    userDocRef.update("fundPoints", newFundPoints)
                                        .addOnSuccessListener {
                                            // Additional success handling if needed
                                        }
                                }
                        }
                        // Create a new transaction
                        val transaction = auth.currentUser?.uid?.let { uid ->
                            Transaction(
                                transactionId = "", // Temporary value
                                userId = uid,
                                projId = project.projId,
                                voucherId = "", // Leave this as blank for a donation transaction
                                transactionType = "Donation",
                                transactionAmount = donationAmount,
                                paymentMethod = paymentMethod,
                                transactionDateTime = com.google.firebase.Timestamp.now(), // Use the current date and time
                                transactionStatus = "Completed"
                            )
                        }

                        // Add the transaction to the "transactions" collection
                        val transactionDocRef = db.collection("transactions").document()
                        if (transaction != null) {
                            transaction.transactionId = transactionDocRef.id
                        } // Set the transactionId to the document ID

                        if (transaction != null) {
                            transactionDocRef.set(transaction)
                                .addOnSuccessListener {
                                    Log.d("DonateToProjectActivity", "Transaction recorded successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DonateToProjectActivity", "Failed to record transaction: ${e.message}")
                                }
                        }
                        LoadingDialog.dismiss()
                        showSuccessDialog()
                    }
            } else {
                Toast.makeText(this, "Donation exceeds funding goal", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please add proof of payment.", Toast.LENGTH_SHORT).show()
            return
        }
    }
}