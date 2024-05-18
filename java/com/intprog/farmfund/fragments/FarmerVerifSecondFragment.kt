package com.intprog.farmfund.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.intprog.farmfund.abstractclass.BaseFragment
import com.intprog.farmfund.adapters.FarmerVerificationFarmImagesAdapter
import com.intprog.farmfund.adapters.UploadProjectImageAdapter
import com.intprog.farmfund.databinding.FragmentFarmverifFarmdetailsBinding
import com.intprog.farmfund.viewmodels.FarmerVerifViewModel

class FarmerVerifSecondFragment : BaseFragment() {
    lateinit var viewModel: FarmerVerifViewModel
    private var _binding: FragmentFarmverifFarmdetailsBinding? = null
    val binding get() = _binding!!

    private var imageList = mutableListOf<Bitmap>() // This is where you'll store the images
    private lateinit var adapter2: FarmerVerificationFarmImagesAdapter
    private val PICK_IMAGE_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmverifFarmdetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[FarmerVerifViewModel::class.java]

        binding.farmLocationInputField.setText(viewModel.farmLocation.value)
        addTextWatcher(binding.farmLocationLayout, binding.farmLocationInputField, viewModel.farmLocation)

        binding.farmAreaInputField.setText(viewModel.farmArea.value)
        addTextWatcher(binding.farmAreaLayout, binding.farmAreaInputField, viewModel.farmArea)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Crops Farming", "Livestock Farming"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.farmTypeSpinner.adapter = adapter

        binding.farmTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (view != null) {
                    viewModel.farmType.value = parent.getItemAtPosition(position).toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        //Grid Layout of Image Uploads
        val numberOfColumns = 2
        binding.uploadFarmImagesRecyclerView.layoutManager = GridLayoutManager(context, numberOfColumns)
        adapter2 = FarmerVerificationFarmImagesAdapter(imageList, viewModel) // Pass the viewModel to the adapter
        binding.uploadFarmImagesRecyclerView.adapter = adapter2

        binding.imageViewUploadBTN.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)

            // Add the bitmap to your list of images and notify the adapter
            imageList.add(bitmap)
            adapter2.notifyDataSetChanged()

            // Update farmImages in the ViewModel
            viewModel.farmImages.value = imageList
        }
    }

    override fun validateInput(): Boolean {
        if (binding.farmLocationInputField.text.toString().isEmpty()) {
            binding.farmLocationLayout.error = "This field is required!"
            return false
        }

        if (binding.farmAreaInputField.text.toString().isEmpty()) {
            binding.farmAreaLayout.error = "This field is required!"
            return false
        }

        val selectedFarmType = binding.farmTypeSpinner.selectedItem.toString()
        if (selectedFarmType != "Crops Farming" && selectedFarmType != "Livestock Farming") {
            Toast.makeText(context, "Please select a valid farm type!", Toast.LENGTH_SHORT).show()
            binding.errorText1.visibility = View.VISIBLE
            return false
        }

        if (viewModel.farmImages.value.isNullOrEmpty()) {
            Toast.makeText(context, "Please upload at least one image!", Toast.LENGTH_SHORT).show()
            binding.errorText2.visibility = View.VISIBLE
            return false
        }

        viewModel.farmLocation.value = binding.farmLocationInputField.text.toString()
        viewModel.farmArea.value = binding.farmAreaInputField.text.toString()
        viewModel.farmType.value = binding.farmTypeSpinner.selectedItem.toString()

        return true
    }
    private fun addTextWatcher(layout: TextInputLayout, editText: EditText, liveData: MutableLiveData<String>) {
        layout.isErrorEnabled = false
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                liveData.value = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        layout.error = null
                        layout.isErrorEnabled = false
                    }
                }
            }
        })
    }
}