package com.intprog.farmfund.activities

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.google.firebase.auth.FirebaseAuth
<<<<<<< HEAD
import com.google.firebase.database.FirebaseDatabase
=======
>>>>>>> e3e49fcbdc8f6e58d8937ae03cb059a1381c9420
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.PaymentMethodAdapter
import com.intprog.farmfund.databinding.ActivityDonateToProjectBinding
import com.intprog.farmfund.dataclasses.PaymentMethod
import com.intprog.farmfund.dataclasses.Project
import com.intprog.farmfund.objects.HideKeyboardOnClick

class DonateToProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonateToProjectBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonateToProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Retrieve data from intent
        val project = intent.getSerializableExtra("project") as? Project
        val imageUrl = intent.getStringExtra("imageUrl")

        if (project == null) {
            // Handle the error gracefully if the project is null
            Toast.makeText(this, "Project data not found", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
            return
        }

<<<<<<< HEAD
        val projTitle = project?.projTitle
        val projStatus = project?.projStatus
        val imageUrl = intent.getStringExtra("imageUrl")

        // Set the data to the views
        binding.projTitleDonate.text = projTitle
        binding.projStatusDonate.text = projStatus
        Glide.with(this).load(imageUrl).into(binding.projImageDonate) // Use Glide or any image loading library to load the image

        val checkBoxes = listOf(binding.box50, binding.box100, binding.box500, binding.box1000)
=======
        binding.projTitleDonate.text = project.projTitle
        binding.projFundGoalDonate.text = "${project?.projFundsReceived} / ${project?.projFundGoal}"
        Glide.with(this).load(imageUrl).into(binding.projFirstImageDonate)

        val checkBoxes =
            listOf(binding.box50, binding.box100, binding.box500, binding.box1000)
>>>>>>> e3e49fcbdc8f6e58d8937ae03cb059a1381c9420

        checkBoxes.forEach { checkBox ->
            checkBox.setBackgroundResource(R.drawable.bg_checkboxes_amount)
            checkBox.setOnClickListener {

                binding.customAmountEditText.text = null // Clear custom amount field
                binding.customAmountEditText.clearFocus()
                //Close keyboard
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
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkBoxes.forEach { otherCheckBox ->
                    otherCheckBox.isChecked = false
                    otherCheckBox.setTextColor(ContextCompat.getColorStateList(this@DonateToProjectActivity, R.color.black)) } }
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

        //Code to confirm donation
        binding.confirmPaymentBTN.setOnClickListener {
            val donationAmount = getDonationAmount()
            if (donationAmount > 0) {
                project.let {
                    if (it.projFundsReceived + donationAmount <= it.projFundGoal) {
                        Log.d("DonateToProjectActivity", "Current Funds Received: ${project!!.projFundsReceived}")
                        Log.d("DonateToProjectActivity", "Donation Amount: $donationAmount")

                        val firestore = FirebaseFirestore.getInstance()
                        val projectDocRef = firestore.collection("projects").document(it.projId)

                        val updatedProject = mapOf(
                            "projFundsReceived" to it.projFundsReceived + donationAmount,
                            "projDonorsCount" to it.projDonorsCount + 1
                        )

                        projectDocRef.update(updatedProject)
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
                Toast.makeText(this, "Please add amount to donate.", Toast.LENGTH_SHORT).show()
                binding.customAmountEditText.error = "Please enter or select a valid amount"
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        //Code to close keyboard when clicking outside the input fields
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
            // Navigate to BrowseProjectsFragment or Activity
            finish() // or startActivity(Intent(this, BrowseProjectsActivity::class.java))
        }
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
<<<<<<< HEAD

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
=======
>>>>>>> e3e49fcbdc8f6e58d8937ae03cb059a1381c9420
    }
}