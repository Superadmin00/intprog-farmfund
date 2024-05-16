package com.intprog.farmfund.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intprog.farmfund.R
import com.intprog.farmfund.databinding.ActivityFarmerVerificationBinding

class FarmerVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFarmerVerificationBinding
    private val PICK_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFarmerVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val farmTypes = arrayOf(
            "Type of Farm",
            "Aquaculture Farming",
            "Cooperative Farming",
            "Hay Farming",
            "Organic Farming",
            "Pastoral Farming",
            "Arable Farming",
            "Mixed Farming",
            "Dairy Farming",
            "Poultry Farming",
            "Flower Farming"
        )

        val idTypes = arrayOf(
            "Passport",
            "Driver's License",
            "Philippine Postal ID",
            "Professional Regulation Commission (PRC) ID",
            "National Bureau of Investigation (NBI) Clearance",
            "Police Clearance",
            "Barangay Clearance",
            "Postal ID",
            "Tax Identification Number (TIN) ID"
        )

        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, farmTypes)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.farmTypeSpinner.adapter = adapter1

        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, idTypes)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.idSpinner.adapter = adapter2

        // Add text change listeners to enable/disable the button
        binding.farmNameEditText.addTextChangedListener(watcher)
        binding.cropTypeEdiText.addTextChangedListener(watcher)
        binding.farmLocationEditText.addTextChangedListener(watcher)

        // Trigger image picker when imageView is clicked
        // Use a loop to handle image picking
        val imageButtons = listOf(binding.imageView1, binding.imageView2, binding.imageView3)
        for (i in imageButtons.indices) {
            imageButtons[i].setOnClickListener {
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, PICK_IMAGE + i)
            }
        }

        binding.verifyButton.setOnClickListener {
            val emptyFields = getEmptyFields()
            if (emptyFields.isNotEmpty()) {
                Toast.makeText(this, "Please fill in the following fields: $emptyFields", Toast.LENGTH_SHORT).show()
            } else {
                val verifSubmiDialog =
                    LayoutInflater.from(this).inflate(R.layout.dialog_verification_submitted, null)
                val builder = AlertDialog.Builder(this).setView(verifSubmiDialog)
                val alertDialog = builder.show()
                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val gotoHome: Button = verifSubmiDialog.findViewById(R.id.gotoHome)
                gotoHome.setOnClickListener {
                    alertDialog.dismiss()
                    finish()
                    val intent = Intent(this, NavigatorActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            checkFields()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun checkFields() {
        // Enable the button if all fields are not empty
        binding.verifyButton.isEnabled = getEmptyFields().isEmpty()
    }


    private fun getEmptyFields(): String {
        val emptyFields = mutableListOf<String>()
        val textFields = listOf(
            binding.farmNameEditText,
            binding.cropTypeEdiText,
            binding.farmLocationEditText
        )
        val spinners = listOf(
            binding.farmTypeSpinner,
            binding.idSpinner
        )

        for (field in textFields) {
            if (field.text!!.isEmpty()) {
                emptyFields.add("\n${field.hint}")
            }
        }

        for (spinner in spinners) {
            if (spinner.selectedItemPosition == 0) {
                val hint = (spinner.adapter as ArrayAdapter<*>).getItem(0)
                emptyFields.add("\n$hint")
            }
        }

        // Existing image check using upImage1, upImage2, upImage3
        if (binding.upImage1.drawable == null || binding.upImage2.drawable == null || binding.upImage3.drawable == null) {
            emptyFields.add("\nImages")
        }

        return emptyFields.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode >= PICK_IMAGE && requestCode <= PICK_IMAGE + 2) {
            val imageUri = data?.data
            when (requestCode) {
                PICK_IMAGE -> binding.upImage1.setImageURI(imageUri)
                PICK_IMAGE + 1 -> binding.upImage2.setImageURI(imageUri)
                PICK_IMAGE + 2 -> binding.upImage3.setImageURI(imageUri)
            }
        }
    }
}