package com.intprog.farmfund.activities

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.PaymentMethodAdapter
import com.intprog.farmfund.databinding.ActivityDonateToProjectBinding
import com.intprog.farmfund.dataclasses.PaymentMethod
import com.intprog.farmfund.dataclasses.Project

class DonateToProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonateToProjectBinding
    private var project: Project? = null
    private val database = FirebaseDatabase.getInstance()
    private val projectsRef = database.getReference("projects")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonateToProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from intent
        project = intent.getSerializableExtra("project") as? Project
        if (project == null) {
            // Handle the error gracefully if the project is null
            Toast.makeText(this, "Project data not found", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
            return
        }

        val projTitle = project?.projTitle
        val projStatus = project?.projStatus
        val imageUrl = intent.getStringExtra("imageUrl")

        // Set the data to the views
        binding.projTitleDonate.text = projTitle
        binding.projStatusDonate.text = projStatus
        Glide.with(this).load(imageUrl).into(binding.projImageDonate) // Use Glide or any image loading library to load the image

        val checkBoxes = listOf(binding.box50, binding.box100, binding.box500, binding.box1000)

        checkBoxes.forEach { checkBox ->
            checkBox.setBackgroundResource(R.drawable.bg_checkboxes_amount)
            checkBox.setOnClickListener {
                binding.customAmountEditText.text = null // Clear custom amount field
                binding.customAmountEditText.clearFocus()
                // Close keyboard
                val inputMethodManager =
                    it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    it.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )

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
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Uncheck all radio buttons when text is entered
                checkBoxes.forEach { otherCheckBox ->
                    otherCheckBox.isChecked = false
                    otherCheckBox.setTextColor(ContextCompat.getColorStateList(this@DonateToProjectActivity, R.color.black))
                }
            }

            override fun afterTextChanged(s: Editable) {
                // No action needed here
            }
        })

        val paymentMethods = listOf(
            PaymentMethod(1, "GCash", R.drawable.ic_gcashlogo),
            PaymentMethod(2, "PayMaya", R.drawable.ic_gcashlogo),
            PaymentMethod(3, "Paypal", R.drawable.ic_gcashlogo)
        )

        val adapter = PaymentMethodAdapter(paymentMethods)
        binding.paymethodRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.paymethodRecyclerView.adapter = adapter

        binding.backButton.setOnClickListener {
            finish()
        }

        // Code to close keyboard when clicking outside the input fields
        binding.paymentLayout.setOnClickListener {
            val inputMethodManager =
                it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
        binding.mainLayout.setOnClickListener {
            val inputMethodManager =
                it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

        // Confirm donation button click listener
        binding.confirmPaymentBTN.setOnClickListener {
            val donationAmount = getDonationAmount()
            if (donationAmount > 0) {
                project?.let {
                    if (it.projFundsReceived + donationAmount <= it.projFundGoal) {
                        Log.d("DonateToProjectActivity", "Current Funds Received: ${project!!.projFundsReceived}")
                        Log.d("DonateToProjectActivity", "Donation Amount: $donationAmount")

                        val firestore = FirebaseFirestore.getInstance()
                        val projectDocRef = firestore.collection("projects").document(it.projId.toString())

                        val updatedProject = it.copy(
                            projFundsReceived = it.projFundsReceived + donationAmount,
                            projDonorsCount = it.projDonorsCount + 1
                        )

                        projectDocRef.set(updatedProject)
                            .addOnSuccessListener {
                                Log.d("DonateToProjectActivity", "Document updated successfully")
                                showSuccessDialog()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("DonateToProjectActivity", "Failed to update donation: ${exception.message}")
                                Toast.makeText(this, "Failed to update donation: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Donation exceeds funding goal", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.customAmountEditText.error = "Please enter or select a valid amount"
            }
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
        dialog.findViewById<Button>(R.id.gotoBrowseProjects).setOnClickListener {
            dialog.dismiss()
            // Navigate to BrowseProjectsFragment or Activity
            finish() // or startActivity(Intent(this, BrowseProjectsActivity::class.java))
        }
        dialog.show()
    }
}
