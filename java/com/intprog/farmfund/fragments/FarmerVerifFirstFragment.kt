package com.intprog.farmfund.fragments

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.intprog.farmfund.R
import com.intprog.farmfund.abstractclass.BaseFragment
import com.intprog.farmfund.databinding.FragmentFarmverifPersonalinfoBinding
import com.intprog.farmfund.viewmodels.FarmerVerifViewModel
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.regex.Pattern

class FarmerVerifFirstFragment : BaseFragment() {
    lateinit var viewModel: FarmerVerifViewModel
    private var _binding: FragmentFarmverifPersonalinfoBinding? = null
    val binding get() = _binding!!

    companion object {
        private const val REQUEST_CODE_SELFIE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmverifPersonalinfoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(FarmerVerifViewModel::class.java)

        setupFields()

        Glide.with(this).load(viewModel.selfieImage.value).into(binding.selfieImage)

        binding.selfieLayout.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_CODE_SELFIE)
                }
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_SELFIE
                )
            }
        }

        binding.birthDateDialogLayout.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                    binding.birthDateInputField.text = Editable.Factory.getInstance().newEditable(selectedDate)
                    viewModel.birthDate.value = selectedDate
                }, year, month, day).show()
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELFIE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            Glide.with(this).clear(binding.selfieImage)
            binding.selfieImage.setImageBitmap(imageBitmap)
            viewModel.selfieImage.value = imageBitmap
        }
    }

    private fun setupFields() {
        with(binding) {
            setupField(firstNameLayout, firstNameInputField, viewModel.firstName)
            setupField(lastNameLayout, lastNameInputField, viewModel.lastName)
            setupField(provinceLayout, provinceInputField, viewModel.province)
            setupField(municipalityLayout, municipalityInputField, viewModel.municipality)
            setupField(barangayLayout, barangayInputField, viewModel.barangay)
            setupField(zipCodeLayout, zipCodeInputField, viewModel.zipCode)
            setupField(phoneNumLayout, phoneNumInputField, viewModel.phoneNum)
            setupField(emailLayout, emailInputField, viewModel.email)
        }
    }

    private fun setupField(layout: TextInputLayout, field: EditText, liveData: MutableLiveData<String>) {
        field.setText(liveData.value)
        layout.isErrorEnabled = false
        field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                liveData.value = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Clear the error message without affecting the layout spacing
                if (s != null) {
                    if (s.isNotEmpty()) {
                        layout.error = null
                        layout.isErrorEnabled = false
                    }
                }
            }
        })
    }

    override fun validateInput(): Boolean {

        if (_binding == null) {
            return false
        }
        if (binding.firstNameInputField.text.toString().isEmpty()) {
            binding.firstNameLayout.error = "This field is required!"
            return false
        }
        if (binding.lastNameInputField.text.toString().isEmpty()) {
            binding.lastNameLayout.error = "This field is required!"
            return false
        }

        if (binding.birthDateInputField.text.toString().isEmpty()) {
            binding.birthDateLayout.error = "This field is required!"
            return false
        }

        if (binding.provinceInputField.text.toString().isEmpty()) {
            binding.provinceLayout.error = "This field is required!"
            return false
        }
        if (binding.municipalityInputField.text.toString().isEmpty()) {
            binding.municipalityLayout.error = "This field is required!"
            return false
        }
        if (binding.barangayInputField.text.toString().isEmpty()) {
            binding.barangayLayout.error = "This field is required!"
            return false
        }
        if (binding.zipCodeInputField.text.toString().isEmpty()) {
            binding.zipCodeLayout.error = "This field is required!"
            return false
        }

        val validEmailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val validPhonePattern = "^09[0-9]{9}$"

        val email = binding.emailInputField.text.toString()
        val phone = binding.phoneNumInputField.text.toString()

        if (email.isEmpty() && phone.isEmpty()) {
            binding.phoneNumLayout.error = "At least one field (phone or email) is required!"
            binding.emailLayout.error = "At least one field (phone or email) is required!"
            return false
        }

        if (email.isNotEmpty() && !Pattern.matches(validEmailPattern, email)) {
            binding.emailLayout.error = "Invalid email format!"
            return false
        }

        if (phone.isNotEmpty() && !Pattern.matches(validPhonePattern, phone)) {
            binding.phoneNumLayout.error = "Invalid phone number format!"
            return false
        }

        if (viewModel.selfieImage.value == null) {
            Toast.makeText(context, "Selfie image is required!", Toast.LENGTH_SHORT).show()
            binding.errorText2.visibility = View.VISIBLE
            return false
        }

        // If validation is successful, save the data in all the input fields to the FarmerVerifViewModel
        viewModel.firstName.value = binding.firstNameInputField.text.toString()
        viewModel.midName.value = binding.midNameInputField.text.toString()
        viewModel.lastName.value = binding.lastNameInputField.text.toString()
        viewModel.province.value = binding.provinceInputField.text.toString()
        viewModel.municipality.value = binding.municipalityInputField.text.toString()
        viewModel.barangay.value = binding.barangayInputField.text.toString()
        viewModel.zipCode.value = binding.zipCodeInputField.text.toString()
        viewModel.phoneNum.value = binding.phoneNumInputField.text.toString()
        viewModel.email.value = binding.emailInputField.text.toString()
        viewModel.birthDate.value = binding.birthDateInputField.text.toString()
        viewModel.selfieImage.value = viewModel.selfieImage.value

        return true
    }
}